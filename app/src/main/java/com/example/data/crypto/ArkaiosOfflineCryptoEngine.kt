package com.example.data.crypto

import android.content.Context
import android.util.Log
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Arkaios DRM & Offline Cache Encryption Engine.
 * Encrypts music streams into proprietary .arkcache format (AES-128-CBC)
 * preventing raw audio extraction by third-party apps while allowing
 * zero-latency hardware streaming in Arkaios-Tify.
 */
object ArkaiosOfflineCryptoEngine {
    private const val TAG = "ArkaiosCryptoEngine"
    private const val HEADER_MAGIC = "ARKAIOS_VAULT_DRM_V1"
    private val AES_KEY_BYTES = byteArrayOf(
        0x41.toByte(), 0x72.toByte(), 0x6B.toByte(), 0x61.toByte(),
        0x69.toByte(), 0x6F.toByte(), 0x73.toByte(), 0x5F.toByte(),
        0x54.toByte(), 0x72.toByte(), 0x65.toByte(), 0x61.toByte(),
        0x73.toByte(), 0x75.toByte(), 0x72.toByte(), 0x65.toByte()
    ) // "Arkaios_Treasure"
    private val FIXED_IV = byteArrayOf(
        0x10, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88.toByte(),
        0x99.toByte(), 0xAA.toByte(), 0xBB.toByte(), 0xCC.toByte(),
        0xDD.toByte(), 0xEE.toByte(), 0xFF.toByte(), 0x00
    )

    private fun getSecretKey(): SecretKeySpec = SecretKeySpec(AES_KEY_BYTES, "AES")
    private fun getIvSpec(): IvParameterSpec = IvParameterSpec(FIXED_IV)

    /**
     * Encrypts input stream into an .arkcache DRM container file.
     */
    fun encryptStreamToFile(input: InputStream, targetFile: File, totalLength: Long = 0L, onProgress: ((Int) -> Unit)? = null) {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(), getIvSpec())

        FileOutputStream(targetFile).use { fos ->
            // Write proprietary Arkaios magic header
            fos.write(HEADER_MAGIC.toByteArray(Charsets.UTF_8))
            fos.write(0x0A) // newline delimiter

            CipherOutputStream(fos, cipher).use { cos ->
                val buffer = ByteArray(16 * 1024)
                var read: Int
                var writtenBytes = 0L
                while (input.read(buffer).also { read = it } != -1) {
                    cos.write(buffer, 0, read)
                    writtenBytes += read
                    if (totalLength > 0 && onProgress != null) {
                        val pct = ((writtenBytes.toDouble() / totalLength.toDouble()) * 100).toInt().coerceIn(10, 95)
                        onProgress(pct)
                    }
                }
                cos.flush()
            }
        }
    }

    /**
     * Checks if a file is an encrypted .arkcache container.
     */
    fun isEncryptedArkCache(file: File): Boolean {
        if (!file.exists() || file.length() < HEADER_MAGIC.length) return false
        try {
            FileInputStream(file).use { fis ->
                val header = ByteArray(HEADER_MAGIC.length)
                val read = fis.read(header)
                return read == HEADER_MAGIC.length && String(header, Charsets.UTF_8) == HEADER_MAGIC
            }
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Creates a temporary decrypted playable buffer file in private cache for MediaPlayer.
     */
    fun getDecryptedPlayableFile(context: Context, encryptedFile: File): File? {
        try {
            if (!isEncryptedArkCache(encryptedFile)) {
                return encryptedFile // Not encrypted or raw audio
            }

            val cacheDir = File(context.cacheDir, "arkaios_play_stream").apply { if (!exists()) mkdirs() }
            val tempPlayable = File(cacheDir, "temp_stream_${encryptedFile.name.hashCode()}.mp3")

            // Decrypt stream
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), getIvSpec())

            FileInputStream(encryptedFile).use { fis ->
                // Skip magic header + newline
                val headerBytes = ByteArray(HEADER_MAGIC.length + 1)
                fis.read(headerBytes)

                CipherInputStream(fis, cipher).use { cis ->
                    FileOutputStream(tempPlayable).use { fos ->
                        val buffer = ByteArray(32 * 1024)
                        var read: Int
                        while (cis.read(buffer).also { read = it } != -1) {
                            fos.write(buffer, 0, read)
                        }
                    }
                }
            }
            tempPlayable.deleteOnExit()
            return tempPlayable
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decrypt Arkaios cache file", e)
            return null
        }
    }
}
