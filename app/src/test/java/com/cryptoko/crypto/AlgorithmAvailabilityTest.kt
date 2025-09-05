package com.cryptoko.crypto

import org.junit.Test
import org.junit.Assert.*

/**
 * Test to check which enhanced algorithms are actually available
 */
class AlgorithmAvailabilityTest {
    
    @Test
    fun testAvailableEnhancedAlgorithms() {
        val algorithms = AlgorithmCatalog.getAllAlgorithms()
        val algorithmNames = algorithms.map { it.algorithmName }.toSet()
        
        println("Current algorithm catalog contains ${algorithms.size} algorithm configurations")
        println("Available algorithm families: ${algorithmNames.size}")
        
        // Test which of our enhanced algorithms are actually available
        val targetAlgorithms = listOf(
            "XSalsa20", "HC128", "HC256", "ZUC128", "ZUC256", "Grain128", "VMPC",
            "NOEKEON", "SHACAL2", "DSTU7624", "GOST3412", "Tnepres"
        )
        
        val available = mutableListOf<String>()
        val missing = mutableListOf<String>()
        
        targetAlgorithms.forEach { algorithm ->
            if (algorithmNames.contains(algorithm)) {
                available.add(algorithm)
            } else {
                missing.add(algorithm)
            }
        }
        
        println("Enhanced algorithms available: ${available.size}/${targetAlgorithms.size}")
        if (available.isNotEmpty()) {
            println("Available: ${available.joinToString(", ")}")
        }
        if (missing.isNotEmpty()) {
            println("Missing: ${missing.joinToString(", ")}")
        }
        
        // Test that we have a comprehensive set of cryptographic algorithms
        val basicAlgorithms = setOf("AES", "DES", "3DES", "RC4", "Blowfish")
        val availableBasic = algorithmNames.intersect(basicAlgorithms).size
        
        assertTrue("Should have basic encryption algorithms", availableBasic >= 4)
        assertTrue("Should have comprehensive algorithm support", 
            algorithmNames.size > basicAlgorithms.size * 3)
        
        // Print full list for verification
        val output = StringBuilder()
        output.appendLine("\nAll available algorithms:")
        algorithms.groupBy { it.algorithmName }.toSortedMap().forEach { (name, variants) ->
            val keySizes = variants.map { it.keySize }.distinct().sorted()
            val ranking = AlgorithmCatalog.getSecurityRanking(name)
            output.appendLine("  $name (security: $ranking/10): ${keySizes.joinToString(", ")} bit keys")
        }
        
        // Write to file
        val file = java.io.File("/tmp/available_algorithms.txt")
        file.writeText(output.toString())
        println("Algorithm list written to ${file.absolutePath}")
        println("Total algorithms: ${algorithmNames.size}")
        println("Enhanced algorithms available: ${available.size}/${targetAlgorithms.size}")
    }
}