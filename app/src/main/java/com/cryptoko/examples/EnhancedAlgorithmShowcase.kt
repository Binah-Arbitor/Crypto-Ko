package com.cryptoko.examples

import com.cryptoko.crypto.AlgorithmCatalog
import com.cryptoko.crypto.CipherAlgorithm

/**
 * Enhanced example demonstrating the expanded cryptographic algorithm support
 * beyond traditional OpenSSL capabilities using Bouncy Castle
 */
object EnhancedAlgorithmShowcase {
    
    /**
     * Demonstrates the enhanced algorithm capabilities beyond OpenSSL
     */
    fun showcaseEnhancedAlgorithms() {
        println("=== Enhanced Cryptographic Algorithm Showcase ===")
        println("Beyond OpenSSL: 40+ Algorithm Families Available\n")
        
        val algorithms = AlgorithmCatalog.getAllAlgorithms()
        
        // Group algorithms by category
        showcaseByCategory(algorithms)
        
        // Show international standards
        showcaseInternationalStandards(algorithms)
        
        // Show modern stream ciphers
        showcaseModernStreamCiphers(algorithms)
        
        // Show lightweight/IoT algorithms  
        showcaseLightweightAlgorithms(algorithms)
        
        // Show advanced/research algorithms
        showcaseAdvancedAlgorithms(algorithms)
        
        // Security comparison
        showcaseSecurityComparison()
    }
    
    private fun showcaseByCategory(algorithms: List<CipherAlgorithm>) {
        println("🔒 HIGH SECURITY ALGORITHMS (8-10/10)")
        val highSecurity = algorithms.filter { 
            AlgorithmCatalog.getSecurityRanking(it.algorithmName) >= 8 
        }.groupBy { it.algorithmName }
        
        highSecurity.forEach { (name, variants) ->
            val keySizes = variants.map { it.keySize }.distinct().sorted()
            val ranking = AlgorithmCatalog.getSecurityRanking(name)
            println("  ✓ $name ($ranking/10): ${keySizes.joinToString(", ")} bit keys")
        }
        println()
    }
    
    private fun showcaseInternationalStandards(algorithms: List<CipherAlgorithm>) {
        println("🌍 INTERNATIONAL CRYPTOGRAPHIC STANDARDS")
        val international = mapOf(
            "AES" to "USA/International",
            "ARIA" to "South Korea", 
            "Camellia" to "Japan",
            "SEED" to "South Korea",
            "SM4" to "China",
            "ZUC128" to "China",
            "ZUC256" to "China", 
            "DSTU7624" to "Ukraine",
            "GOST3412" to "Russia (modern)",
            "GOST28147" to "Russia (legacy)"
        )
        
        international.forEach { (algorithm, country) ->
            val variants = algorithms.filter { it.algorithmName == algorithm }
            if (variants.isNotEmpty()) {
                val keySizes = variants.map { it.keySize }.distinct().sorted()
                val ranking = AlgorithmCatalog.getSecurityRanking(algorithm)
                println("  🏛️ $algorithm ($country): $ranking/10 security, ${keySizes.joinToString(", ")} bit keys")
            }
        }
        println()
    }
    
    private fun showcaseModernStreamCiphers(algorithms: List<CipherAlgorithm>) {
        println("🚀 MODERN STREAM CIPHERS")
        val streamCiphers = listOf("ChaCha20", "XSalsa20", "Salsa20", "HC128", "HC256", "ZUC128", "ZUC256")
        
        streamCiphers.forEach { algorithm ->
            val variants = algorithms.filter { it.algorithmName == algorithm }
            if (variants.isNotEmpty()) {
                val keySizes = variants.map { it.keySize }.distinct().sorted()
                val ranking = AlgorithmCatalog.getSecurityRanking(algorithm)
                val description = when (algorithm) {
                    "ChaCha20" -> "Google's modern cipher"
                    "XSalsa20" -> "Extended Salsa20 with larger nonce"
                    "HC128", "HC256" -> "eSTREAM finalist"
                    "ZUC128", "ZUC256" -> "Chinese standard"
                    else -> "Stream cipher"
                }
                println("  ⚡ $algorithm ($description): $ranking/10 security, ${keySizes.joinToString(", ")} bit keys")
            }
        }
        println()
    }
    
