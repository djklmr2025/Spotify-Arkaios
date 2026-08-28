package com.example.data.crypto

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Arkaios Storage & Stream Engine.
 * Direct passthrough audio downloader storing raw unencrypted audio (.mp3, .m4a, .flac)
 * directly into user-accessible storage without DRM or encryption locks.
 */
object ArkaiosOfflineCryptoEngine {
    private const val TAG = "ArkaiosCryptoEngine"

    /**
     * Writes input stream directly to target file as raw unencrypted audio.
     */
    fun encryptStreamToFile(input: InputStream, targetFile: File, totalLength: Long = 0L, onProgress: ((Int) -> Unit)? = null) {
        FileOutputStream(targetFile).use { fos ->
            val buffer = ByteArray(32 * 1024)
            var read: Int
            var writtenBytes = 0L
            while (input.read(buffer).also { read = it } != -1) {
                fos.write(buffer, 0, read)
                writtenBytes += read
                if (totalLength > 0 && onProgress != null) {
                    val pct = ((writtenBytes.toDouble() / totalLength.toDouble()) * 100).toInt().coerceIn(10, 95)
                    onProgress(pct)
                }
            }
            fos.flush()
        }
    }

    /**
     * Files are stored as raw audio files. Returns false as no encryption container is used.
     */
    fun isEncryptedArkCache(file: File): Boolean = false

    /**
     * Returns the target file directly since audio files are standard unencrypted audio streams.
     */
    fun getDecryptedPlayableFile(context: Context, encryptedFile: File): File? {
        return if (encryptedFile.exists()) encryptedFile else null
    }
}
