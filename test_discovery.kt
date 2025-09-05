import com.cryptoko.crypto.CipherAlgorithm
import com.cryptoko.crypto.AlgorithmDiscovery

fun main() {
    println("=== Testing Dynamic Algorithm Discovery ===")
    
    try {
        val algorithms = CipherAlgorithm.ALL_ALGORITHMS
        println("Total algorithms found: ${algorithms.size}")
        
        val baseAlgorithms = CipherAlgorithm.getBaseAlgorithmNames()
        println("Base algorithms (${baseAlgorithms.size}): $baseAlgorithms")
        
        println("\nDetailed algorithm breakdown:")
        algorithms.groupBy { it.algorithmName }.forEach { (name, algos) ->
            val keySizes = algos.map { it.keySize }.sorted()
            println("  $name: $keySizes-bit keys")
        }
        
        println("\nProviders available:")
        val providers = CipherAlgorithm.getAvailableCipherProviders()
        providers.forEach { println("  - $provider") }
        
        // Test specific algorithms expected by tests
        println("\nTesting specific algorithm expectations:")
        
        val aesSizes = CipherAlgorithm.getKeySizesForAlgorithm("AES")
        println("AES key sizes: $aesSizes")
        println("AES supports 224-bit: ${aesSizes.contains(224)}")
        
        val blowfishSizes = CipherAlgorithm.getKeySizesForAlgorithm("Blowfish") 
        println("Blowfish key sizes: $blowfishSizes")
        println("Blowfish supports 448-bit: ${blowfishSizes.contains(448)}")
        
        val lastAlgorithm = baseAlgorithms.lastOrNull()
        println("Least secure algorithm: $lastAlgorithm")
        
    } catch (e: Exception) {
        println("Error during discovery: ${e.message}")
        e.printStackTrace()
    }
}