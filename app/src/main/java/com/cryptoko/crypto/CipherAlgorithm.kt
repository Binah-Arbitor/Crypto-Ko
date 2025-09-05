package com.cryptoko.crypto

/**
 * Represents a symmetric encryption algorithm with its supported modes
 */
data class CipherAlgorithm(
    val name: String,
    val algorithmName: String,
    val supportedModes: List<String>,
    val keySize: Int,
    val blockSize: Int = 16
) {
    companion object {
        /**
         * All OpenSSL symmetric encryption algorithms with their supported modes
         */
        val ALL_ALGORITHMS = listOf(
            // AES/Rijndael algorithms - OpenSSL supports various key sizes
            CipherAlgorithm("AES-128", "AES", listOf("CBC", "CFB", "OFB", "ECB", "GCM", "CTR"), 128),
            CipherAlgorithm("AES-192", "AES", listOf("CBC", "CFB", "OFB", "ECB", "GCM", "CTR"), 192),
            CipherAlgorithm("AES-224", "AES", listOf("CBC", "CFB", "OFB", "ECB", "GCM", "CTR"), 224),
            CipherAlgorithm("AES-256", "AES", listOf("CBC", "CFB", "OFB", "ECB", "GCM", "CTR"), 256),
            
            // ARIA algorithms - Korean standard cipher
            CipherAlgorithm("ARIA-128", "ARIA", listOf("CBC", "CFB", "OFB", "ECB", "GCM", "CTR"), 128),
            CipherAlgorithm("ARIA-192", "ARIA", listOf("CBC", "CFB", "OFB", "ECB", "GCM", "CTR"), 192),
            CipherAlgorithm("ARIA-256", "ARIA", listOf("CBC", "CFB", "OFB", "ECB", "GCM", "CTR"), 256),
            
            // Camellia algorithms - Japanese standard cipher
            CipherAlgorithm("Camellia-128", "Camellia", listOf("CBC", "CFB", "OFB", "ECB", "GCM", "CTR"), 128),
            CipherAlgorithm("Camellia-192", "Camellia", listOf("CBC", "CFB", "OFB", "ECB", "GCM", "CTR"), 192),
            CipherAlgorithm("Camellia-256", "Camellia", listOf("CBC", "CFB", "OFB", "ECB", "GCM", "CTR"), 256),
            
            // ChaCha20 - Modern stream cipher
            CipherAlgorithm("ChaCha20", "ChaCha20", listOf("NONE"), 256, 1),
            
            // Twofish - AES finalist
            CipherAlgorithm("Twofish-128", "Twofish", listOf("CBC", "CFB", "OFB", "ECB"), 128),
            CipherAlgorithm("Twofish-192", "Twofish", listOf("CBC", "CFB", "OFB", "ECB"), 192),
            CipherAlgorithm("Twofish-256", "Twofish", listOf("CBC", "CFB", "OFB", "ECB"), 256),
            
            // Blowfish - Variable key size (32-448 bits), using common sizes
            CipherAlgorithm("Blowfish-128", "Blowfish", listOf("CBC", "CFB", "OFB", "ECB"), 128, 8),
            CipherAlgorithm("Blowfish-192", "Blowfish", listOf("CBC", "CFB", "OFB", "ECB"), 192, 8),
            CipherAlgorithm("Blowfish-256", "Blowfish", listOf("CBC", "CFB", "OFB", "ECB"), 256, 8),
            CipherAlgorithm("Blowfish-448", "Blowfish", listOf("CBC", "CFB", "OFB", "ECB"), 448, 8),
            
            // SEED - Korean standard cipher
            CipherAlgorithm("SEED", "SEED", listOf("CBC", "CFB", "OFB", "ECB"), 128),
            
            // IDEA - International Data Encryption Algorithm
            CipherAlgorithm("IDEA", "IDEA", listOf("CBC", "CFB", "OFB", "ECB"), 128, 8),
            
            // CAST algorithms
            CipherAlgorithm("CAST5", "CAST5", listOf("CBC", "CFB", "OFB", "ECB"), 128, 8),
            
            // SM4 - Chinese national standard
            CipherAlgorithm("SM4", "SM4", listOf("CBC", "CFB", "OFB", "ECB"), 128),
            
            // RC2 - Variable key size
            CipherAlgorithm("RC2-40", "RC2", listOf("CBC", "CFB", "OFB", "ECB"), 40, 8),
            CipherAlgorithm("RC2-64", "RC2", listOf("CBC", "CFB", "OFB", "ECB"), 64, 8),
            CipherAlgorithm("RC2-128", "RC2", listOf("CBC", "CFB", "OFB", "ECB"), 128, 8),
            
            // DES algorithms
            CipherAlgorithm("DES", "DES", listOf("CBC", "CFB", "OFB", "ECB"), 56, 8),
            CipherAlgorithm("3DES", "DESede", listOf("CBC", "CFB", "OFB", "ECB"), 168, 8),
            
            // RC4 (stream cipher) - Variable key size
            CipherAlgorithm("RC4-40", "RC4", listOf("NONE"), 40, 1),
            CipherAlgorithm("RC4-128", "RC4", listOf("NONE"), 128, 1),
            CipherAlgorithm("RC4-256", "RC4", listOf("NONE"), 256, 1)
        )
        
        fun getAlgorithmByName(name: String): CipherAlgorithm? {
            return ALL_ALGORITHMS.find { it.name == name }
        }
        
        fun getAlgorithmNames(): List<String> {
            return ALL_ALGORITHMS.map { it.name }
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
            "SM4" to 6,         // Chinese national standard, relatively secure
            "Blowfish" to 6,    // Variable key, reasonably secure for most applications
            "SEED" to 5,        // Korean standard, decent security
            "IDEA" to 5,        // Good security design, but aging
            "CAST5" to 4,       // Reasonable security for legacy applications
            "DESede" to 4,      // Triple DES, legacy but significantly better than DES
            "RC2" to 3,         // Weak cipher, should be avoided
            "RC4" to 3,         // Deprecated due to known vulnerabilities
            "DES" to 2          // Broken encryption, should not be used
        )

        /**
         * Get unique base algorithm names (AES, DES, etc.) for separate selection
         * Sorted by security strength (best to worst)
         */
        fun getBaseAlgorithmNames(): List<String> {
            return ALL_ALGORITHMS.map { it.algorithmName }.distinct()
                .sortedByDescending { ALGORITHM_SECURITY_RANKING[it] ?: 0 }
        }

        /**
         * Get available key sizes for a specific base algorithm
         */
        fun getKeySizesForAlgorithm(algorithmName: String): List<Int> {
            return ALL_ALGORITHMS
                .filter { it.algorithmName == algorithmName }
                .map { it.keySize }
                .distinct()
                .sorted()
        }

        /**
         * Get supported modes for a specific algorithm and key size combination
         */
        fun getModesForAlgorithm(algorithmName: String, keySize: Int): List<String> {
            return ALL_ALGORITHMS
                .filter { it.algorithmName == algorithmName && it.keySize == keySize }
                .flatMap { it.supportedModes }
                .distinct()
        }

        /**
         * Create a CipherAlgorithm instance from separate components
         */
        fun createFromComponents(algorithmName: String, keySize: Int): CipherAlgorithm? {
            return ALL_ALGORITHMS.find { it.algorithmName == algorithmName && it.keySize == keySize }
        }
    }
}