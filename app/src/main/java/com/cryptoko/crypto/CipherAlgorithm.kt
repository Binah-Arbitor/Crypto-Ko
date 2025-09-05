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
         * All supported symmetric encryption algorithms from the hardcoded catalog
         * This replaces the complex dynamic discovery with a reliable, modular approach
         */
        @JvmStatic
        val ALL_ALGORITHMS: List<CipherAlgorithm>
            get() = AlgorithmCatalog.getAllAlgorithms()
        
        fun getAlgorithmByName(name: String): CipherAlgorithm? {
            return ALL_ALGORITHMS.find { it.name == name }
        }
        
        fun getAlgorithmNames(): List<String> {
            return ALL_ALGORITHMS.map { it.name }
        }

        /**
         * Get unique base algorithm names (AES, DES, etc.) for separate selection
         * Sorted by security strength (best to worst)
         */
        fun getBaseAlgorithmNames(): List<String> {
            return ALL_ALGORITHMS.map { it.algorithmName }.distinct()
                .sortedByDescending { AlgorithmCatalog.getSecurityRanking(it) }
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

        /**
         * Get all available cipher providers in the current environment
         */
        fun getAvailableCipherProviders(): List<String> {
            return AlgorithmCatalog.getAvailableCipherProviders()
        }

        /**
         * Check if a specific algorithm configuration is available
         */
        fun isAlgorithmAvailable(algorithmName: String, keySize: Int): Boolean {
            return AlgorithmCatalog.isAlgorithmAvailable(algorithmName, keySize)
        }

        /**
         * Refresh the algorithm list (no-op for hardcoded catalog, kept for compatibility)
         */
        fun refreshAlgorithms() {
            // No-op for hardcoded catalog, but kept for UI compatibility
        }

        /**
         * Get detailed information about algorithm availability
         */
        fun getAlgorithmInfo(): Map<String, Any> {
            val algorithms = ALL_ALGORITHMS
            return mapOf(
                "totalAlgorithms" to algorithms.size,
                "uniqueBaseAlgorithms" to algorithms.map { it.algorithmName }.distinct().size,
                "availableProviders" to getAvailableCipherProviders(),
                "algorithmsByFamily" to algorithms.groupBy { it.algorithmName }
                    .mapValues { (_, algos) -> algos.map { "${it.keySize}-bit" } }
            )
        }
    }
}