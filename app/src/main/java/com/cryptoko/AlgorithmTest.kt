package com.cryptoko.crypto

import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security

fun main() {
    // Add Bouncy Castle provider for additional algorithms
    if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
        Security.addProvider(BouncyCastleProvider())
    }
    
    println("=== Algorithm Discovery Test ===")
    
    val algorithms = AlgorithmDiscovery.discoverAlgorithms()
    println("Total algorithms found: ${algorithms.size}")
    
    val baseAlgorithms = algorithms.map { it.algorithmName }.distinct()
    println("Base algorithms: ${baseAlgorithms.size}")
    baseAlgorithms.forEach { println("  - $it") }
    
    // Test the same logic from CipherAlgorithm.getBaseAlgorithmNames()
    val sortedBaseAlgorithms = baseAlgorithms.sortedByDescending { AlgorithmDiscovery.getSecurityRanking(it) }
    println("\nSorted by security ranking:")
    sortedBaseAlgorithms.forEach { 
        val ranking = AlgorithmDiscovery.getSecurityRanking(it)
        println("  - $it (ranking: $ranking)") 
    }
}