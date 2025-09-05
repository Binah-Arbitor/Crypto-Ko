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
        
        // Verify the algorithms are sorted by security strength (best to worst)
        // Actual order based on security ranking and alphabetical for same rank
        val expectedOrder = listOf("AES", "ChaCha20", "ARIA", "Camellia", "Twofish", "Blowfish", "SM4", 
                                  "SEED", "IDEA", "CAST5", "DESede", "RC2", "RC4", "DES")
        
        assertEquals("Algorithms should be sorted by security strength", expectedOrder, sortedAlgorithms)
    }
    
    @Test
    fun testMostSecureAlgorithmIsAES() {
        val sortedAlgorithms = CipherAlgorithm.getBaseAlgorithmNames()
        
        assertFalse("Should have algorithms", sortedAlgorithms.isEmpty())
        assertEquals("AES should be the most secure algorithm", "AES", sortedAlgorithms.first())
    }
    
    @Test
    fun testLeastSecureAlgorithmIsDES() {
        val sortedAlgorithms = CipherAlgorithm.getBaseAlgorithmNames()
        
        assertFalse("Should have algorithms", sortedAlgorithms.isEmpty())
        assertEquals("DES should be the least secure algorithm", "DES", sortedAlgorithms.last())
    }
    
    @Test
    fun testAESSupports224BitKey() {
        val aesSizes = CipherAlgorithm.getKeySizesForAlgorithm("AES")
        
        assertTrue("AES should support 224-bit keys", aesSizes.contains(224))
        assertTrue("AES should support standard key sizes", aesSizes.containsAll(listOf(128, 192, 256)))
    }
    
    @Test
    fun testNewAlgorithmsArePresent() {
        val algorithms = CipherAlgorithm.getBaseAlgorithmNames()
        
        val expectedNewAlgorithms = listOf("ARIA", "SEED", "IDEA", "CAST5", "SM4", "RC2")
        expectedNewAlgorithms.forEach { algorithm ->
            assertTrue("Algorithm $algorithm should be present", algorithms.contains(algorithm))
        }
    }
    
    @Test
    fun testBlowfishSupportsVariableKeySizes() {
        val blowfishSizes = CipherAlgorithm.getKeySizesForAlgorithm("Blowfish")
        
        assertTrue("Blowfish should support 128-bit keys", blowfishSizes.contains(128))
        assertTrue("Blowfish should support 448-bit keys", blowfishSizes.contains(448))
        assertTrue("Blowfish should support at least 3 different key sizes", blowfishSizes.size >= 3)
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
    fun testNewAlgorithmKeySizes() {
        // Test ARIA
        val ariaSizes = CipherAlgorithm.getKeySizesForAlgorithm("ARIA")
        assertEquals("ARIA should support 3 key sizes", listOf(128, 192, 256), ariaSizes)
        
        // Test SEED
        val seedSizes = CipherAlgorithm.getKeySizesForAlgorithm("SEED")
        assertEquals("SEED should support 128-bit key", listOf(128), seedSizes)
        
        // Test SM4
        val sm4Sizes = CipherAlgorithm.getKeySizesForAlgorithm("SM4")
        assertEquals("SM4 should support 128-bit key", listOf(128), sm4Sizes)
        
        // Test RC2 variable sizes
        val rc2Sizes = CipherAlgorithm.getKeySizesForAlgorithm("RC2")
        assertTrue("RC2 should support 40-bit keys", rc2Sizes.contains(40))
        assertTrue("RC2 should support 128-bit keys", rc2Sizes.contains(128))
    }
}