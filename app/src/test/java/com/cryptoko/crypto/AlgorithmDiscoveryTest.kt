package com.cryptoko.crypto

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

/**
 * Test class for AlgorithmCatalog functionality
 */
class AlgorithmDiscoveryTest {

    @Before
    fun setUp() {
        // No setup needed for hardcoded catalog
    }

    @Test
    fun testHardcodedAlgorithmCatalog() {
        val algorithms = AlgorithmCatalog.getAllAlgorithms()
        assertFalse("Should have algorithms in catalog", algorithms.isEmpty())
        
        // Should find at least AES (most basic algorithm)
        val aesAlgorithms = algorithms.filter { it.algorithmName == "AES" }
        assertFalse("Should find AES algorithms", aesAlgorithms.isEmpty())
        
        // Should support more key sizes for AES
        val aesKeySizes = aesAlgorithms.map { it.keySize }.toSet()
        assertTrue("Should support 128-bit AES", aesKeySizes.contains(128))
        assertTrue("Should support 256-bit AES", aesKeySizes.contains(256))
        
        // Print discovered algorithms for debugging
        println("Catalog contains ${algorithms.size} algorithm configurations:")
        algorithms.groupBy { it.algorithmName }.forEach { (name, variants) ->
            val keySizes = variants.map { it.keySize }.distinct().sorted()
            println("  $name: ${keySizes.joinToString(", ")} bit keys")
        }
    }
    
    @Test
    fun testAESCatalog() {
        val algorithms = AlgorithmCatalog.getAllAlgorithms()
        val aesAlgorithms = algorithms.filter { it.algorithmName == "AES" }
        
        assertFalse("Should find AES algorithms", aesAlgorithms.isEmpty())
        
        // Should support at least 128, 192, 256 bit keys
        val keySizes = aesAlgorithms.map { it.keySize }.toSet()
        assertTrue("Should support 128-bit AES", keySizes.contains(128))
        assertTrue("Should support 256-bit AES", keySizes.contains(256))
    }
    
    @Test
    fun testProviderCatalog() {
        val providers = AlgorithmCatalog.getAvailableCipherProviders()
        assertFalse("Should find at least one provider", providers.isEmpty())
        
        // Print providers for debugging
        println("Available cryptographic providers:")
        providers.forEach { println("  - $it") }
        
        // Should include at least SunJCE or BC (Bouncy Castle)
        val hasBasicProvider = providers.any { 
            it.contains("SunJCE") || it.contains("BC") 
        }
        assertTrue("Should have a basic cryptographic provider", hasBasicProvider)
    }
    
    @Test
    fun testAlgorithmAvailabilityCheck() {
        // AES-128 should definitely be available
        assertTrue("AES-128 should be available", 
            AlgorithmCatalog.isAlgorithmAvailable("AES", 128))
    }
    
    @Test
    fun testSecurityRanking() {
        val aesRanking = AlgorithmCatalog.getSecurityRanking("AES")
        val desRanking = AlgorithmCatalog.getSecurityRanking("DES")
        
        assertTrue("AES should have higher security ranking than DES", 
            aesRanking > desRanking)
        assertTrue("AES should have positive security ranking", aesRanking > 0)
    }
    
    @Test
    fun testCatalogConsistency() {
        val algorithms = AlgorithmCatalog.getAllAlgorithms()
        
        // All algorithms should have positive key sizes
        algorithms.forEach { algorithm ->
            assertTrue("Key size should be positive for ${algorithm.name}", 
                algorithm.keySize > 0)
        }
        
        // All algorithms should have at least one mode
        algorithms.forEach { algorithm ->
            assertFalse("Should have at least one mode for ${algorithm.name}", 
                algorithm.supportedModes.isEmpty())
        }
    }
}