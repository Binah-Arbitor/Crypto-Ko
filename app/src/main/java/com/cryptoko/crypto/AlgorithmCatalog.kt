package com.cryptoko.crypto

import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security

/**
 * Hardcoded but modular catalog of supported encryption algorithms
 * Replaces the complex regex-based dynamic discovery system with a simple, reliable approach
 */
object AlgorithmCatalog {
    
    init {
        // Ensure Bouncy Castle is registered
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(BouncyCastleProvider())
        }
    }
    
    /**
     * Security ranking of algorithms (higher number = better security)
     * Based on cryptographic security strength and current standards
     */
    private val ALGORITHM_SECURITY_RANKING = mapOf(
        "AES" to 10,        // Modern standard, excellent security with various key sizes
        "ChaCha20" to 9,    // Modern stream cipher with excellent security properties
        "ARIA" to 8,        // Korean standard, similar security level to AES
        "Camellia" to 8,    // Similar security level to AES, approved by NESSIE
        "Twofish" to 7,     // AES finalist, strong security design
        "Serpent" to 7,     // AES finalist, excellent security
        "SM4" to 6,         // Chinese national standard, relatively secure
        "Blowfish" to 6,    // Variable key, reasonably secure for most applications
        "Threefish" to 6,   // Part of Skein hash function family
        "Salsa20" to 6,     // Good stream cipher security
        "SEED" to 5,        // Korean standard, decent security
        "IDEA" to 5,        // Good security design, but aging
        "GOST28147" to 5,   // Russian standard
        "CAST5" to 4,       // Reasonable security for legacy applications
        "CAST6" to 4,       // CAST family cipher
        "3DES" to 4,        // Triple DES, legacy but significantly better than DES
        "RC5" to 4,         // Variable parameters, decent security
        "RC6" to 4,         // AES finalist candidate
        "XTEA" to 4,        // Improved TEA
        "Mars" to 4,        // AES finalist candidate
        "RC2" to 3,         // Weak cipher, should be avoided
        "RC4" to 3,         // Deprecated due to known vulnerabilities
        "TEA" to 3,         // Simple but weak
        "Skipjack" to 3,    // NSA cipher, limited key size
        "DES" to 2,         // Broken encryption, should not be used
        "None" to 1         // No encryption
    )
    
    /**
     * Hardcoded algorithm definitions with their supported configurations
     * This replaces the complex dynamic discovery with reliable, tested configurations
     */
    private val ALGORITHM_DEFINITIONS = listOf(
        // AES - Modern standard encryption
        AlgorithmDefinition("AES", listOf(128, 192, 256), listOf("CBC", "CFB", "OFB", "ECB", "GCM", "CTR"), 16),
        
        // ChaCha20 - Modern stream cipher
        AlgorithmDefinition("ChaCha20", listOf(256), listOf("STREAM"), 1),
        
        // ARIA - Korean standard
        AlgorithmDefinition("ARIA", listOf(128, 192, 256), listOf("CBC", "CFB", "OFB", "ECB", "GCM", "CTR"), 16),
        
        // Camellia - Japanese standard
        AlgorithmDefinition("Camellia", listOf(128, 192, 256), listOf("CBC", "CFB", "OFB", "ECB", "GCM", "CTR"), 16),
        
        // Twofish - AES finalist
        AlgorithmDefinition("Twofish", listOf(128, 192, 256), listOf("CBC", "CFB", "OFB", "ECB"), 16),
        
        // Serpent - AES finalist
        AlgorithmDefinition("Serpent", listOf(128, 192, 256), listOf("CBC", "CFB", "OFB", "ECB"), 16),
        
        // SM4 - Chinese standard
        AlgorithmDefinition("SM4", listOf(128), listOf("CBC", "CFB", "OFB", "ECB", "CTR"), 16),
        
        // Blowfish - Variable key length
        AlgorithmDefinition("Blowfish", listOf(32, 64, 128, 192, 256, 320, 384, 448), listOf("CBC", "CFB", "OFB", "ECB"), 8),
        
        // Threefish - Part of Skein hash family
        AlgorithmDefinition("Threefish", listOf(256, 512, 1024), listOf("CBC", "CFB", "OFB", "ECB"), 32),
        
        // Salsa20 - Stream cipher
        AlgorithmDefinition("Salsa20", listOf(128, 256), listOf("STREAM"), 1),
        
        // SEED - Korean standard
        AlgorithmDefinition("SEED", listOf(128), listOf("CBC", "CFB", "OFB", "ECB"), 16),
        
        // IDEA - International Data Encryption Algorithm
        AlgorithmDefinition("IDEA", listOf(128), listOf("CBC", "CFB", "OFB", "ECB"), 8),
        
        // GOST28147 - Russian standard
        AlgorithmDefinition("GOST28147", listOf(256), listOf("CBC", "CFB", "OFB", "ECB"), 8),
        
        // CAST5 - CAST-128
        AlgorithmDefinition("CAST5", listOf(40, 64, 80, 128), listOf("CBC", "CFB", "OFB", "ECB"), 8),
        
        // CAST6 - CAST-256
        AlgorithmDefinition("CAST6", listOf(128, 160, 192, 224, 256), listOf("CBC", "CFB", "OFB", "ECB"), 16),
        
        // 3DES - Triple DES
        AlgorithmDefinition("3DES", listOf(112, 168), listOf("CBC", "CFB", "OFB", "ECB"), 8),
        
        // RC5 - Variable parameters
        AlgorithmDefinition("RC5", listOf(32, 64, 128, 192, 256), listOf("CBC", "CFB", "OFB", "ECB"), 8),
        
        // RC6 - AES finalist
        AlgorithmDefinition("RC6", listOf(128, 192, 256), listOf("CBC", "CFB", "OFB", "ECB"), 16),
        
        // XTEA - Extended TEA
        AlgorithmDefinition("XTEA", listOf(128), listOf("CBC", "CFB", "OFB", "ECB"), 8),
        
        // Mars - AES finalist
        AlgorithmDefinition("Mars", listOf(128, 192, 256), listOf("CBC", "CFB", "OFB", "ECB"), 16),
        
        // RC2 - Weak cipher (included for legacy compatibility)
        AlgorithmDefinition("RC2", listOf(40, 64, 128), listOf("CBC", "CFB", "OFB", "ECB"), 8),
        
        // RC4 - Stream cipher (weak, included for legacy)
        AlgorithmDefinition("RC4", listOf(40, 56, 64, 80, 128, 256), listOf("STREAM"), 1),
        
        // TEA - Tiny Encryption Algorithm (weak)
        AlgorithmDefinition("TEA", listOf(128), listOf("CBC", "CFB", "OFB", "ECB"), 8),
        
        // Skipjack - NSA cipher
        AlgorithmDefinition("Skipjack", listOf(80), listOf("CBC", "CFB", "OFB", "ECB"), 8),
        
        // DES - Data Encryption Standard (weak, included for legacy)
        AlgorithmDefinition("DES", listOf(56), listOf("CBC", "CFB", "OFB", "ECB"), 8)
    )
    
    /**
     * Stream ciphers that don't use traditional block modes
     */
    private val STREAM_CIPHERS = setOf("ChaCha20", "RC4", "Salsa20")
    
    /**
     * Get all supported cipher algorithms as CipherAlgorithm instances
     */
    fun getAllAlgorithms(): List<CipherAlgorithm> {
        val algorithms = mutableListOf<CipherAlgorithm>()
        
        for (definition in ALGORITHM_DEFINITIONS) {
            for (keySize in definition.keySizes) {
                val supportedModes = if (STREAM_CIPHERS.contains(definition.name)) {
                    listOf("STREAM")
                } else {
                    definition.modes
                }
                
                algorithms.add(CipherAlgorithm(
                    name = "${definition.name}-$keySize",
                    algorithmName = definition.name,
                    supportedModes = supportedModes,
                    keySize = keySize,
                    blockSize = definition.blockSize
                ))
            }
        }
        
        return algorithms.sortedByDescending { 
            ALGORITHM_SECURITY_RANKING[it.algorithmName] ?: 0 
        }
    }
    
    /**
     * Get the security ranking of an algorithm
     */
    fun getSecurityRanking(algorithmName: String): Int {
        return ALGORITHM_SECURITY_RANKING[algorithmName] ?: 0
    }
    
    /**
     * Get all available providers that support cipher operations
     */
    fun getAvailableCipherProviders(): List<String> {
        return Security.getProviders()
            .filter { provider -> 
                provider.services.any { it.type == "Cipher" }
            }
            .map { it.name }
            .sorted()
    }
    
    /**
     * Check if a specific algorithm is available in the current environment
     */
    fun isAlgorithmAvailable(algorithmName: String, keySize: Int): Boolean {
        return ALGORITHM_DEFINITIONS.any { definition ->
            definition.name == algorithmName && definition.keySizes.contains(keySize)
        }
    }
    
    /**
     * Internal data class for algorithm definitions
     */
    private data class AlgorithmDefinition(
        val name: String,
        val keySizes: List<Int>,
        val modes: List<String>,
        val blockSize: Int
    )
}