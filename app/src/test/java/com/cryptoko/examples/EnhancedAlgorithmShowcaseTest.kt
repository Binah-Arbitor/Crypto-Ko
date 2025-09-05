package com.cryptoko.examples

import org.junit.Test
import java.io.File

/**
 * Test demonstrating the enhanced algorithm showcase
 */
class EnhancedAlgorithmShowcaseTest {
    
    @Test
    fun runEnhancedAlgorithmShowcase() {
        // Capture the showcase output
        val originalOut = System.out
        val output = StringBuilder()
        
        try {
            // Redirect stdout to capture output
            System.setOut(java.io.PrintStream(object : java.io.OutputStream() {
                override fun write(b: Int) {
                    output.append(b.toChar())
                }
            }))
            
            // Run the showcase
            EnhancedAlgorithmShowcase.showcaseEnhancedAlgorithms()
            
        } finally {
            // Restore stdout
            System.setOut(originalOut)
        }
        
        // Write output to file for verification
        val file = File("/tmp/enhanced_algorithm_showcase.txt")
        file.writeText(output.toString())
        
        println("Enhanced algorithm showcase written to ${file.absolutePath}")
        println("Output size: ${output.length} characters")
        
        // Verify the showcase contains expected content
        val content = output.toString()
        assert(content.contains("40+ Algorithm Families")) { "Should mention 40+ algorithms" }
        assert(content.contains("INTERNATIONAL CRYPTOGRAPHIC STANDARDS")) { "Should include international standards" }
        assert(content.contains("MODERN STREAM CIPHERS")) { "Should include modern stream ciphers" }
        assert(content.contains("XSalsa20")) { "Should mention XSalsa20" }
        assert(content.contains("ZUC256")) { "Should mention ZUC256" }
        assert(content.contains("NOEKEON")) { "Should mention NOEKEON" }
        
        // Print a sample to console for verification
        println("\nSample of enhanced algorithm showcase:")
        println(content.split("\n").take(10).joinToString("\n"))
    }
}