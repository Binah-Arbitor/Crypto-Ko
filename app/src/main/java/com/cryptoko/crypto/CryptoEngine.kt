package com.cryptoko.crypto

/**
 * Result of an encryption/decryption operation
 */
sealed class CryptoResult {
    data class Success(val message: String) : CryptoResult()
    data class Error(val error: String, val exception: Throwable? = null) : CryptoResult()
    data class Progress(
        val percentage: Int, 
        val message: String = "",
        val currentBlock: Int = 0,
        val totalBlocks: Int = 0,
        val bytesProcessed: Long = 0,
        val totalBytes: Long = 0
    ) : CryptoResult()
}

/**
 * Configuration for encryption/decryption operations
 */
data class CryptoConfig(
    val algorithm: CipherAlgorithm,
    val mode: String,
    val password: String,
    val inputFile: String,
    val outputFile: String? = null,
    val threadCount: Int = 1,
    val enableMultithreading: Boolean = true
) {
    fun validate(): CryptoResult? {
        if (password.isEmpty()) {
            return CryptoResult.Error("Password cannot be empty")
        }
        if (!algorithm.supportedModes.contains(mode)) {
            return CryptoResult.Error("Mode $mode is not supported for algorithm ${algorithm.name}")
        }
        if (threadCount < 1) {
            return CryptoResult.Error("Thread count must be at least 1")
        }
        return null
    }
    
    /**
     * Check if the algorithm/mode combination supports multithreading
     */
    fun supportsMultithreading(): Boolean {
        return enableMultithreading && threadCount > 1 && when (mode) {
            "ECB", "CTR", "OFB", "GCM" -> true
            "CBC", "CFB" -> false  // Sequential dependency
            "NONE" -> algorithm.algorithmName != "RC4" && algorithm.algorithmName != "ChaCha20"  // Stream ciphers are sequential
            else -> false
        }
    }
}

/**
 * Interface for crypto operations with progress callbacks
 */
interface CryptoEngine {
    suspend fun encrypt(
        config: CryptoConfig,
        progressCallback: (CryptoResult.Progress) -> Unit = {}
    ): CryptoResult
    
    suspend fun decrypt(
        config: CryptoConfig,
        progressCallback: (CryptoResult.Progress) -> Unit = {}
    ): CryptoResult
    
    fun isSupported(algorithm: CipherAlgorithm, mode: String): Boolean
}