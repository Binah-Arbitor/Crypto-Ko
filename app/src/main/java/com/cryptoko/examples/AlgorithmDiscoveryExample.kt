package com.cryptoko.examples

import com.cryptoko.crypto.CipherAlgorithm
import com.cryptoko.crypto.AlgorithmDiscovery

/**
 * Example demonstrating the dynamic algorithm discovery capabilities
 * of Crypto-Ko. This shows how to programmatically access algorithm
 * information and make informed security choices.
 */
object AlgorithmDiscoveryExample {
    
    /**
     * Demonstrates basic algorithm discovery and information retrieval
     */
    fun basicAlgorithmDiscovery() {
        println("=== Crypto-Ko Dynamic Algorithm Discovery Example ===\n")
        
        // Get all available algorithms
        val algorithms = CipherAlgorithm.ALL_ALGORITHMS
        println("Total algorithms discovered: ${algorithms.size}")
        
        // Get unique algorithm families
        val algorithmFamilies = CipherAlgorithm.getBaseAlgorithmNames()
        println("Algorithm families: ${algorithmFamilies.size}")
        println("Available: ${algorithmFamilies.joinToString(", ")}\n")
        
        // Show security rankings
        println("=== Security Rankings ===")
        algorithmFamilies.forEach { algorithm ->
            val ranking = AlgorithmDiscovery.getSecurityRanking(algorithm)
            val level = when {
                ranking >= 8 -> "Excellent"
                ranking >= 6 -> "Good"  
                ranking >= 4 -> "Fair"
                else -> "Weak"
            }
            println("$algorithm: $ranking/10 ($level)")
        }
        println()
    }
    
    /**
     * Shows how to find the best algorithms for different use cases
     */
    fun algorithmRecommendations() {
        println("=== Algorithm Recommendations ===")
        
        // Get high-security algorithms
        val highSecurityAlgorithms = CipherAlgorithm.getBaseAlgorithmNames()
            .filter { AlgorithmDiscovery.getSecurityRanking(it) >= 8 }
        
        println("🔒 High Security Algorithms (8+/10):")
        highSecurityAlgorithms.forEach { algorithm ->
            val keySizes = CipherAlgorithm.getKeySizesForAlgorithm(algorithm)
            val largestKeySize = keySizes.maxOrNull() ?: 0
            val modes = CipherAlgorithm.getModesForAlgorithm(algorithm, largestKeySize)
            println("  • $algorithm-$largestKeySize: ${modes.joinToString(", ")}")
        }
        
        // Show algorithms to avoid
        val weakAlgorithms = CipherAlgorithm.getBaseAlgorithmNames()
            .filter { AlgorithmDiscovery.getSecurityRanking(it) <= 3 }
        
        if (weakAlgorithms.isNotEmpty()) {
            println("\n⚠️ Weak Algorithms (≤3/10) - Avoid for new applications:")
            weakAlgorithms.forEach { algorithm ->
                val ranking = AlgorithmDiscovery.getSecurityRanking(algorithm)
                println("  • $algorithm ($ranking/10)")
            }
        }
        println()
    }
    
    /**
     * Demonstrates provider information and capabilities
     */
    fun providerInformation() {
        println("=== Cryptographic Provider Information ===")
        
        val providers = CipherAlgorithm.getAvailableCipherProviders()
        println("Available providers: ${providers.size}")
        providers.forEach { provider ->
            println("  • $provider")
        }
        println()
        
        // Show algorithm info
        val algorithmInfo = CipherAlgorithm.getAlgorithmInfo()
        println("Algorithm Statistics:")
        println("  • Total algorithms: ${algorithmInfo["totalAlgorithms"]}")
        println("  • Unique families: ${algorithmInfo["uniqueBaseAlgorithms"]}")
        
        @Suppress("UNCHECKED_CAST")
        val algorithmsByFamily = algorithmInfo["algorithmsByFamily"] as? Map<String, List<String>>
        if (algorithmsByFamily != null) {
            println("\nAlgorithm Family Breakdown:")
            algorithmsByFamily.forEach { (family, variants) ->
                println("  • $family: ${variants.joinToString(", ")}")
            }
        }
        println()
    }
    
    /**
     * Example of checking specific algorithm availability
     */
    fun algorithmAvailabilityCheck() {
        println("=== Algorithm Availability Check ===")
        
        val testCases = listOf(
            "AES" to 256,
            "ChaCha20" to 256,
            "Twofish" to 256,
            "ARIA" to 256,
            "DES" to 56,
            "BlowFish" to 128
        )
        
        testCases.forEach { (algorithm, keySize) ->
            val available = CipherAlgorithm.isAlgorithmAvailable(algorithm, keySize)
            val status = if (available) "✅ Available" else "❌ Not Available"
            val ranking = AlgorithmDiscovery.getSecurityRanking(algorithm)
            println("$algorithm-$keySize: $status (Security: $ranking/10)")
        }
        println()
    }
    
    /**
     * Shows multithreading capabilities for different modes
     */
    fun multithreadingCapabilities() {
        println("=== Multithreading Support Analysis ===")
        
        val aesAlgorithm = CipherAlgorithm.createFromComponents("AES", 256)
        if (aesAlgorithm != null) {
            println("AES-256 Mode Analysis:")
            aesAlgorithm.supportedModes.forEach { mode ->
                val supportsMultithreading = when (mode) {
                    "ECB", "CTR", "OFB", "GCM" -> true
                    else -> false
                }
                val icon = if (supportsMultithreading) "🚀" else "🐌"
                val desc = if (supportsMultithreading) "Multithreaded" else "Single-threaded"
                println("  • $mode: $icon $desc")
            }
        }
        println()
    }
    
    /**
     * Complete example showing all features
     */
    fun completeExample() {
        basicAlgorithmDiscovery()
        algorithmRecommendations()
        providerInformation() 
        algorithmAvailabilityCheck()
        multithreadingCapabilities()
        
        println("=== Usage Summary ===")
        println("For maximum security, use: AES-256-GCM or ChaCha20")
        println("For best performance: AES-128-CTR with multithreading")
        println("For legacy compatibility: AES-128-CBC")
        println("Always check algorithm availability with isAlgorithmAvailable()")
        println("\nUse the Crypto-Ko app's Algorithm Information card for real-time discovery!")
    }
}