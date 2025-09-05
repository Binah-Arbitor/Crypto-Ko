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
    val defaultPadding: String = "PKCS7Padding",
    val pbkdf2Iterations: Int = 100000,
    val bufferSize: Int = 8192,
    val enableSecureDelete: Boolean = true,
    val showPasswordStrength: Boolean = true,
    val autoGenerateFilenames: Boolean = true,
    val enableFileHashVerification: Boolean = false,
    val threadCount: Int = getDefaultThreadCount(),
    val enableMultithreading: Boolean = true
) {
    companion object {
        private const val PREFS_NAME = "crypto_ko_settings"
        private const val KEY_DEFAULT_ALGORITHM = "default_algorithm"
        private const val KEY_DEFAULT_MODE = "default_mode"
        private const val KEY_DEFAULT_PADDING = "default_padding"
        private const val KEY_PBKDF2_ITERATIONS = "pbkdf2_iterations"
        private const val KEY_BUFFER_SIZE = "buffer_size"
        private const val KEY_SECURE_DELETE = "secure_delete"
        private const val KEY_PASSWORD_STRENGTH = "password_strength"
        private const val KEY_AUTO_FILENAMES = "auto_filenames"
        private const val KEY_HASH_VERIFICATION = "hash_verification"
        private const val KEY_THREAD_COUNT = "thread_count"
        private const val KEY_ENABLE_MULTITHREADING = "enable_multithreading"
        
        /**
         * Get the default thread count based on CPU cores (max 2x cores, minimum 1)
         */
        fun getDefaultThreadCount(): Int {
            val coreCount = Runtime.getRuntime().availableProcessors()
            return (coreCount * 2).coerceIn(1, 16) // Cap at 16 threads for safety
        }
        
        /**
         * Get maximum allowed thread count (2x CPU cores)
         */
        fun getMaxThreadCount(): Int {
            return getDefaultThreadCount()
        }
        
        fun load(context: Context): AppConfig {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val defaultThreadCount = getDefaultThreadCount()
            return AppConfig(
                defaultAlgorithm = prefs.getString(KEY_DEFAULT_ALGORITHM, "AES-256") ?: "AES-256",
                defaultMode = prefs.getString(KEY_DEFAULT_MODE, "CBC") ?: "CBC",
                defaultPadding = prefs.getString(KEY_DEFAULT_PADDING, "PKCS7Padding") ?: "PKCS7Padding",
                pbkdf2Iterations = prefs.getInt(KEY_PBKDF2_ITERATIONS, 100000),
                bufferSize = prefs.getInt(KEY_BUFFER_SIZE, 8192),
                enableSecureDelete = prefs.getBoolean(KEY_SECURE_DELETE, true),
                showPasswordStrength = prefs.getBoolean(KEY_PASSWORD_STRENGTH, true),
                autoGenerateFilenames = prefs.getBoolean(KEY_AUTO_FILENAMES, true),
                enableFileHashVerification = prefs.getBoolean(KEY_HASH_VERIFICATION, false),
                threadCount = prefs.getInt(KEY_THREAD_COUNT, defaultThreadCount).coerceIn(1, getMaxThreadCount()),
                enableMultithreading = prefs.getBoolean(KEY_ENABLE_MULTITHREADING, true)
            )
        }
        
        fun save(context: Context, config: AppConfig) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().apply {
                putString(KEY_DEFAULT_ALGORITHM, config.defaultAlgorithm)
                putString(KEY_DEFAULT_MODE, config.defaultMode)
                putString(KEY_DEFAULT_PADDING, config.defaultPadding)
                putInt(KEY_PBKDF2_ITERATIONS, config.pbkdf2Iterations)
                putInt(KEY_BUFFER_SIZE, config.bufferSize)
                putBoolean(KEY_SECURE_DELETE, config.enableSecureDelete)
                putBoolean(KEY_PASSWORD_STRENGTH, config.showPasswordStrength)
                putBoolean(KEY_AUTO_FILENAMES, config.autoGenerateFilenames)
                putBoolean(KEY_HASH_VERIFICATION, config.enableFileHashVerification)
                putInt(KEY_THREAD_COUNT, config.threadCount.coerceIn(1, getMaxThreadCount()))
                putBoolean(KEY_ENABLE_MULTITHREADING, config.enableMultithreading)
                apply()
            }
        }
    }
}