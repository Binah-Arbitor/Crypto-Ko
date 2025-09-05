package com.cryptoko.crypto

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.junit.Test
import java.io.File
import java.security.Security
import javax.crypto.Cipher

class BouncyCastleAlgorithmDiscoveryTest {
    
    @Test
    fun discoverBouncyCastleAlgorithms() {
        // Add Bouncy Castle provider
        Security.addProvider(BouncyCastleProvider())
        
        // Get all Bouncy Castle services
        val bc = Security.getProvider("BC")
        
        val output = StringBuilder()
        output.appendLine("=== Bouncy Castle Cipher Algorithms ===")
        
        // Get all cipher services
        val cipherServices = bc.services
            .filter { it.type == "Cipher" }
            .map { it.algorithm }
            .sorted()
            .distinct()
        
        output.appendLine("Total cipher algorithms found: ${cipherServices.size}")
        
        val supportedAlgorithms = mutableListOf<String>()
        val unsupportedAlgorithms = mutableListOf<String>()
        
        cipherServices.forEach { algorithm ->
            try {
                val cipher = Cipher.getInstance(algorithm, "BC")
                output.appendLine("✓ $algorithm")
                supportedAlgorithms.add(algorithm)
            } catch (e: Exception) {
                output.appendLine("✗ $algorithm (${e.javaClass.simpleName}: ${e.message})")
                unsupportedAlgorithms.add(algorithm)
            }
        }
        
        output.appendLine("\n=== Algorithm Families ===")
        val families = supportedAlgorithms
            .map { it.split("/", ".").first() }
            .distinct()
            .sorted()
        
        families.forEach { family ->
            val variants = supportedAlgorithms.filter { it.startsWith(family) }
            output.appendLine("$family: ${variants.size} variants")
            variants.take(3).forEach { output.appendLine("  - $it") }
            if (variants.size > 3) output.appendLine("  ... and ${variants.size - 3} more")
        }
        
        // Write to file for inspection
        val file = File("/tmp/bc_algorithms.txt")
        file.writeText(output.toString())
        
        println("Algorithm discovery written to ${file.absolutePath}")
        println("Supported algorithms: ${supportedAlgorithms.size}")
        println("Unsupported algorithms: ${unsupportedAlgorithms.size}")
    }
}