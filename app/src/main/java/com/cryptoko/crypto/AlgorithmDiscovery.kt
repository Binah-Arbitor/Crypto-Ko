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
     * Enhanced with additional OpenSSL modes
     */
    private val COMMON_MODES = listOf("CBC", "CFB", "OFB", "ECB", "GCM", "CTR", "CCM", "XTS")
    
    /**
     * Stream cipher mode (for algorithms like ChaCha20, RC4)
     * Enhanced with additional stream modes
     */
    private val STREAM_MODE = listOf("NONE", "STREAM")
    
    /**
     * Security ranking of algorithms (higher number = better security)
     * Based on cryptographic security strength and current standards
     * Enhanced with additional OpenSSL algorithms
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
        "DESede" to 4,      // Triple DES, legacy but significantly better than DES
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
     * Algorithm name mappings from provider names to standard names
     * Enhanced to support more OpenSSL algorithm variants
     */
    private val ALGORITHM_NAME_MAPPINGS = mapOf(
        "DESEDE" to "3DES",
        "TRIPLEDES" to "3DES",
        "AESEDE" to "3DES",
        "TDEA" to "3DES",
        "AES128" to "AES",
        "AES192" to "AES",
        "AES256" to "AES",
        "AES-128" to "AES",
        "AES-192" to "AES", 
        "AES-256" to "AES",
        "RIJNDAEL" to "AES",
        "CHACHA20" to "ChaCha20",
        "CHACHA20-POLY1305" to "ChaCha20",
        "SALSA20" to "Salsa20",
        "RC4-40" to "RC4",
        "RC4-128" to "RC4",
        "ARCFOUR" to "RC4",
        "GOST28147-89" to "GOST28147",
        "GOST89" to "GOST28147",
        "SKIPJACK" to "Skipjack",
        "MARS" to "Mars",
        "NULL" to "None"
    )
    
    /**
     * Common key sizes for different algorithm families
     * Expanded to support more OpenSSL algorithms
     */
    private val ALGORITHM_KEY_SIZES = mapOf(
        "AES" to listOf(128, 192, 224, 256),
        "ARIA" to listOf(128, 192, 256),
        "Camellia" to listOf(128, 192, 256),
        "ChaCha20" to listOf(256),
        "Salsa20" to listOf(128, 256),
        "Twofish" to listOf(128, 192, 256),
        "Blowfish" to listOf(32, 64, 128, 192, 256, 320, 384, 448),
        "SEED" to listOf(128),
        "IDEA" to listOf(128),
        "CAST5" to listOf(40, 64, 80, 128),
        "CAST6" to listOf(128, 160, 192, 224, 256),
        "SM4" to listOf(128),
        "RC2" to listOf(40, 64, 128, 1024),
        "RC4" to listOf(40, 56, 64, 80, 128, 256),
        "RC5" to listOf(32, 64, 128, 192, 256),
        "RC6" to listOf(128, 192, 256),
        "DES" to listOf(56),
        "3DES" to listOf(112, 168),
        "DESede" to listOf(112, 168),
        "Serpent" to listOf(128, 192, 256),
        "TEA" to listOf(128),
        "XTEA" to listOf(128),
        "GOST28147" to listOf(256),
        "Threefish" to listOf(256, 512, 1024),
        "Skipjack" to listOf(80),
        "Mars" to listOf(128, 192, 256),
        "None" to listOf(0) // Null cipher
    )
    
    /**
     * Algorithms that are stream ciphers and don't use traditional block modes
     * Expanded to include more OpenSSL stream ciphers
     */
    private val STREAM_CIPHERS = setOf("ChaCha20", "RC4", "Salsa20", "None")
    
    /**
     * Standard block sizes for algorithms (in bytes)
     * Enhanced with more OpenSSL algorithm support
     */
    private val BLOCK_SIZES = mapOf(
        "AES" to 16,
        "ARIA" to 16,
        "Camellia" to 16,
        "ChaCha20" to 1,
        "Salsa20" to 1,
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
        "Threefish" to 32,
        "Skipjack" to 8,
        "Mars" to 16,
        "None" to 1
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
            
            // Skip empty or invalid algorithm names
            if (normalizedName.isBlank() || normalizedName.length < 2) continue
            
            // Get predefined key sizes, but also try common sizes if not predefined
            val predefinedKeySizes = ALGORITHM_KEY_SIZES[normalizedName]
            val possibleKeySizes = if (predefinedKeySizes != null) {
                predefinedKeySizes
            } else {
                // For unknown algorithms, try common key sizes
                listOf(56, 64, 80, 128, 192, 256, 512).filter { keySize ->
                    // Quick test to see if this key size might work
                    testQuickKeySize(normalizedName, keySize)
                }
            }
            
            val blockSize = BLOCK_SIZES[normalizedName] ?: inferBlockSize(normalizedName)
            val isStreamCipher = STREAM_CIPHERS.contains(normalizedName) || 
                               inferStreamCipher(normalizedName)
            
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
     * Enhanced to handle more OpenSSL algorithm naming patterns
     */
    private fun extractBaseAlgorithm(transformation: String): String {
        val cleanTransformation = transformation.uppercase()
        
        // Remove common suffixes and prefixes
        val normalized = cleanTransformation
            .replace("WRAP", "")
            .replace("UNWRAP", "")
            .replace("PADDING", "")
            .replace("WITHOUTPADDING", "")
            .replace("ENCRYPTION", "")
            .replace("DECRYPTION", "")
        
        // Split by common separators and take the first meaningful part
        val parts = normalized.split("/", "_", "-", ".", "+")
        val algorithmPart = parts.firstOrNull()?.trim() ?: ""
        
        // Apply mappings for known aliases
        val mapped = ALGORITHM_NAME_MAPPINGS[algorithmPart] ?: algorithmPart
        
        // Filter out obviously non-algorithm parts
        return if (mapped.isBlank() || 
                   mapped.length < 2 || 
                   mapped.startsWith("OID") ||
                   mapped.contains("KEYSTORE") ||
                   mapped.contains("CERTIFICATE") ||
                   mapped.contains("SIGNATURE") ||
                   mapped.contains("MAC") ||
                   mapped.contains("DIGEST") ||
                   mapped.contains("HASH")) {
            ""
        } else {
            mapped
        }
    }
    
    /**
     * Quick test to see if a key size might work for an algorithm
     * More lightweight than full cipher testing
     */
    private fun testQuickKeySize(algorithm: String, keySize: Int): Boolean {
        return try {
            // Quick validation - create a key spec
            val keyBytes = ByteArray(keySize / 8)
            SecretKeySpec(keyBytes, algorithm)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Infers block size for unknown algorithms
     */
    private fun inferBlockSize(algorithm: String): Int {
        return when {
            algorithm.contains("AES") || algorithm.contains("RIJNDAEL") -> 16
            algorithm.contains("DES") -> 8
            algorithm.contains("RC") && algorithm.contains("4") -> 1  // Stream cipher
            algorithm.contains("CHACHA") || algorithm.contains("SALSA") -> 1  // Stream cipher
            else -> 16  // Default for most modern block ciphers
        }
    }
    
    /**
     * Infers if an algorithm is a stream cipher based on name patterns
     */
    private fun inferStreamCipher(algorithm: String): Boolean {
        return algorithm.contains("RC4") || 
               algorithm.contains("CHACHA") || 
               algorithm.contains("SALSA") ||
               algorithm.contains("ARCFOUR") ||
               algorithm == "None"
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
                "CCM" -> {
                    val iv = ByteArray(7)  // CCM typically uses 7-13 byte nonces
                    secureRandom.nextBytes(iv)
                    cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, iv))
                }
                "ECB", "NONE", "STREAM" -> {
                    cipher.init(Cipher.ENCRYPT_MODE, key)
                }
                "XTS" -> {
                    // XTS requires special handling - skip for now as it needs sector numbers
                    return false
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
     * Enhanced to support more OpenSSL modes
     */
    private fun buildTransformation(algorithm: String, mode: String): String {
        return when (mode) {
            "GCM", "CCM" -> "$algorithm/$mode/NoPadding"
            "CTR", "OFB", "CFB" -> "$algorithm/$mode/NoPadding"
            "XTS" -> "$algorithm/$mode/NoPadding"
            "ECB" -> "$algorithm/$mode/PKCS5Padding"
            "CBC" -> "$algorithm/$mode/PKCS5Padding"
            "NONE", "STREAM" -> algorithm  // Stream ciphers don't use modes/padding
            else -> "$algorithm/$mode/NoPadding"  // Default for unknown modes
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