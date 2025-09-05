package com.cryptoko.crypto

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

/**
 * Test class for AlgorithmDiscovery functionality
 */
class AlgorithmDiscoveryTest {

    @Before
    fun setUp() {
        // Ensure fresh discovery for each test
        AlgorithmDiscovery.refreshAlgorithms()
    }

    @Test
    fun testEnhancedAlgorithmDiscovery() {
        val algorithms = AlgorithmDiscovery.discoverAlgorithms()
        assertFalse("Should discover at least some algorithms", algorithms.isEmpty())
        
        // Should find at least AES (most basic algorithm)
        val aesAlgorithms = algorithms.filter { it.algorithmName == "AES" }
        assertFalse("Should find AES algorithms", aesAlgorithms.isEmpty())
        
        // Should support more key sizes for AES
        val aesKeySizes = aesAlgorithms.map { it.keySize }.toSet()
        assertTrue("Should support 128-bit AES", aesKeySizes.contains(128))
        assertTrue("Should support 256-bit AES", aesKeySizes.contains(256))
        
        // Print discovered algorithms for debugging
        println("Discovered ${algorithms.size} algorithm configurations:")
        algorithms.groupBy { it.algorithmName }.forEach { (name, variants) ->
            val keySizes = variants.map { it.keySize }.distinct().sorted()
            println("  $name: ${keySizes.joinToString(", ")} bit keys")
        }
    }
    
    @Test
    fun testAESDiscovery() {
        val algorithms = AlgorithmDiscovery.discoverAlgorithms()
        val aesAlgorithms = algorithms.filter { it.algorithmName == "AES" }
        
        assertFalse("Should find AES algorithms", aesAlgorithms.isEmpty())
        
        // Should support at least 128, 192, 256 bit keys
        val keySizes = aesAlgorithms.map { it.keySize }.toSet()
        assertTrue("Should support 128-bit AES", keySizes.contains(128))
        assertTrue("Should support 256-bit AES", keySizes.contains(256))
    }
    
    @Test
    fun testProviderDiscovery() {
        val providers = AlgorithmDiscovery.getAvailableCipherProviders()
        assertFalse("Should find at least one provider", providers.isEmpty())
        
        // Should include at least SunJCE or BC (Bouncy Castle)
        val hasBasicProvider = providers.any { 
            it.contains("SunJCE") || it.contains("BC") || it.contains("AndroidOpenSSL") 
        }
        assertTrue("Should have a basic cryptographic provider", hasBasicProvider)
    }
    
    @Test
    fun testAlgorithmAvailabilityCheck() {
        // AES-128 should definitely be available
        assertTrue("AES-128 should be available", 
            AlgorithmDiscovery.isAlgorithmAvailable("AES", 128))
    }
    
    @Test
    fun testSecurityRanking() {
        val aesRanking = AlgorithmDiscovery.getSecurityRanking("AES")
        val desRanking = AlgorithmDiscovery.getSecurityRanking("DES")
        
        assertTrue("AES should have higher security ranking than DES", 
            aesRanking > desRanking)
        assertTrue("AES should have positive security ranking", aesRanking > 0)
    }
    
    @Test
    fun testDiscoveryCache() {
        // First call should populate cache
        val algorithms1 = AlgorithmDiscovery.discoverAlgorithms()
        
        // Second call should return cached result
        val algorithms2 = AlgorithmDiscovery.discoverAlgorithms()
        
        assertEquals("Cached result should be identical", algorithms1, algorithms2)
    }
}