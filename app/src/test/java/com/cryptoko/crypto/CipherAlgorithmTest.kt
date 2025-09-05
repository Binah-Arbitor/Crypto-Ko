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
}