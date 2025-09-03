package com.cryptoko.utils

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Utility class for secure key derivation using PBKDF2
 * Provides future-ready implementation for better security
 */
object KeyDerivation {
    
    private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val DEFAULT_ITERATIONS = 100000
    private const val SALT_LENGTH = 16
    
    /**
     * Derives a cryptographic key from a password using PBKDF2
     * 
     * @param password The user password
     * @param salt Random salt (will be generated if null)
     * @param keyLength Desired key length in bytes
     * @param iterations Number of PBKDF2 iterations
     * @return Pair of (derived key, salt used)
     */
    fun deriveKey(
        password: String,
        salt: ByteArray? = null,
        keyLength: Int,
        iterations: Int = DEFAULT_ITERATIONS
    ): Pair<SecretKeySpec, ByteArray> {
        
        val actualSalt = salt ?: generateSalt()
        
        val spec = PBEKeySpec(
            password.toCharArray(),
            actualSalt,
            iterations,
            keyLength * 8 // Convert to bits
        )
        
        val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
        val derivedKeyBytes = factory.generateSecret(spec).encoded
        
        // Clear the password from memory
        spec.clearPassword()
        
        val secretKey = SecretKeySpec(derivedKeyBytes, "AES")
        
        return Pair(secretKey, actualSalt)
    }
    
    /**
     * Generates a cryptographically secure random salt
     */
    fun generateSalt(length: Int = SALT_LENGTH): ByteArray {
        val salt = ByteArray(length)
        SecureRandom().nextBytes(salt)
        return salt
    }
    
    /**
     * Securely clears a byte array from memory
     */
    fun clearByteArray(array: ByteArray) {
        array.fill(0)
    }
    
    /**
     * Validates password strength (basic implementation)
     * Can be extended for more sophisticated validation
     */
    fun validatePasswordStrength(password: String): PasswordStrength {
        return when {
            password.length < 8 -> PasswordStrength.WEAK
            password.length < 12 -> PasswordStrength.MEDIUM
            password.length >= 12 && hasSpecialCharacters(password) -> PasswordStrength.STRONG
            else -> PasswordStrength.MEDIUM
        }
    }
    
    private fun hasSpecialCharacters(password: String): Boolean {
        return password.any { it.isDigit() } &&
               password.any { it.isUpperCase() } &&
               password.any { it.isLowerCase() } &&
               password.any { !it.isLetterOrDigit() }
    }
}

enum class PasswordStrength {
    WEAK, MEDIUM, STRONG
}