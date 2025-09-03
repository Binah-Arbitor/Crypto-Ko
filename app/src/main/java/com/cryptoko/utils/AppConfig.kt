package com.cryptoko.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Application configuration and settings management
 * Provides extensible configuration system for future features
 */
data class AppConfig(
    val defaultAlgorithm: String = "AES-256",
    val defaultMode: String = "CBC",
    val pbkdf2Iterations: Int = 100000,
    val bufferSize: Int = 8192,
    val enableSecureDelete: Boolean = true,
    val showPasswordStrength: Boolean = true,
    val autoGenerateFilenames: Boolean = true,
    val enableFileHashVerification: Boolean = false
) {
    companion object {
        private const val PREFS_NAME = "crypto_ko_settings"
        private const val KEY_DEFAULT_ALGORITHM = "default_algorithm"
        private const val KEY_DEFAULT_MODE = "default_mode"
        private const val KEY_PBKDF2_ITERATIONS = "pbkdf2_iterations"
        private const val KEY_BUFFER_SIZE = "buffer_size"
        private const val KEY_SECURE_DELETE = "secure_delete"
        private const val KEY_PASSWORD_STRENGTH = "password_strength"
        private const val KEY_AUTO_FILENAMES = "auto_filenames"
        private const val KEY_HASH_VERIFICATION = "hash_verification"
        
        fun load(context: Context): AppConfig {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return AppConfig(
                defaultAlgorithm = prefs.getString(KEY_DEFAULT_ALGORITHM, "AES-256") ?: "AES-256",
                defaultMode = prefs.getString(KEY_DEFAULT_MODE, "CBC") ?: "CBC",
                pbkdf2Iterations = prefs.getInt(KEY_PBKDF2_ITERATIONS, 100000),
                bufferSize = prefs.getInt(KEY_BUFFER_SIZE, 8192),
                enableSecureDelete = prefs.getBoolean(KEY_SECURE_DELETE, true),
                showPasswordStrength = prefs.getBoolean(KEY_PASSWORD_STRENGTH, true),
                autoGenerateFilenames = prefs.getBoolean(KEY_AUTO_FILENAMES, true),
                enableFileHashVerification = prefs.getBoolean(KEY_HASH_VERIFICATION, false)
            )
        }
        
        fun save(context: Context, config: AppConfig) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().apply {
                putString(KEY_DEFAULT_ALGORITHM, config.defaultAlgorithm)
                putString(KEY_DEFAULT_MODE, config.defaultMode)
                putInt(KEY_PBKDF2_ITERATIONS, config.pbkdf2Iterations)
                putInt(KEY_BUFFER_SIZE, config.bufferSize)
                putBoolean(KEY_SECURE_DELETE, config.enableSecureDelete)
                putBoolean(KEY_PASSWORD_STRENGTH, config.showPasswordStrength)
                putBoolean(KEY_AUTO_FILENAMES, config.autoGenerateFilenames)
                putBoolean(KEY_HASH_VERIFICATION, config.enableFileHashVerification)
                apply()
            }
        }
    }
}