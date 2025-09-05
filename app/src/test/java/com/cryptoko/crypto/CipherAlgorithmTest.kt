package com.cryptoko.crypto

import org.junit.Test
import org.junit.Assert.*

/**
 * Test class for CipherAlgorithm security-based sorting
 */
class CipherAlgorithmTest {

    @Test
    fun testAlgorithmsSortedBySecurityStrength() {
        val sortedAlgorithms = CipherAlgorithm.getBaseAlgorithmNames()
        
        // Verify that algorithms are sorted by security strength (best to worst)
        // The exact list depends on what's available, but verify ordering principles
        assertFalse("Should have algorithms", sortedAlgorithms.isEmpty())
        
        // AES should be among the top (most secure)
        val aesIndex = sortedAlgorithms.indexOf("AES")
        assertTrue("AES should be found", aesIndex >= 0)
        assertTrue("AES should be among the top 3 most secure", aesIndex < 3)
        
        // DES should be among the bottom (least secure) if present
        val desIndex = sortedAlgorithms.indexOf("DES")
        if (desIndex >= 0) {
            assertTrue("DES should be less secure than AES", desIndex > aesIndex)
            assertTrue("DES should be among the bottom 3 least secure", 
                desIndex >= sortedAlgorithms.size - 3)
        }
    }
    
    @Test
    fun testMostSecureAlgorithmIsAES() {
        val sortedAlgorithms = CipherAlgorithm.getBaseAlgorithmNames()
        
        assertFalse("Should have algorithms", sortedAlgorithms.isEmpty())
        assertEquals("AES should be the most secure algorithm", "AES", sortedAlgorithms.first())
    }
    
    @Test
    fun testLeastSecureAlgorithmCheck() {
        val sortedAlgorithms = CipherAlgorithm.getBaseAlgorithmNames()
        
        assertFalse("Should have algorithms", sortedAlgorithms.isEmpty())
        
        val lastAlgorithm = sortedAlgorithms.last()
        // The least secure algorithm should have a low security ranking
        val ranking = AlgorithmCatalog.getSecurityRanking(lastAlgorithm)
        assertTrue("Least secure algorithm should have low ranking (≤ 3)", ranking <= 3)
    }
    
    @Test
    fun testAESKeySupport() {
        val aesSizes = CipherAlgorithm.getKeySizesForAlgorithm("AES")
        
        assertFalse("AES should have key sizes", aesSizes.isEmpty())
        assertTrue("AES should support 128-bit keys", aesSizes.contains(128))
        assertTrue("AES should support 256-bit keys", aesSizes.contains(256))
        // 224-bit support depends on provider, so make it optional
        if (aesSizes.contains(224)) {
            assertTrue("If AES supports 224-bit, it should also support standard sizes", 
                aesSizes.containsAll(listOf(128, 192, 256)))
        }
    }
    
    @Test
    fun testDiscoveredAlgorithmsArePresent() {
        val algorithms = CipherAlgorithm.getBaseAlgorithmNames()
        
        // Test that certain algorithms that should definitely be available are present
        assertTrue("AES should be available", algorithms.contains("AES"))
        
        // Test any additional algorithms that are found
        val commonAlgorithms = listOf("AES", "DES", "3DES", "DESede")
        val foundCommonAlgorithms = algorithms.filter { commonAlgorithms.contains(it) }
        assertTrue("Should find at least one common algorithm", foundCommonAlgorithms.isNotEmpty())
        
        // Check for modern algorithms if available
        val modernAlgorithms = listOf("ChaCha20", "ARIA", "Camellia", "Twofish")
        val foundModernAlgorithms = algorithms.filter { modernAlgorithms.contains(it) }
        
        println("Found algorithms: $algorithms")
        println("Found modern algorithms: $foundModernAlgorithms")
    }
    
    @Test
    fun testBlowfishKeySupport() {
        val blowfishSizes = CipherAlgorithm.getKeySizesForAlgorithm("Blowfish")
        
        // Only test if Blowfish is available
        if (blowfishSizes.isNotEmpty()) {
            assertTrue("Blowfish should support at least 128-bit keys", blowfishSizes.contains(128))
            assertTrue("Blowfish should support at least 2 different key sizes", blowfishSizes.size >= 2)
            // 448-bit support is optional depending on provider
            if (blowfishSizes.contains(448)) {
                assertTrue("If Blowfish supports 448-bit, it should be the largest", 
                    blowfishSizes.max() == 448)
            }
        }
    }
    
    @Test
    fun testAllAlgorithmsHaveValidKeySizes() {
        val algorithms = CipherAlgorithm.getBaseAlgorithmNames()
        
        algorithms.forEach { algorithm ->
            val keySizes = CipherAlgorithm.getKeySizesForAlgorithm(algorithm)
            assertFalse("Algorithm $algorithm should have at least one key size", keySizes.isEmpty())
            
            keySizes.forEach { keySize ->
                assertTrue("Key size $keySize for $algorithm should be positive", keySize > 0)
                
                val modes = CipherAlgorithm.getModesForAlgorithm(algorithm, keySize)
                assertFalse("Algorithm $algorithm with key size $keySize should have at least one mode", modes.isEmpty())
            }
        }
    }
    
    @Test
    fun testAlgorithmKeySizesBasedOnDiscovery() {
        val algorithms = CipherAlgorithm.getBaseAlgorithmNames()
        
        // Test only algorithms that are actually discovered
        val testCases = mapOf(
            "AES" to listOf(128, 256), // Should definitely have these
            "DES" to listOf(56), // If DES exists, should be 56-bit
            "3DES" to listOf(168), // If 3DES exists
            "DESede" to listOf(168) // Alternative name for 3DES
        )
        
        algorithms.forEach { algorithm ->
            val keySizes = CipherAlgorithm.getKeySizesForAlgorithm(algorithm)
            assertFalse("Algorithm $algorithm should have at least one key size", keySizes.isEmpty())
            
            keySizes.forEach { keySize ->
                assertTrue("Key size $keySize for $algorithm should be positive", keySize > 0)
                
                val modes = CipherAlgorithm.getModesForAlgorithm(algorithm, keySize)
                assertFalse("Algorithm $algorithm with key size $keySize should have at least one mode", modes.isEmpty())
            }
            
            // Test expected key sizes if this algorithm matches known ones
            testCases[algorithm]?.let { expectedSizes ->
                expectedSizes.forEach { expectedSize ->
                    assertTrue("Algorithm $algorithm should support $expectedSize-bit key", 
                        keySizes.contains(expectedSize))
                }
            }
        }
    }
}