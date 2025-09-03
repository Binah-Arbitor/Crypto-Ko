package com.cryptoko.crypto

import com.cryptoko.utils.KeyDerivation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.io.*
import java.security.SecureRandom
import java.security.Security
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
            
            progressCallback(CryptoResult.Progress(0, "Initializing encryption..."))
            
            // Generate salt and derive key using PBKDF2
            val (key, salt) = KeyDerivation.deriveKey(
                password = config.password,
                keyLength = config.algorithm.keySize / 8
            )
            
            val cipher = createCipher(config.algorithm, config.mode, key, true)
            
            // Generate IV for block ciphers
            val iv = if (needsIV(config.algorithm, config.mode)) {
                generateIV(config.algorithm.blockSize)
            } else null
            
            if (iv != null) {
                initializeCipher(cipher, Cipher.ENCRYPT_MODE, key, config.mode, iv)
            } else {
                cipher.init(Cipher.ENCRYPT_MODE, key)
            }
            
            progressCallback(CryptoResult.Progress(5, "Starting encryption..."))
            
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
                            
                            val progress = ((totalRead * 90) / fileSize).toInt() + 5
                            progressCallback(CryptoResult.Progress(progress, "Encrypting..."))
                        }
                    }
                }
            }
            
            // Clear sensitive data
            KeyDerivation.clearByteArray(salt)
            iv?.let { KeyDerivation.clearByteArray(it) }
            
            progressCallback(CryptoResult.Progress(100, "Encryption completed"))
            CryptoResult.Success("File encrypted successfully to ${outputFile.absolutePath}")
            
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
            
            progressCallback(CryptoResult.Progress(0, "Initializing decryption..."))
            
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
                
                val cipher = createCipher(config.algorithm, config.mode, key, false)
                
                // Read IV from file if needed
                val iv = if (needsIV(config.algorithm, config.mode)) {
                    val ivBytes = ByteArray(config.algorithm.blockSize)
                    fis.read(ivBytes)
                    ivBytes
                } else null
                
                if (iv != null) {
                    initializeCipher(cipher, Cipher.DECRYPT_MODE, key, config.mode, iv)
                } else {
                    cipher.init(Cipher.DECRYPT_MODE, key)
                }
                
                progressCallback(CryptoResult.Progress(5, "Starting decryption..."))
                
                FileOutputStream(outputFile).use { fos ->
                    CipherInputStream(fis, cipher).use { cis ->
                        val buffer = ByteArray(BUFFER_SIZE)
                        var totalRead = 0L
                        var bytesRead: Int
                        
                        while (cis.read(buffer).also { bytesRead = it } != -1) {
                            fos.write(buffer, 0, bytesRead)
                            totalRead += bytesRead
                            
                            val progress = ((totalRead * 90) / fileSize).toInt() + 5
                            progressCallback(CryptoResult.Progress(progress, "Decrypting..."))
                        }
                    }
                }
                
                // Clear sensitive data
                KeyDerivation.clearByteArray(salt)
                iv?.let { KeyDerivation.clearByteArray(it) }
            }
            
            progressCallback(CryptoResult.Progress(100, "Decryption completed"))
            CryptoResult.Success("File decrypted successfully to ${outputFile.absolutePath}")
            
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
}