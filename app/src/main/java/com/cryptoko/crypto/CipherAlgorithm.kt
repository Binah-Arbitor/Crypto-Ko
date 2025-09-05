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
            // AES algorithms
            CipherAlgorithm("AES-128", "AES", listOf("CBC", "CFB", "OFB", "ECB", "GCM", "CTR"), 128),
            CipherAlgorithm("AES-192", "AES", listOf("CBC", "CFB", "OFB", "ECB", "GCM", "CTR"), 192),
            CipherAlgorithm("AES-256", "AES", listOf("CBC", "CFB", "OFB", "ECB", "GCM", "CTR"), 256),
            
            // DES algorithms
            CipherAlgorithm("DES", "DES", listOf("CBC", "CFB", "OFB", "ECB"), 56, 8),
            CipherAlgorithm("3DES", "DESede", listOf("CBC", "CFB", "OFB", "ECB"), 168, 8),
            
            // Blowfish
            CipherAlgorithm("Blowfish", "Blowfish", listOf("CBC", "CFB", "OFB", "ECB"), 128, 8),
            
            // Twofish
            CipherAlgorithm("Twofish", "Twofish", listOf("CBC", "CFB", "OFB", "ECB"), 256),
            
            // RC4 (stream cipher)
            CipherAlgorithm("RC4", "RC4", listOf("NONE"), 128, 1),
            
            // ChaCha20
            CipherAlgorithm("ChaCha20", "ChaCha20", listOf("NONE"), 256, 1),
            
            // Camellia
            CipherAlgorithm("Camellia-128", "Camellia", listOf("CBC", "CFB", "OFB", "ECB"), 128),
            CipherAlgorithm("Camellia-192", "Camellia", listOf("CBC", "CFB", "OFB", "ECB"), 192),
            CipherAlgorithm("Camellia-256", "Camellia", listOf("CBC", "CFB", "OFB", "ECB"), 256)
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
            "Camellia" to 8,    // Similar security level to AES, approved by NESSIE
            "Twofish" to 7,     // AES finalist, strong security design
            "Blowfish" to 6,    // Older but still reasonably secure for most applications
            "DESede" to 4,      // Triple DES, legacy but significantly better than DES
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