package com.cryptoko.crypto

import org.junit.Test
import org.junit.Assert.*

/**
 * Test class for enhanced algorithm support beyond OpenSSL capabilities
 */
class EnhancedAlgorithmTest {
    
    @Test
    fun testEnhancedAlgorithmCatalog() {
        val algorithms = AlgorithmCatalog.getAllAlgorithms()
        
        // Print enhanced algorithm set for verification
        println("Enhanced algorithm catalog contains ${algorithms.size} algorithm configurations:")
        algorithms.groupBy { it.algorithmName }.toSortedMap().forEach { (name, variants) ->
            val keySizes = variants.map { it.keySize }.distinct().sorted()
            val ranking = AlgorithmCatalog.getSecurityRanking(name)
            println("  $name (security: $ranking/10): ${keySizes.joinToString(", ")} bit keys")
        }
        
        // Verify presence of key enhanced algorithms that are actually available
        val algorithmNames = algorithms.map { it.algorithmName }.toSet()
        
        // Core enhanced algorithms that should be available
        val coreEnhancements = listOf("XSalsa20", "HC128", "HC256", "NOEKEON", "SHACAL2")
        coreEnhancements.forEach { algorithm ->
            assertTrue("Should include enhanced algorithm $algorithm", 
                algorithmNames.contains(algorithm))
        }
        
        // International standards
        val internationalStandards = listOf("ZUC128", "ZUC256", "DSTU7624", "GOST3412")
        internationalStandards.forEach { algorithm ->
            assertTrue("Should include international standard $algorithm", 
                algorithmNames.contains(algorithm))
        }
        
        // Modern stream ciphers  
        assertTrue("Should include XSalsa20", algorithmNames.contains("XSalsa20"))
        assertTrue("Should include HC256", algorithmNames.contains("HC256"))
        assertTrue("Should include ZUC256", algorithmNames.contains("ZUC256"))
        
        // Enhanced block ciphers
        assertTrue("Should include NOEKEON", algorithmNames.contains("NOEKEON"))
        assertTrue("Should include SHACAL2", algorithmNames.contains("SHACAL2"))
        assertTrue("Should include DSTU7624", algorithmNames.contains("DSTU7624"))
        
        // Verify total algorithm count has increased significantly beyond basic OpenSSL
        assertTrue("Should have at least 35 different algorithm families", algorithmNames.size >= 35)
    }
    
    @Test
    fun testStreamCipherModes() {
        val algorithms = AlgorithmCatalog.getAllAlgorithms()
        
        // Test that stream ciphers have STREAM mode
        val streamCiphers = listOf("XSalsa20", "HC128", "HC256", "ZUC128", "ZUC256", "Grain128", "VMPC")
        
        streamCiphers.forEach { streamCipher ->
            val cipherAlgorithms = algorithms.filter { it.algorithmName == streamCipher }
            assertFalse("Should find $streamCipher algorithms", cipherAlgorithms.isEmpty())
            
            cipherAlgorithms.forEach { algorithm ->
                assertTrue("$streamCipher should support STREAM mode", 
                    algorithm.supportedModes.contains("STREAM"))
            }
        }
    }
    
    @Test
    fun testEnhancedSecurityRankings() {
        // Verify security rankings for new algorithms
        assertEquals("XSalsa20 should have high security ranking", 7, 
            AlgorithmCatalog.getSecurityRanking("XSalsa20"))
        assertEquals("HC256 should have good security ranking", 6, 
            AlgorithmCatalog.getSecurityRanking("HC256"))
        assertEquals("NOEKEON should have moderate security ranking", 5, 
            AlgorithmCatalog.getSecurityRanking("NOEKEON"))
        assertEquals("SHACAL2 should have good security ranking", 6, 
            AlgorithmCatalog.getSecurityRanking("SHACAL2"))
        assertEquals("ZUC256 should have good security ranking", 6, 
            AlgorithmCatalog.getSecurityRanking("ZUC256"))
        
        // Verify that enhanced algorithms rank better than legacy ones
        assertTrue("XSalsa20 should rank higher than RC4", 
            AlgorithmCatalog.getSecurityRanking("XSalsa20") > 
            AlgorithmCatalog.getSecurityRanking("RC4"))
        assertTrue("HC256 should rank higher than DES", 
            AlgorithmCatalog.getSecurityRanking("HC256") > 
            AlgorithmCatalog.getSecurityRanking("DES"))
    }
    
    @Test
    fun testInternationalStandards() {
        val algorithms = AlgorithmCatalog.getAllAlgorithms()
        val algorithmNames = algorithms.map { it.algorithmName }.toSet()
        
        // Test international cryptographic standards
        assertTrue("Should include Chinese ZUC128", algorithmNames.contains("ZUC128"))
        assertTrue("Should include Chinese ZUC256", algorithmNames.contains("ZUC256"))
        assertTrue("Should include Ukrainian DSTU7624", algorithmNames.contains("DSTU7624"))
        assertTrue("Should include Russian GOST3412", algorithmNames.contains("GOST3412"))
        assertTrue("Should include Korean ARIA", algorithmNames.contains("ARIA"))
        assertTrue("Should include Korean SEED", algorithmNames.contains("SEED"))
        assertTrue("Should include Japanese Camellia", algorithmNames.contains("Camellia"))
        
        println("International cryptographic standards supported:")
        val international = mapOf(
            "ZUC128" to "China",
            "ZUC256" to "China", 
            "SM4" to "China",
            "DSTU7624" to "Ukraine",
            "GOST3412" to "Russia",
            "GOST28147" to "Russia (legacy)",
            "ARIA" to "South Korea",
            "SEED" to "South Korea",
            "Camellia" to "Japan"
        )
        
        international.forEach { (algorithm, country) ->
            if (algorithmNames.contains(algorithm)) {
                val ranking = AlgorithmCatalog.getSecurityRanking(algorithm)
                println("  $algorithm ($country): security ranking $ranking/10")
            }
        }
    }
    
    @Test
    fun testLightweightCiphers() {
        val algorithms = AlgorithmCatalog.getAllAlgorithms()
        val algorithmNames = algorithms.map { it.algorithmName }.toSet()
        
        // Test lightweight/IoT-focused ciphers
        assertTrue("Should include lightweight NOEKEON", algorithmNames.contains("NOEKEON"))
        assertTrue("Should include lightweight Grain128", algorithmNames.contains("Grain128"))
        assertTrue("Should include efficient VMPC", algorithmNames.contains("VMPC"))
        
        println("Lightweight/IoT-optimized ciphers:")
        val lightweight = listOf("NOEKEON", "Grain128", "VMPC", "TEA", "XTEA")
        lightweight.forEach { algorithm ->
            if (algorithmNames.contains(algorithm)) {
                val variants = algorithms.filter { it.algorithmName == algorithm }
                val keySizes = variants.map { it.keySize }.distinct().sorted()
                val ranking = AlgorithmCatalog.getSecurityRanking(algorithm)
                println("  $algorithm: ${keySizes.joinToString(", ")} bit keys, security $ranking/10")
            }
        }
    }
}