    private fun showcaseLightweightAlgorithms(algorithms: List<CipherAlgorithm>) {
        println("📱 LIGHTWEIGHT/IoT OPTIMIZED ALGORITHMS")
        val lightweight = listOf("NOEKEON", "Grain128", "VMPC", "TEA", "XTEA")
        
        lightweight.forEach { algorithm ->
            val variants = algorithms.filter { it.algorithmName == algorithm }
            if (variants.isNotEmpty()) {
                val keySizes = variants.map { it.keySize }.distinct().sorted()
                val ranking = AlgorithmCatalog.getSecurityRanking(algorithm)
                val description = when (algorithm) {
                    "NOEKEON" -> "Compact block cipher"
                    "Grain128" -> "Hardware-efficient stream cipher"
                    "VMPC" -> "Variable Memory Parameter Cipher"
                    "XTEA" -> "Extended TEA"
                    else -> "Lightweight cipher"
                }
                println("  🔧 $algorithm ($description): $ranking/10 security, ${keySizes.joinToString(", ")} bit keys")
            }
        }
        println()
    }
    
    private fun showcaseAdvancedAlgorithms(algorithms: List<CipherAlgorithm>) {
        println("🔬 ADVANCED/RESEARCH ALGORITHMS")
        val advanced = listOf("SHACAL2", "Threefish", "Tnepres", "DSTU7624")
        
        advanced.forEach { algorithm ->
            val variants = algorithms.filter { it.algorithmName == algorithm }
            if (variants.isNotEmpty()) {
                val keySizes = variants.map { it.keySize }.distinct().sorted()
                val ranking = AlgorithmCatalog.getSecurityRanking(algorithm)
                val description = when (algorithm) {
                    "SHACAL2" -> "Based on SHA-256 compression function"
                    "Threefish" -> "Part of Skein hash family, large blocks"
                    "Tnepres" -> "Serpent variant with different S-boxes"
                    "DSTU7624" -> "Ukrainian standard with large blocks"
                    else -> "Advanced cipher"
                }
                println("  🧪 $algorithm ($description): $ranking/10 security, ${keySizes.joinToString(", ")} bit keys")
            }
        }
        println()
    }
    
    private fun showcaseSecurityComparison() {
        println("📊 SECURITY COMPARISON vs OpenSSL")
        println("OpenSSL typically provides: AES, DES, 3DES, RC4, Blowfish (~5-6 algorithms)")
        println("Crypto-Ko Enhanced provides: 40+ algorithm families")
        println()
        
        val totalAlgorithms = CipherAlgorithm.getBaseAlgorithmNames().size
        val highSecurity = CipherAlgorithm.getBaseAlgorithmNames()
            .count { AlgorithmCatalog.getSecurityRanking(it) >= 8 }
        val international = CipherAlgorithm.getBaseAlgorithmNames()
            .count { 
                listOf("ARIA", "Camellia", "SEED", "SM4", "ZUC128", "ZUC256", 
                       "DSTU7624", "GOST3412", "GOST28147").contains(it) 
            }
        
        println("📈 Enhancement Statistics:")
        println("  • Total algorithms: $totalAlgorithms families")
        println("  • High security (8+/10): $highSecurity algorithms")
        println("  • International standards: $international algorithms")
        println("  • Modern stream ciphers: 7+ variants")
        println("  • Lightweight/IoT: 5+ optimized algorithms")
        println("  • Advanced/research: 4+ cutting-edge algorithms")
        println()
        
        println("🎯 Recommended Usage:")
        println("  • Maximum Security: AES-256-GCM, ChaCha20, XSalsa20")
        println("  • International: ARIA-256, Camellia-256, ZUC-256") 
        println("  • Lightweight: NOEKEON-128, Grain128, VMPC")
        println("  • Research: SHACAL-2-512, Threefish-1024")
        println("  • Avoid: DES, RC4, TEA (low security ratings)")
    }
}