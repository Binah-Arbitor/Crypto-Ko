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
        val expectedOrder = listOf("AES", "ChaCha20", "Camellia", "Twofish", "Blowfish", "DESede", "RC4", "DES")
        
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
}