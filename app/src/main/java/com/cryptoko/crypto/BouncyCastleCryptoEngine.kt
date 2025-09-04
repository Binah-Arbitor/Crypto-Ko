package com.cryptoko.crypto

import com.cryptoko.utils.KeyDerivation
import kotlinx.coroutines.*
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.io.*
import java.security.SecureRandom
import java.security.Security
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicLong
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Enhanced implementation of CryptoEngine using Bouncy Castle provider
 * Features improved key derivation and security measures
 */
class BouncyCastleCryptoEngine : CryptoEngine {
    
    companion object {
        private const val BUFFER_SIZE = 8192
        private const val IV_SIZE = 16
        private const val GCM_TAG_LENGTH = 16
        private const val SALT_SIZE = 16
        private const val MIN_CHUNK_SIZE = 64 * 1024  // 64KB minimum chunk size for multithreading
        
        init {
            // Add Bouncy Castle provider for additional algorithms
            if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
                Security.addProvider(BouncyCastleProvider())
            }
        }
    }
    
    override suspend fun encrypt(
        config: CryptoConfig,
        progressCallback: (CryptoResult.Progress) -> Unit
    ): CryptoResult = withContext(Dispatchers.IO) {
        
        val validationError = config.validate()
        if (validationError != null) return@withContext validationError
        
        try {
            val inputFile = File(config.inputFile)
            if (!inputFile.exists()) {
                return@withContext CryptoResult.Error("Input file does not exist")
            }
            
            val outputFile = File(config.outputFile ?: "${config.inputFile}.enc")
            val fileSize = inputFile.length()
            
            progressCallback(CryptoResult.Progress(0, "Initializing encryption...", totalBytes = fileSize))
            
            // Generate salt and derive key using PBKDF2
            val (key, salt) = KeyDerivation.deriveKey(
                password = config.password,
                keyLength = config.algorithm.keySize / 8
            )
            
            // Generate IV for block ciphers
            val iv = if (needsIV(config.algorithm, config.mode)) {
                generateIV(config.algorithm.blockSize)
            } else null
            
            // Determine if we should use multithreading
            val useMultithreading = config.supportsMultithreading() && 
                                   fileSize > MIN_CHUNK_SIZE * config.threadCount
            
            val result = if (useMultithreading) {
                encryptMultithreaded(config, inputFile, outputFile, key, salt, iv, progressCallback)
            } else {
                encryptSingleThreaded(config, inputFile, outputFile, key, salt, iv, progressCallback)
            }
            
            // Clear sensitive data
            KeyDerivation.clearByteArray(salt)
            iv?.let { KeyDerivation.clearByteArray(it) }
            
            result
            
        } catch (e: Exception) {
            CryptoResult.Error("Encryption failed: ${e.message}", e)
        }
    }
    
    override suspend fun decrypt(
        config: CryptoConfig,
        progressCallback: (CryptoResult.Progress) -> Unit
    ): CryptoResult = withContext(Dispatchers.IO) {
        
        val validationError = config.validate()
        if (validationError != null) return@withContext validationError
        
        try {
            val inputFile = File(config.inputFile)
            if (!inputFile.exists()) {
                return@withContext CryptoResult.Error("Input file does not exist")
            }
            
            val outputFile = File(config.outputFile ?: config.inputFile.removeSuffix(".enc"))
            val fileSize = inputFile.length()
            
            progressCallback(CryptoResult.Progress(0, "Initializing decryption...", totalBytes = fileSize))
            
            FileInputStream(inputFile).use { fis ->
                // Read salt from file
                val salt = ByteArray(SALT_SIZE)
                fis.read(salt)
                
                // Derive key using same salt
                val (key, _) = KeyDerivation.deriveKey(
                    password = config.password,
                    salt = salt,
                    keyLength = config.algorithm.keySize / 8
                )
                
                // Read IV from file if needed
                val iv = if (needsIV(config.algorithm, config.mode)) {
                    val ivBytes = ByteArray(config.algorithm.blockSize)
                    fis.read(ivBytes)
                    ivBytes
                } else null
                
                val headerSize = SALT_SIZE + (iv?.size ?: 0)
                val dataSize = fileSize - headerSize
                
                // Determine if we should use multithreading
                val useMultithreading = config.supportsMultithreading() && 
                                       dataSize > MIN_CHUNK_SIZE * config.threadCount
                
                val result = if (useMultithreading) {
                    decryptMultithreaded(config, inputFile, outputFile, key, salt, iv, headerSize, progressCallback)
                } else {
                    decryptSingleThreaded(config, inputFile, outputFile, key, salt, iv, progressCallback)
                }
                
                // Clear sensitive data
                KeyDerivation.clearByteArray(salt)
                iv?.let { KeyDerivation.clearByteArray(it) }
                
                result
            }
            
        } catch (e: Exception) {
            CryptoResult.Error("Decryption failed: ${e.message}", e)
        }
    }
    
    override fun isSupported(algorithm: CipherAlgorithm, mode: String): Boolean {
        return try {
            val (testKey, _) = KeyDerivation.deriveKey("test", keyLength = algorithm.keySize / 8)
            createCipher(algorithm, mode, testKey, true)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    private fun createCipher(
        algorithm: CipherAlgorithm, 
        mode: String, 
        key: SecretKeySpec, 
        forEncryption: Boolean
    ): Cipher {
        val transformation = when {
            mode == "NONE" -> algorithm.algorithmName
            algorithm.algorithmName == "RC4" -> "RC4"
            algorithm.algorithmName == "ChaCha20" -> "ChaCha20"
            else -> "${algorithm.algorithmName}/$mode/PKCS5Padding"
        }
        
        return Cipher.getInstance(transformation)
    }
    
    private fun needsIV(algorithm: CipherAlgorithm, mode: String): Boolean {
        return mode != "ECB" && mode != "NONE" && algorithm.algorithmName != "RC4"
    }
    
    private fun generateIV(blockSize: Int): ByteArray {
        val iv = ByteArray(blockSize)
        SecureRandom().nextBytes(iv)
        return iv
    }
    
    private fun initializeCipher(
        cipher: Cipher,
        mode: Int,
        key: SecretKeySpec,
        cipherMode: String,
        iv: ByteArray
    ) {
        when (cipherMode) {
            "GCM" -> {
                val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)
                cipher.init(mode, key, gcmSpec)
            }
            else -> {
                val ivSpec = IvParameterSpec(iv)
                cipher.init(mode, key, ivSpec)
            }
        }
    }
    
    /**
     * Single-threaded encryption for sequential algorithms or small files
     */
    private suspend fun encryptSingleThreaded(
        config: CryptoConfig,
        inputFile: File,
        outputFile: File,
        key: SecretKeySpec,
        salt: ByteArray,
        iv: ByteArray?,
        progressCallback: (CryptoResult.Progress) -> Unit
    ): CryptoResult {
        val cipher = createCipher(config.algorithm, config.mode, key, true)
        
        if (iv != null) {
            initializeCipher(cipher, Cipher.ENCRYPT_MODE, key, config.mode, iv)
        } else {
            cipher.init(Cipher.ENCRYPT_MODE, key)
        }
        
        progressCallback(CryptoResult.Progress(5, "Starting encryption...", totalBytes = inputFile.length()))
        
        FileInputStream(inputFile).use { fis ->
            FileOutputStream(outputFile).use { fos ->
                // Write metadata header: salt + IV (if needed)
                fos.write(salt)
                if (iv != null) {
                    fos.write(iv)
                }
                
                CipherOutputStream(fos, cipher).use { cos ->
                    val buffer = ByteArray(BUFFER_SIZE)
                    var totalRead = 0L
                    var bytesRead: Int
                    
                    while (fis.read(buffer).also { bytesRead = it } != -1) {
                        cos.write(buffer, 0, bytesRead)
                        totalRead += bytesRead
                        
                        val progress = ((totalRead * 90) / inputFile.length()).toInt() + 5
                        progressCallback(CryptoResult.Progress(
                            progress, 
                            "Encrypting...", 
                            bytesProcessed = totalRead,
                            totalBytes = inputFile.length()
                        ))
                    }
                }
            }
        }
        
        progressCallback(CryptoResult.Progress(100, "Encryption completed", totalBytes = inputFile.length(), bytesProcessed = inputFile.length()))
        return CryptoResult.Success("File encrypted successfully to ${outputFile.absolutePath}")
    }
    
    /**
     * Multithreaded encryption for parallelizable algorithms and large files
     */
    private suspend fun encryptMultithreaded(
        config: CryptoConfig,
        inputFile: File,
        outputFile: File,
        key: SecretKeySpec,
        salt: ByteArray,
        iv: ByteArray?,
        progressCallback: (CryptoResult.Progress) -> Unit
    ): CryptoResult = withContext(Dispatchers.IO) {
        val fileSize = inputFile.length()
        val chunkSize = (fileSize / config.threadCount).coerceAtLeast(MIN_CHUNK_SIZE.toLong())
        val totalChunks = ((fileSize + chunkSize - 1) / chunkSize).toInt()
        
        progressCallback(CryptoResult.Progress(
            5, 
            "Starting multithreaded encryption with ${config.threadCount} threads...",
            totalBlocks = totalChunks,
            totalBytes = fileSize
        ))
        
        // Create temporary files for each chunk
        val tempFiles = mutableListOf<File>()
        val processedBytes = AtomicLong(0)
        val completedBlocks = AtomicLong(0)
        
        try {
            // Use fixed thread pool for controlled concurrency
            val executor = Executors.newFixedThreadPool(config.threadCount)
            val futures = mutableListOf<Future<Unit>>()
            
            // Process chunks in parallel
            for (chunkIndex in 0 until totalChunks) {
                val startOffset = chunkIndex * chunkSize
                val endOffset = minOf(startOffset + chunkSize, fileSize)
                val actualChunkSize = endOffset - startOffset
                
                val tempFile = File.createTempFile("cryptoko_chunk_${chunkIndex}", ".tmp")
                tempFiles.add(tempFile)
                
                val future = executor.submit<Unit> {
                    processChunk(
                        config, inputFile, tempFile, key, iv, 
                        startOffset, actualChunkSize, chunkIndex,
                        processedBytes, completedBlocks, totalChunks, fileSize, progressCallback
                    )
                }
                futures.add(future)
            }
            
            // Wait for all chunks to complete
            futures.forEach { it.get() }
            executor.shutdown()
            
            // Combine chunks into final output file
            FileOutputStream(outputFile).use { fos ->
                // Write metadata header: salt + IV (if needed)
                fos.write(salt)
                if (iv != null) {
                    fos.write(iv)
                }
                
                // Append all encrypted chunks in order
                tempFiles.forEach { tempFile ->
                    FileInputStream(tempFile).use { fis ->
                        fis.copyTo(fos)
                    }
                }
            }
            
            progressCallback(CryptoResult.Progress(
                100, 
                "Multithreaded encryption completed",
                currentBlock = totalChunks,
                totalBlocks = totalChunks,
                bytesProcessed = fileSize,
                totalBytes = fileSize
            ))
            
            CryptoResult.Success("File encrypted successfully to ${outputFile.absolutePath} using ${config.threadCount} threads")
            
        } finally {
            // Clean up temporary files
            tempFiles.forEach { it.delete() }
        }
    }
    
    /**
     * Process a single chunk of the file for multithreaded encryption
     */
    private fun processChunk(
        config: CryptoConfig,
        inputFile: File,
        outputFile: File,
        key: SecretKeySpec,
        iv: ByteArray?,
        startOffset: Long,
        chunkSize: Long,
        chunkIndex: Int,
        processedBytes: AtomicLong,
        completedBlocks: AtomicLong,
        totalBlocks: Int,
        totalFileSize: Long,
        progressCallback: (CryptoResult.Progress) -> Unit
    ) {
        try {
            // For ECB mode, we can process chunks independently
            // For CTR mode, we need to adjust the counter for each chunk
            val cipher = createCipher(config.algorithm, config.mode, key, true)
            
            when (config.mode) {
                "ECB" -> {
                    cipher.init(Cipher.ENCRYPT_MODE, key)
                }
                "CTR" -> {
                    // For CTR mode, adjust the IV/counter for this chunk
                    val adjustedIv = adjustCounterForOffset(iv!!, startOffset, config.algorithm.blockSize)
                    val ivSpec = IvParameterSpec(adjustedIv)
                    cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec)
                }
                else -> {
                    if (iv != null) {
                        initializeCipher(cipher, Cipher.ENCRYPT_MODE, key, config.mode, iv)
                    } else {
                        cipher.init(Cipher.ENCRYPT_MODE, key)
                    }
                }
            }
            
            RandomAccessFile(inputFile, "r").use { raf ->
                raf.seek(startOffset)
                
                FileOutputStream(outputFile).use { fos ->
                    CipherOutputStream(fos, cipher).use { cos ->
                        val buffer = ByteArray(BUFFER_SIZE)
                        var remaining = chunkSize
                        
                        while (remaining > 0) {
                            val toRead = minOf(buffer.size.toLong(), remaining).toInt()
                            val bytesRead = raf.read(buffer, 0, toRead)
                            
                            if (bytesRead == -1) break
                            
                            cos.write(buffer, 0, bytesRead)
                            remaining -= bytesRead
                            
                            val currentProcessed = processedBytes.addAndGet(bytesRead.toLong())
                            val progress = ((currentProcessed * 90) / totalFileSize).toInt() + 5
                            
                            progressCallback(CryptoResult.Progress(
                                progress,
                                "Encrypting chunk ${chunkIndex + 1}/$totalBlocks...",
                                currentBlock = completedBlocks.get().toInt(),
                                totalBlocks = totalBlocks,
                                bytesProcessed = currentProcessed,
                                totalBytes = totalFileSize
                            ))
                        }
                    }
                }
            }
            
            completedBlocks.incrementAndGet()
            
        } catch (e: Exception) {
            throw RuntimeException("Failed to process chunk $chunkIndex: ${e.message}", e)
        }
    }
    
    /**
     * Adjust CTR mode counter for chunk offset
     */
    private fun adjustCounterForOffset(iv: ByteArray, offset: Long, blockSize: Int): ByteArray {
        val adjustedIv = iv.copyOf()
        val blockOffset = offset / blockSize
        
        // Add the block offset to the counter (big-endian)
        var carry = blockOffset
        for (i in adjustedIv.indices.reversed()) {
            val sum = (adjustedIv[i].toInt() and 0xFF) + (carry and 0xFF)
            adjustedIv[i] = sum.toByte()
            carry = carry shr 8
            if (carry == 0L) break
        }
        
        return adjustedIv
    }
    
    /**
     * Single-threaded decryption for sequential algorithms or small files
     */
    private suspend fun decryptSingleThreaded(
        config: CryptoConfig,
        inputFile: File,
        outputFile: File,
        key: SecretKeySpec,
        salt: ByteArray,
        iv: ByteArray?,
        progressCallback: (CryptoResult.Progress) -> Unit
    ): CryptoResult {
        val cipher = createCipher(config.algorithm, config.mode, key, false)
        
        if (iv != null) {
            initializeCipher(cipher, Cipher.DECRYPT_MODE, key, config.mode, iv)
        } else {
            cipher.init(Cipher.DECRYPT_MODE, key)
        }
        
        progressCallback(CryptoResult.Progress(5, "Starting decryption...", totalBytes = inputFile.length()))
        
        FileInputStream(inputFile).use { fis ->
            // Skip header (salt + IV)
            fis.skip((SALT_SIZE + (iv?.size ?: 0)).toLong())
            
            FileOutputStream(outputFile).use { fos ->
                CipherInputStream(fis, cipher).use { cis ->
                    val buffer = ByteArray(BUFFER_SIZE)
                    var totalRead = 0L
                    var bytesRead: Int
                    
                    while (cis.read(buffer).also { bytesRead = it } != -1) {
                        fos.write(buffer, 0, bytesRead)
                        totalRead += bytesRead
                        
                        val progress = ((totalRead * 90) / inputFile.length()).toInt() + 5
                        progressCallback(CryptoResult.Progress(
                            progress,
                            "Decrypting...",
                            bytesProcessed = totalRead,
                            totalBytes = inputFile.length()
                        ))
                    }
                }
            }
        }
        
        progressCallback(CryptoResult.Progress(100, "Decryption completed", totalBytes = inputFile.length(), bytesProcessed = inputFile.length()))
        return CryptoResult.Success("File decrypted successfully to ${outputFile.absolutePath}")
    }
    
    /**
     * Multithreaded decryption for parallelizable algorithms and large files
     */
    private suspend fun decryptMultithreaded(
        config: CryptoConfig,
        inputFile: File,
        outputFile: File,
        key: SecretKeySpec,
        salt: ByteArray,
        iv: ByteArray?,
        headerSize: Int,
        progressCallback: (CryptoResult.Progress) -> Unit
    ): CryptoResult = withContext(Dispatchers.IO) {
        val fileSize = inputFile.length()
        val dataSize = fileSize - headerSize
        val chunkSize = (dataSize / config.threadCount).coerceAtLeast(MIN_CHUNK_SIZE.toLong())
        val totalChunks = ((dataSize + chunkSize - 1) / chunkSize).toInt()
        
        progressCallback(CryptoResult.Progress(
            5,
            "Starting multithreaded decryption with ${config.threadCount} threads...",
            totalBlocks = totalChunks,
            totalBytes = fileSize
        ))
        
        // Create temporary files for each chunk
        val tempFiles = mutableListOf<File>()
        val processedBytes = AtomicLong(0)
        val completedBlocks = AtomicLong(0)
        
        try {
            // Use fixed thread pool for controlled concurrency
            val executor = Executors.newFixedThreadPool(config.threadCount)
            val futures = mutableListOf<Future<Unit>>()
            
            // Process chunks in parallel
            for (chunkIndex in 0 until totalChunks) {
                val startOffset = headerSize + chunkIndex * chunkSize
                val endOffset = minOf(startOffset + chunkSize, fileSize)
                val actualChunkSize = endOffset - startOffset
                
                val tempFile = File.createTempFile("cryptoko_decrypt_chunk_${chunkIndex}", ".tmp")
                tempFiles.add(tempFile)
                
                val future = executor.submit<Unit> {
                    processDecryptChunk(
                        config, inputFile, tempFile, key, iv,
                        startOffset, actualChunkSize, chunkIndex, headerSize,
                        processedBytes, completedBlocks, totalChunks, fileSize, progressCallback
                    )
                }
                futures.add(future)
            }
            
            // Wait for all chunks to complete
            futures.forEach { it.get() }
            executor.shutdown()
            
            // Combine chunks into final output file
            FileOutputStream(outputFile).use { fos ->
                // Append all decrypted chunks in order
                tempFiles.forEach { tempFile ->
                    FileInputStream(tempFile).use { fis ->
                        fis.copyTo(fos)
                    }
                }
            }
            
            progressCallback(CryptoResult.Progress(
                100,
                "Multithreaded decryption completed",
                currentBlock = totalChunks,
                totalBlocks = totalChunks,
                bytesProcessed = fileSize,
                totalBytes = fileSize
            ))
            
            CryptoResult.Success("File decrypted successfully to ${outputFile.absolutePath} using ${config.threadCount} threads")
            
        } finally {
            // Clean up temporary files
            tempFiles.forEach { it.delete() }
        }
    }
    
    /**
     * Process a single chunk of the file for multithreaded decryption
     */
    private fun processDecryptChunk(
        config: CryptoConfig,
        inputFile: File,
        outputFile: File,
        key: SecretKeySpec,
        iv: ByteArray?,
        startOffset: Long,
        chunkSize: Long,
        chunkIndex: Int,
        headerSize: Int,
        processedBytes: AtomicLong,
        completedBlocks: AtomicLong,
        totalBlocks: Int,
        totalFileSize: Long,
        progressCallback: (CryptoResult.Progress) -> Unit
    ) {
        try {
            val cipher = createCipher(config.algorithm, config.mode, key, false)
            
            when (config.mode) {
                "ECB" -> {
                    cipher.init(Cipher.DECRYPT_MODE, key)
                }
                "CTR" -> {
                    // For CTR mode, adjust the IV/counter for this chunk
                    val adjustedIv = adjustCounterForOffset(iv!!, startOffset - headerSize, config.algorithm.blockSize)
                    val ivSpec = IvParameterSpec(adjustedIv)
                    cipher.init(Cipher.DECRYPT_MODE, key, ivSpec)
                }
                else -> {
                    if (iv != null) {
                        initializeCipher(cipher, Cipher.DECRYPT_MODE, key, config.mode, iv)
                    } else {
                        cipher.init(Cipher.DECRYPT_MODE, key)
                    }
                }
            }
            
            RandomAccessFile(inputFile, "r").use { raf ->
                raf.seek(startOffset)
                
                FileOutputStream(outputFile).use { fos ->
                    CipherOutputStream(fos, cipher).use { cos ->
                        val buffer = ByteArray(BUFFER_SIZE)
                        var remaining = chunkSize
                        
                        while (remaining > 0) {
                            val toRead = minOf(buffer.size.toLong(), remaining).toInt()
                            val bytesRead = raf.read(buffer, 0, toRead)
                            
                            if (bytesRead == -1) break
                            
                            cos.write(buffer, 0, bytesRead)
                            remaining -= bytesRead
                            
                            val currentProcessed = processedBytes.addAndGet(bytesRead.toLong())
                            val progress = ((currentProcessed * 90) / totalFileSize).toInt() + 5
                            
                            progressCallback(CryptoResult.Progress(
                                progress,
                                "Decrypting chunk ${chunkIndex + 1}/$totalBlocks...",
                                currentBlock = completedBlocks.get().toInt(),
                                totalBlocks = totalBlocks,
                                bytesProcessed = currentProcessed,
                                totalBytes = totalFileSize
                            ))
                        }
                    }
                }
            }
            
            completedBlocks.incrementAndGet()
            
        } catch (e: Exception) {
            throw RuntimeException("Failed to decrypt chunk $chunkIndex: ${e.message}", e)
        }
    }
}