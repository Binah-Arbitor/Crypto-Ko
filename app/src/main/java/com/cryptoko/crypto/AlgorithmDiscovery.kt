package com.cryptoko.crypto

import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Provider
import java.security.Security
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.GCMParameterSpec
import java.security.SecureRandom

/**
 * Dynamically discovers available cryptographic algorithms and their capabilities
 * from the installed security providers, primarily Bouncy Castle.
 */
object AlgorithmDiscovery {
    
    private val secureRandom = SecureRandom()
    
    /**
     * Cache of discovered algorithms to avoid repeated expensive discovery operations
     */
    private var discoveredAlgorithms: List<CipherAlgorithm>? = null
    
    /**
     * Common block cipher modes supported by most algorithms
     */
    private val COMMON_MODES = listOf("CBC", "CFB", "OFB", "ECB", "GCM", "CTR")
    
    /**
     * Stream cipher mode (for algorithms like ChaCha20, RC4)
     */
    private val STREAM_MODE = listOf("NONE")
    
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
        "SM4" to 6,         // Chinese national standard, relatively secure
        "Blowfish" to 6,    // Variable key, reasonably secure for most applications
        "SEED" to 5,        // Korean standard, decent security
        "IDEA" to 5,        // Good security design, but aging
        "CAST5" to 4,       // Reasonable security for legacy applications
        "CAST6" to 4,       // CAST family cipher
        "Serpent" to 7,     // AES finalist, excellent security
        "DESede" to 4,      // Triple DES, legacy but significantly better than DES
        "RC2" to 3,         // Weak cipher, should be avoided
        "RC4" to 3,         // Deprecated due to known vulnerabilities
        "RC5" to 4,         // Variable parameters, decent security
        "RC6" to 4,         // AES finalist candidate
        "TEA" to 3,         // Simple but weak
        "XTEA" to 4,        // Improved TEA
        "GOST28147" to 5,   // Russian standard
        "Threefish" to 6,   // Part of Skein hash function family
        "DES" to 2          // Broken encryption, should not be used
    )
    
    /**
     * Algorithm name mappings from provider names to standard names
     */
    private val ALGORITHM_NAME_MAPPINGS = mapOf(
        "DESEDE" to "3DES",
        "TRIPLEDES" to "3DES"
    )
    
    /**
     * Common key sizes for different algorithm families
     */
    private val ALGORITHM_KEY_SIZES = mapOf(
        "AES" to listOf(128, 192, 224, 256),
        "ARIA" to listOf(128, 192, 256),
        "Camellia" to listOf(128, 192, 256),
        "ChaCha20" to listOf(256),
        "Twofish" to listOf(128, 192, 256),
        "Blowfish" to listOf(128, 192, 256, 448),
        "SEED" to listOf(128),
        "IDEA" to listOf(128),
        "CAST5" to listOf(128),
        "CAST6" to listOf(128, 160, 192, 224, 256),
        "SM4" to listOf(128),
        "RC2" to listOf(40, 64, 128),
        "RC4" to listOf(40, 128, 256),
        "RC5" to listOf(128, 192, 256),
        "RC6" to listOf(128, 192, 256),
        "DES" to listOf(56),
        "3DES" to listOf(168),
        "DESede" to listOf(168),
        "Serpent" to listOf(128, 192, 256),
        "TEA" to listOf(128),
        "XTEA" to listOf(128),
        "GOST28147" to listOf(256),
        "Threefish" to listOf(256, 512, 1024)
    )
    
    /**
     * Algorithms that are stream ciphers and don't use traditional block modes
     */
    private val STREAM_CIPHERS = setOf("ChaCha20", "RC4", "SALSA20")
    
    /**
     * Standard block sizes for algorithms (in bytes)
     */
    private val BLOCK_SIZES = mapOf(
        "AES" to 16,
        "ARIA" to 16,
        "Camellia" to 16,
        "ChaCha20" to 1,
        "Twofish" to 16,
        "Blowfish" to 8,
        "SEED" to 16,
        "IDEA" to 8,
        "CAST5" to 8,
        "CAST6" to 16,
        "SM4" to 16,
        "RC2" to 8,
        "RC4" to 1,
        "RC5" to 8,
        "RC6" to 16,
        "DES" to 8,
        "3DES" to 8,
        "DESede" to 8,
        "Serpent" to 16,
        "TEA" to 8,
        "XTEA" to 8,
        "GOST28147" to 8,
        "Threefish" to 32
    )
    
    /**
     * Discovers all available cipher algorithms and their supported configurations
     */
    fun discoverAlgorithms(): List<CipherAlgorithm> {
        if (discoveredAlgorithms != null) {
            return discoveredAlgorithms!!
        }
        
        // Ensure Bouncy Castle is registered
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(BouncyCastleProvider())
        }
        
        val algorithms = mutableListOf<CipherAlgorithm>()
        
        // Get all cipher services from all providers
        val providers = Security.getProviders()
        val cipherServices = providers.flatMap { provider ->
            provider.services.filter { it.type == "Cipher" }
        }
        
        // Extract base algorithm names
        val baseAlgorithms = cipherServices.map { service ->
            extractBaseAlgorithm(service.algorithm)
        }.distinct().filter { it.isNotBlank() }
        
        // For each base algorithm, test what key sizes and modes work
        for (baseAlgorithm in baseAlgorithms) {
            val normalizedName = ALGORITHM_NAME_MAPPINGS[baseAlgorithm.uppercase()] ?: baseAlgorithm
            val possibleKeySizes = ALGORITHM_KEY_SIZES[normalizedName] ?: listOf(128, 192, 256)
            val blockSize = BLOCK_SIZES[normalizedName] ?: 16
            val isStreamCipher = STREAM_CIPHERS.contains(normalizedName)
            
            for (keySize in possibleKeySizes) {
                val supportedModes = if (isStreamCipher) {
                    STREAM_MODE
                } else {
                    findSupportedModes(normalizedName, keySize)
                }
                
                if (supportedModes.isNotEmpty()) {
                    algorithms.add(CipherAlgorithm(
                        name = "$normalizedName-$keySize",
                        algorithmName = normalizedName,
                        supportedModes = supportedModes,
                        keySize = keySize,
                        blockSize = blockSize
                    ))
                }
            }
        }
        
        discoveredAlgorithms = algorithms.sortedByDescending { 
            ALGORITHM_SECURITY_RANKING[it.algorithmName] ?: 0 
        }
        
        return discoveredAlgorithms!!
    }
    
    /**
     * Extracts the base algorithm name from a cipher transformation string
     */
    private fun extractBaseAlgorithm(transformation: String): String {
        return transformation.split("/", "_", "-").first().uppercase()
    }
    
    /**
     * Tests which modes are actually supported for a given algorithm and key size
     */
    private fun findSupportedModes(algorithm: String, keySize: Int): List<String> {
        val supportedModes = mutableListOf<String>()
        
        for (mode in COMMON_MODES) {
            if (testCipherSupport(algorithm, mode, keySize)) {
                supportedModes.add(mode)
            }
        }
        
        return supportedModes
    }
    
    /**
     * Tests if a specific cipher configuration is supported by trying to create it
     */
    private fun testCipherSupport(algorithm: String, mode: String, keySize: Int): Boolean {
        return try {
            val transformation = buildTransformation(algorithm, mode)
            val cipher = Cipher.getInstance(transformation)
            
            // Create a test key of the appropriate size
            val keyBytes = ByteArray(keySize / 8)
            secureRandom.nextBytes(keyBytes)
            val key = SecretKeySpec(keyBytes, algorithm)
            
            // Try to initialize the cipher
            when (mode) {
                "GCM" -> {
                    val iv = ByteArray(12)
                    secureRandom.nextBytes(iv)
                    cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, iv))
                }
                "ECB" -> {
                    cipher.init(Cipher.ENCRYPT_MODE, key)
                }
                else -> {
                    val blockSize = BLOCK_SIZES[algorithm] ?: 16
                    val iv = ByteArray(blockSize)
                    secureRandom.nextBytes(iv)
                    cipher.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(iv))
                }
            }
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Builds a cipher transformation string for testing
     */
    private fun buildTransformation(algorithm: String, mode: String): String {
        return when (mode) {
            "GCM" -> "$algorithm/$mode/NoPadding"
            "CTR" -> "$algorithm/$mode/NoPadding"
            "OFB", "CFB" -> "$algorithm/$mode/NoPadding"
            "ECB" -> "$algorithm/$mode/PKCS5Padding"
            "CBC" -> "$algorithm/$mode/PKCS5Padding"
            else -> algorithm
        }
    }
    
    /**
     * Gets the security ranking of an algorithm
     */
    fun getSecurityRanking(algorithmName: String): Int {
        return ALGORITHM_SECURITY_RANKING[algorithmName] ?: 0
    }
    
    /**
     * Forces a re-discovery of algorithms (clears cache)
     */
    fun refreshAlgorithms() {
        discoveredAlgorithms = null
    }
    
    /**
     * Gets all available providers that support cipher operations
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
     * Checks if a specific algorithm is available in the current environment
     */
    fun isAlgorithmAvailable(algorithmName: String, keySize: Int): Boolean {
        return discoverAlgorithms().any { 
            it.algorithmName == algorithmName && it.keySize == keySize 
        }
    }
}