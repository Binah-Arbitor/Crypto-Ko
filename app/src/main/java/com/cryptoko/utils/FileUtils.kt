package com.cryptoko.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest

/**
 * Utility class for file operations and integrity checking
 * Provides extensible file handling capabilities
 */
object FileUtils {
    
    /**
     * Gets the display name of a file from its URI
     */
    fun getFileName(context: Context, uri: Uri): String? {
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            // Ensure proper UTF-8 encoding for Korean characters
            val fileName = cursor.getString(nameIndex)
            fileName?.let {
                // Check if the filename contains corrupted characters and try to fix them
                if (it.contains("?") || it.contains("�")) {
                    // If there are corruption indicators, try to get a clean name
                    "file_${System.currentTimeMillis()}"
                } else {
                    it
                }
            }
        }
    }
    
    /**
     * Gets the size of a file from its URI
     */
    fun getFileSize(context: Context, uri: Uri): Long {
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            cursor.moveToFirst()
            cursor.getLong(sizeIndex)
        } ?: 0L
    }
    
    /**
     * Copies a file from URI to internal storage
     * Returns the path to the copied file
     */
    fun copyUriToInternalStorage(context: Context, uri: Uri, fileName: String): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            // Sanitize filename for Korean characters - ensure UTF-8 safe filename  
            val safeFileName = sanitizeFileName(fileName)
            val internalFile = File(context.cacheDir, safeFileName)
            
            inputStream?.use { input ->
                FileOutputStream(internalFile).use { output ->
                    input.copyTo(output)
                }
            }
            
            internalFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Sanitizes filename to ensure it's safe for file system and UTF-8 compatible
     */
    private fun sanitizeFileName(fileName: String): String {
        // Replace problematic characters while preserving Korean characters
        val sanitized = fileName.replace(Regex("[<>:\"/\\\\|?*]"), "_")
        
        // Ensure the filename isn't empty and has reasonable length
        return if (sanitized.isBlank()) {
            "file_${System.currentTimeMillis()}"
        } else if (sanitized.length > 200) {
            sanitized.substring(0, 200)
        } else {
            sanitized
        }
    }
    
    /**
     * Calculates SHA-256 hash of a file for integrity verification
     */
    fun calculateFileHash(filePath: String): String? {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val file = File(filePath)
            
            FileInputStream(file).use { fis ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                
                while (fis.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
            }
            
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Formats file size in human-readable format
     */
    fun formatFileSize(bytes: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var size = bytes.toDouble()
        var unitIndex = 0
        
        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024
            unitIndex++
        }
        
        return "%.1f %s".format(size, units[unitIndex])
    }
    
    /**
     * Checks if a file extension suggests it's already encrypted
     */
    fun isLikelyEncryptedFile(fileName: String): Boolean {
        val encryptedExtensions = setOf(".enc", ".encrypted", ".aes", ".gpg", ".pgp")
        return encryptedExtensions.any { fileName.endsWith(it, ignoreCase = true) }
    }
    
    /**
     * Generates a unique output filename for encryption
     */
    fun generateEncryptedFileName(originalPath: String, algorithm: String): String {
        val file = File(originalPath)
        val nameWithoutExt = file.nameWithoutExtension
        val ext = file.extension
        val algorithm_short = algorithm.replace("-", "").lowercase()
        
        return if (ext.isNotEmpty()) {
            "${file.parent}/${nameWithoutExt}_${algorithm_short}.${ext}.enc"
        } else {
            "${originalPath}_${algorithm_short}.enc"
        }
    }
    
    /**
     * Generates output filename for decryption
     */
    fun generateDecryptedFileName(encryptedPath: String): String {
        return when {
            encryptedPath.endsWith(".enc") -> encryptedPath.removeSuffix(".enc")
            encryptedPath.endsWith(".encrypted") -> encryptedPath.removeSuffix(".encrypted")
            else -> "${encryptedPath}.decrypted"
        }
    }
    
    /**
     * Securely deletes a file by overwriting it before deletion
     */
    fun secureDelete(filePath: String): Boolean {
        return try {
            val file = File(filePath)
            if (file.exists()) {
                // Overwrite with random data
                FileOutputStream(file).use { fos ->
                    val randomData = ByteArray(file.length().toInt())
                    java.security.SecureRandom().nextBytes(randomData)
                    fos.write(randomData)
                    fos.flush()
                }
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}