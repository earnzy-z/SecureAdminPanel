package com.earnzy.app.util

import android.content.Context
import android.util.Base64
import android.util.Log
import com.earnzy.app.R
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.PublicKey
import java.security.SecureRandom
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Security utility for hybrid RSA-OAEP + AES-CBC encryption
 * Matches the backend earnzy-auth Cloudflare Worker implementation
 */
object SecurityUtil {

    /**
     * Get RSA Public Key from resources
     */
    fun getRsaPublicKey(context: Context): PublicKey? {
        return try {
            val keyFactory = KeyFactory.getInstance("RSA")
            val pem = context.getString(R.string.server_rsa_public_key_pem2)
            val cleanKey = pem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("\\s".toRegex(), "")
            val keyBytes = Base64.decode(cleanKey, Base64.DEFAULT)
            val spec = X509EncodedKeySpec(keyBytes)
            keyFactory.generatePublic(spec)
        } catch (e: Exception) {
            Log.e("SecurityUtil", "RSA Key Error", e)
            null
        }
    }

    /**
     * Encrypt data using hybrid RSA-OAEP + AES-CBC encryption
     * Format: RSA_Key(B64)|IV(B64)|AES_Body(B64) -> Base64 URL-safe encoded
     */
    fun encryptHybrid(context: Context, data: String): String {
        return try {
            val rsaPublicKey = getRsaPublicKey(context)
            if (rsaPublicKey == null) {
                throw Exception("Failed to load RSA Public Key.")
            }
            
            // Generate session AES key and IV
            val sessionAesKey = ByteArray(32).apply { SecureRandom().nextBytes(this) }
            val iv = ByteArray(16).apply { SecureRandom().nextBytes(this) }
            
            // Encrypt body with AES-CBC
            val aesKeySpec = SecretKeySpec(sessionAesKey, "AES")
            val aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val ivSpec = IvParameterSpec(iv)
            aesCipher.init(Cipher.ENCRYPT_MODE, aesKeySpec, ivSpec)
            val encryptedBody = aesCipher.doFinal(data.toByteArray(StandardCharsets.UTF_8))

            // Encrypt session key with RSA-OAEP
            val rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
            rsaCipher.init(Cipher.ENCRYPT_MODE, rsaPublicKey)
            val encryptedSessionKey = rsaCipher.doFinal(sessionAesKey)

            // Combine: RSA_Key|IV|AES_Body
            val combined = Base64.encodeToString(encryptedSessionKey, Base64.NO_WRAP) + "|" +
                    Base64.encodeToString(iv, Base64.NO_WRAP) + "|" +
                    Base64.encodeToString(encryptedBody, Base64.NO_WRAP)

            // Final transport encoding (base64 URL safe)
            Base64.encodeToString(combined.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)
                .replace("+", "-")
                .replace("/", "_")
                .replace("=", "")

        } catch (e: Exception) {
            Log.e("SecurityUtil", "Encryption failed", e)
            ""
        }
    }

    /**
     * Send encrypted POST request to Cloudflare Worker
     */
    suspend fun sendEncryptedPostSuspend(
        context: Context,
        url: String,
        data: JSONObject
    ): JSONObject = withContext(Dispatchers.IO) {
        try {
            val u = URL(url)
            val conn = u.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "text/plain")
            conn.doOutput = true
            conn.connectTimeout = 30000
            conn.readTimeout = 30000

            val encrypted = encryptHybrid(context, data.toString())
            if (encrypted.isEmpty()) {
                throw Exception("Encryption failed. Hybrid key exchange failed.")
            }

            conn.outputStream.use { os ->
                OutputStreamWriter(os, StandardCharsets.UTF_8).use { writer ->
                    writer.write(encrypted)
                }
            }

            val responseCode = conn.responseCode
            val inputStream = if (responseCode < 400) conn.inputStream else conn.errorStream

            val responseBody = inputStream.use { stream ->
                BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8)).use { br ->
                    br.readText()
                }
            }

            JSONObject(responseBody)

        } catch (e: Exception) {
            Log.e("SecurityUtil", "Network/Response Error", e)
            throw e
        }
    }
}
