package com.earnzy.app.network

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
 * Network client for Earnzy Features API
 * Handles encryption and communication with backend Cloudflare Workers
 */
object FeaturesApiClient {

    // Unified backend with all features - hybrid encrypted, multiple DB connections
    private const val FEATURES_API_URL = "https://earnzy-features.earnzy.workers.dev/"

    /**
     * Get RSA public key from resources
     */
    private fun getRsaPublicKey(context: Context): PublicKey? {
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
            Log.e("FeaturesApiClient", "RSA Key Error", e)
            null
        }
    }

    /**
     * Encrypt data using hybrid RSA-OAEP + AES-CBC encryption
     */
    private fun encryptHybrid(context: Context, data: String): String {
        return try {
            val rsaPublicKey = getRsaPublicKey(context)
                ?: throw Exception("Failed to load RSA Public Key.")

            val sessionAesKey = ByteArray(32).apply { SecureRandom().nextBytes(this) }
            val iv = ByteArray(16).apply { SecureRandom().nextBytes(this) }
            val aesKeySpec = SecretKeySpec(sessionAesKey, "AES")
            val aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val ivSpec = IvParameterSpec(iv)
            aesCipher.init(Cipher.ENCRYPT_MODE, aesKeySpec, ivSpec)
            val encryptedBody = aesCipher.doFinal(data.toByteArray(StandardCharsets.UTF_8))

            val rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
            rsaCipher.init(Cipher.ENCRYPT_MODE, rsaPublicKey)
            val encryptedSessionKey = rsaCipher.doFinal(sessionAesKey)

            val combined = Base64.encodeToString(encryptedSessionKey, Base64.NO_WRAP) + "|" +
                    Base64.encodeToString(iv, Base64.NO_WRAP) + "|" +
                    Base64.encodeToString(encryptedBody, Base64.NO_WRAP)

            Base64.encodeToString(combined.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)
                .replace("+", "-")
                .replace("/", "_")
                .replace("=", "")

        } catch (e: Exception) {
            Log.e("FeaturesApiClient", "Encryption failed", e)
            ""
        }
    }

    /**
     * Send encrypted POST request to the API
     */
    private suspend fun sendEncryptedPost(
        context: Context,
        data: JSONObject
    ): JSONObject = withContext(Dispatchers.IO) {
        var conn: HttpURLConnection? = null
        try {
            val u = URL(FEATURES_API_URL)
            conn = u.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "text/plain; charset=utf-8")
            conn.doOutput = true
            conn.connectTimeout = 30000
            conn.readTimeout = 30000

            val encrypted = encryptHybrid(context, data.toString())
            if (encrypted.isEmpty()) {
                throw Exception("Encryption resulted in an empty string.")
            }
            
            Log.d("FeaturesApiClient", "Request Action: ${data.optString("action", "N/A")}")

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

            if (responseCode >= 400) {
                 Log.e("FeaturesApiClient", "API Error ($responseCode): $responseBody")
                 // Try to parse for a structured error message
                 return@withContext try {
                     val errorJson = JSONObject(responseBody)
                     if (!errorJson.has("status")) {
                        errorJson.put("status", "error")
                     }
                     errorJson
                 } catch (_: Exception) {
                     JSONObject().put("status", "error").put("message", "API Error ($responseCode): $responseBody")
                 }
            }
            
            Log.d("FeaturesApiClient", "Response: $responseBody")
            JSONObject(responseBody)

        } catch (e: Exception) {
            Log.e("FeaturesApiClient", "Network/Response Error for action ${data.optString("action")}", e)
            JSONObject().put("status", "error").put("message", e.localizedMessage ?: "A network error occurred.")
        } finally {
            conn?.disconnect()
        }
    }

    // ==================== DAILY BONUS ====================
    
    /**
     * Get daily bonus status
     */
    suspend fun getDailyBonusStatus(
        context: Context,
        idToken: String,
        deviceID: String,
        deviceToken: String,
        isVpn: Boolean,
        isSslProxy: Boolean
    ): JSONObject {
        val json = JSONObject().apply {
            put("action", "getDailyBonusStatus")
            put("idToken", idToken)
            put("deviceID", deviceID)
            put("deviceToken", deviceToken)
            put("isVpn", isVpn)
            put("isSslProxy", isSslProxy)
        }
        return sendEncryptedPost(context, json)
    }
    
    /**
     * Claim daily bonus
     */
    suspend fun claimDailyBonus(
        context: Context,
        idToken: String,
        deviceID: String,
        deviceToken: String,
        isVpn: Boolean,
        isSslProxy: Boolean
    ): JSONObject {
        val json = JSONObject().apply {
            put("action", "claimDailyBonus")
            put("idToken", idToken)
            put("deviceID", deviceID)
            put("deviceToken", deviceToken)
            put("isVpn", isVpn)
            put("isSslProxy", isSslProxy)
        }
        return sendEncryptedPost(context, json)
    }
    
    // ==================== SPIN WHEEL ====================
    
    /**
     * Get spin wheel status
     */
    suspend fun getSpinWheelStatus(
        context: Context,
        idToken: String,
        deviceID: String,
        deviceToken: String,
        isVpn: Boolean,
        isSslProxy: Boolean
    ): JSONObject {
        val json = JSONObject().apply {
            put("action", "getSpinWheelStatus")
            put("idToken", idToken)
            put("deviceID", deviceID)
            put("deviceToken", deviceToken)
            put("isVpn", isVpn)
            put("isSslProxy", isSslProxy)
        }
        return sendEncryptedPost(context, json)
    }
    
    /**
     * Spin the wheel
     */
    suspend fun spinWheel(
        context: Context,
        idToken: String,
        deviceID: String,
        deviceToken: String,
        isVpn: Boolean,
        isSslProxy: Boolean
    ): JSONObject {
        val json = JSONObject().apply {
            put("action", "spinWheel")
            put("idToken", idToken)
            put("deviceID", deviceID)
            put("deviceToken", deviceToken)
            put("isVpn", isVpn)
            put("isSslProxy", isSslProxy)
        }
        return sendEncryptedPost(context, json)
    }
    
    // ==================== TASKS ====================
    
    /**
     * Get all tasks
     */
    suspend fun getTasks(
        context: Context,
        idToken: String,
        deviceID: String,
        deviceToken: String,
        isVpn: Boolean,
        isSslProxy: Boolean
    ): JSONObject {
        val json = JSONObject().apply {
            put("action", "getTasks")
            put("idToken", idToken)
            put("deviceID", deviceID)
            put("deviceToken", deviceToken)
            put("isVpn", isVpn)
            put("isSslProxy", isSslProxy)
        }
        return sendEncryptedPost(context, json)
    }
    
    /**
     * Complete a task
     */
    suspend fun completeTask(
        context: Context,
        idToken: String,
        deviceID: String,
        deviceToken: String,
        taskId: String,
        isVpn: Boolean,
        isSslProxy: Boolean
    ): JSONObject {
        val json = JSONObject().apply {
            put("action", "completeTask")
            put("idToken", idToken)
            put("deviceID", deviceID)
            put("deviceToken", deviceToken)
            put("taskId", taskId)
            put("isVpn", isVpn)
            put("isSslProxy", isSslProxy)
        }
        return sendEncryptedPost(context, json)
    }
    
    // ==================== WITHDRAWALS ====================
    
    /**
     * Get withdrawal methods
     */
    suspend fun getWithdrawalMethods(
        context: Context,
        idToken: String,
        deviceID: String,
        deviceToken: String,
        isVpn: Boolean,
        isSslProxy: Boolean
    ): JSONObject {
        val json = JSONObject().apply {
            put("action", "getWithdrawalMethods")
            put("idToken", idToken)
            put("deviceID", deviceID)
            put("deviceToken", deviceToken)
            put("isVpn", isVpn)
            put("isSslProxy", isSslProxy)
        }
        return sendEncryptedPost(context, json)
    }
    
    /**
     * Request withdrawal
     */
    suspend fun requestWithdrawal(
        context: Context,
        idToken: String,
        deviceID: String,
        deviceToken: String,
        method: String,
        amount: Int,
        accountDetails: JSONObject,
        isVpn: Boolean,
        isSslProxy: Boolean
    ): JSONObject {
        val json = JSONObject().apply {
            put("action", "requestWithdrawal")
            put("idToken", idToken)
            put("deviceID", deviceID)
            put("deviceToken", deviceToken)
            put("withdrawalMethod", method)
            put("withdrawalAmount", amount)
            put("accountDetails", accountDetails)
            put("isVpn", isVpn)
            put("isSslProxy", isSslProxy)
        }
        return sendEncryptedPost(context, json)
    }
    
    /**
     * Get withdrawal history
     */
    suspend fun getWithdrawalHistory(
        context: Context,
        idToken: String,
        deviceID: String,
        deviceToken: String,
        isVpn: Boolean,
        isSslProxy: Boolean
    ): JSONObject {
        val json = JSONObject().apply {
            put("action", "getWithdrawalHistory")
            put("idToken", idToken)
            put("deviceID", deviceID)
            put("deviceToken", deviceToken)
            put("isVpn", isVpn)
            put("isSslProxy", isSslProxy)
        }
        return sendEncryptedPost(context, json)
    }
    
    // ==================== PROFILE ====================
    
    /**
     * Get user profile
     */
    suspend fun getUserProfile(
        context: Context,
        idToken: String,
        deviceID: String,
        deviceToken: String,
        isVpn: Boolean,
        isSslProxy: Boolean
    ): JSONObject {
        val json = JSONObject().apply {
            put("action", "getUserProfile")
            put("idToken", idToken)
            put("deviceID", deviceID)
            put("deviceToken", deviceToken)
            put("isVpn", isVpn)
            put("isSslProxy", isSslProxy)
        }
        return sendEncryptedPost(context, json)
    }
    
    /**
     * Get transaction history
     */
    suspend fun getTransactionHistory(
        context: Context,
        idToken: String,
        deviceID: String,
        deviceToken: String,
        limit: Int = 50,
        offset: Int = 0,
        isVpn: Boolean,
        isSslProxy: Boolean
    ): JSONObject {
        val json = JSONObject().apply {
            put("action", "getTransactionHistory")
            put("idToken", idToken)
            put("deviceID", deviceID)
            put("deviceToken", deviceToken)
            put("limit", limit)
            put("offset", offset)
            put("isVpn", isVpn)
            put("isSslProxy", isSslProxy)
        }
        return sendEncryptedPost(context, json)
    }
    
    // ==================== OFFERWALL ====================
    
    /**
     * Get offerwall offers (JSON-configurable)
     */
    suspend fun getOfferwall(
        context: Context,
        idToken: String,
        deviceID: String,
        deviceToken: String,
        isVpn: Boolean,
        isSslProxy: Boolean
    ): JSONObject {
        val json = JSONObject().apply {
            put("action", "getOfferwall")
            put("idToken", idToken)
            put("deviceID", deviceID)
            put("deviceToken", deviceToken)
            put("isVpn", isVpn)
            put("isSslProxy", isSslProxy)
        }
        return sendEncryptedPost(context, json)
    }
    
    /**
     * Complete offer
     */
    suspend fun completeOffer(
        context: Context,
        idToken: String,
        deviceID: String,
        deviceToken: String,
        offerId: String,
        isVpn: Boolean,
        isSslProxy: Boolean
    ): JSONObject {
        val json = JSONObject().apply {
            put("action", "completeOffer")
            put("idToken", idToken)
            put("deviceID", deviceID)
            put("deviceToken", deviceToken)
            put("offerId", offerId)
            put("isVpn", isVpn)
            put("isSslProxy", isSslProxy)
        }
        return sendEncryptedPost(context, json)
    }
    
    // ==================== EARN TASKS ====================
    
    /**
     * Get earn tasks (JSON-configurable)
     */
    suspend fun getEarnTasks(
        context: Context,
        idToken: String,
        deviceID: String,
        deviceToken: String,
        isVpn: Boolean,
        isSslProxy: Boolean
    ): JSONObject {
        val json = JSONObject().apply {
            put("action", "getEarnTasks")
            put("idToken", idToken)
            put("deviceID", deviceID)
            put("deviceToken", deviceToken)
            put("isVpn", isVpn)
            put("isSslProxy", isSslProxy)
        }
        return sendEncryptedPost(context, json)
    }

    // ==================== PAYMENT CARDS ====================
    suspend fun getPaymentCards(
        context: Context,
        idToken: String,
        deviceID: String,
        deviceToken: String,
        isVpn: Boolean,
        isSslProxy: Boolean
    ): JSONObject {
        val json = JSONObject().apply {
            put("action", "getPaymentCards")
            put("idToken", idToken)
            put("deviceID", deviceID)
            put("deviceToken", deviceToken)
            put("isVpn", isVpn)
            put("isSslProxy", isSslProxy)
        }
        return sendEncryptedPost(context, json)
    }

    suspend fun addPaymentCard(
        context: Context,
        idToken: String,
        deviceID: String,
        deviceToken: String,
        cardType: String,
        lastFourDigits: String,
        cardHolderName: String,
        expiryMonth: Int,
        expiryYear: Int,
        isDefault: Boolean,
        isVpn: Boolean,
        isSslProxy: Boolean
    ): JSONObject {
        val json = JSONObject().apply {
            put("action", "addPaymentCard")
            put("idToken", idToken)
            put("deviceID", deviceID)
            put("deviceToken", deviceToken)
            put("cardType", cardType)
            put("lastFourDigits", lastFourDigits)
            put("cardHolderName", cardHolderName)
            put("expiryMonth", expiryMonth)
            put("expiryYear", expiryYear)
            put("isDefault", isDefault)
            put("isVpn", isVpn)
            put("isSslProxy", isSslProxy)
        }
        return sendEncryptedPost(context, json)
    }

    suspend fun deletePaymentCard(
        context: Context,
        idToken: String,
        deviceID: String,
        deviceToken: String,
        cardId: Int,
        isVpn: Boolean,
        isSslProxy: Boolean
    ): JSONObject {
        val json = JSONObject().apply {
            put("action", "deletePaymentCard")
            put("idToken", idToken)
            put("deviceID", deviceID)
            put("deviceToken", deviceToken)
            put("cardId", cardId)
            put("isVpn", isVpn)
            put("isSslProxy", isSslProxy)
        }
        return sendEncryptedPost(context, json)
    }

    suspend fun setDefaultCard(
        context: Context,
        idToken: String,
        deviceID: String,
        deviceToken: String,
        cardId: Int,
        isVpn: Boolean,
        isSslProxy: Boolean
    ): JSONObject {
        val json = JSONObject().apply {
            put("action", "setDefaultCard")
            put("idToken", idToken)
            put("deviceID", deviceID)
            put("deviceToken", deviceToken)
            put("cardId", cardId)
            put("isVpn", isVpn)
            put("isSslProxy", isSslProxy)
        }
        return sendEncryptedPost(context, json)
    }

    // ==================== PROFILE MANAGEMENT ====================
    suspend fun updateProfile(
        context: Context,
        idToken: String,
        deviceID: String,
        deviceToken: String,
        name: String?,
        photo: String?,
        isVpn: Boolean,
        isSslProxy: Boolean
    ): JSONObject {
        val json = JSONObject().apply {
            put("action", "updateProfile")
            put("idToken", idToken)
            put("deviceID", deviceID)
            put("deviceToken", deviceToken)
            if (name != null) put("name", name)
            if (photo != null) put("photo", photo)
            put("isVpn", isVpn)
            put("isSslProxy", isSslProxy)
        }
        return sendEncryptedPost(context, json)
    }

    suspend fun updateEmail(
        context: Context,
        idToken: String,
        deviceID: String,
        deviceToken: String,
        email: String,
        isVpn: Boolean,
        isSslProxy: Boolean
    ): JSONObject {
        val json = JSONObject().apply {
            put("action", "updateEmail")
            put("idToken", idToken)
            put("deviceID", deviceID)
            put("deviceToken", deviceToken)
            put("email", email)
            put("isVpn", isVpn)
            put("isSslProxy", isSslProxy)
        }
        return sendEncryptedPost(context, json)
    }

    suspend fun cancelWithdrawal(
        context: Context,
        idToken: String,
        deviceID: String,
        deviceToken: String,
        transactionId: Int,
        isVpn: Boolean,
        isSslProxy: Boolean
    ): JSONObject {
        val json = JSONObject().apply {
            put("action", "cancelWithdrawal")
            put("idToken", idToken)
            put("deviceID", deviceID)
            put("deviceToken", deviceToken)
            put("transactionId", transactionId)
            put("isVpn", isVpn)
            put("isSslProxy", isSslProxy)
        }
        return sendEncryptedPost(context, json)
    }
    
    // ==================== ACHIEVEMENTS ====================
    
    suspend fun getAchievements(
        context: Context,
        idToken: String,
        deviceID: String,
        deviceToken: String,
        isVpn: Boolean,
        isSslProxy: Boolean
    ): JSONObject {
        val json = JSONObject().apply {
            put("action", "getAchievements")
            put("idToken", idToken)
            put("deviceID", deviceID)
            put("deviceToken", deviceToken)
            put("isVpn", isVpn)
            put("isSslProxy", isSslProxy)
        }
        return sendEncryptedPost(context, json)
    }
    
    // ==================== LEADERBOARD ====================
    
    suspend fun getLeaderboard(
        context: Context,
        idToken: String,
        deviceID: String,
        deviceToken: String,
        period: String = "weekly",
        limit: Int = 100,
        isVpn: Boolean,
        isSslProxy: Boolean
    ): JSONObject {
        val json = JSONObject().apply {
            put("action", "getLeaderboard")
            put("idToken", idToken)
            put("deviceID", deviceID)
            put("deviceToken", deviceToken)
            put("period", period)
            put("limit", limit)
            put("isVpn", isVpn)
            put("isSslProxy", isSslProxy)
        }
        return sendEncryptedPost(context, json)
    }
    
    // ==================== SCRATCH CARD ====================
    
    suspend fun getScratchCardStatus(
        context: Context,
        idToken: String,
        deviceID: String,
        deviceToken: String,
        isVpn: Boolean,
        isSslProxy: Boolean
    ): JSONObject {
        val json = JSONObject().apply {
            put("action", "getScratchCardStatus")
            put("idToken", idToken)
            put("deviceID", deviceID)
            put("deviceToken", deviceToken)
            put("isVpn", isVpn)
            put("isSslProxy", isSslProxy)
        }
        return sendEncryptedPost(context, json)
    }
    
    suspend fun scratchCard(
        context: Context,
        idToken: String,
        deviceID: String,
        deviceToken: String,
        isVpn: Boolean,
        isSslProxy: Boolean
    ): JSONObject {
        val json = JSONObject().apply {
            put("action", "scratchCard")
            put("idToken", idToken)
            put("deviceID", deviceID)
            put("deviceToken", deviceToken)
            put("isVpn", isVpn)
            put("isSslProxy", isSslProxy)
        }
        return sendEncryptedPost(context, json)
    }
    
    // ==================== REFERRAL STATS ====================
    
    suspend fun getReferralStats(
        context: Context,
        idToken: String,
        deviceID: String,
        deviceToken: String,
        isVpn: Boolean,
        isSslProxy: Boolean
    ): JSONObject {
        val json = JSONObject().apply {
            put("action", "getReferralStats")
            put("idToken", idToken)
            put("deviceID", deviceID)
            put("deviceToken", deviceToken)
            put("isVpn", isVpn)
            put("isSslProxy", isSslProxy)
        }
        return sendEncryptedPost(context, json)
    }
    
    // ==================== SUPPORT CHAT ====================
    
    suspend fun sendSupportMessage(
        context: Context,
        idToken: String,
        deviceID: String,
        deviceToken: String,
        message: String,
        isVpn: Boolean,
        isSslProxy: Boolean
    ): JSONObject {
        val json = JSONObject().apply {
            put("action", "sendSupportMessage")
            put("idToken", idToken)
            put("deviceID", deviceID)
            put("deviceToken", deviceToken)
            put("message", message)
            put("isVpn", isVpn)
            put("isSslProxy", isSslProxy)
        }
        return sendEncryptedPost(context, json)
    }
    
    suspend fun getSupportMessages(
        context: Context,
        idToken: String,
        deviceID: String,
        deviceToken: String,
        isVpn: Boolean,
        isSslProxy: Boolean
    ): JSONObject {
        val json = JSONObject().apply {
            put("action", "getSupportMessages")
            put("idToken", idToken)
            put("deviceID", deviceID)
            put("deviceToken", deviceToken)
            put("isVpn", isVpn)
            put("isSslProxy", isSslProxy)
        }
        return sendEncryptedPost(context, json)
    }
}