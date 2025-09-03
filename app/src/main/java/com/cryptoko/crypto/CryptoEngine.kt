package com.cryptoko.crypto

/**
 * Result of an encryption/decryption operation
 */
sealed class CryptoResult {
    data class Success(val message: String) : CryptoResult()
    data class Error(val error: String, val exception: Throwable? = null) : CryptoResult()
    data class Progress(val percentage: Int, val message: String = "") : CryptoResult()
}

/**
 * Configuration for encryption/decryption operations
 */
data class CryptoConfig(
    val algorithm: CipherAlgorithm,
    val mode: String,
    val password: String,
    val inputFile: String,
    val outputFile: String? = null
) {
    fun validate(): CryptoResult? {
        if (password.isEmpty()) {
            return CryptoResult.Error("Password cannot be empty")
        }
        if (!algorithm.supportedModes.contains(mode)) {
            return CryptoResult.Error("Mode $mode is not supported for algorithm ${algorithm.name}")
        }
        return null
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