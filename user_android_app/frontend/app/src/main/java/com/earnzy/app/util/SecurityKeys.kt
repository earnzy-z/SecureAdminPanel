package com.earnzy.app

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import java.security.KeyStore
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import java.util.Locale

object SecurityKeys {
    private const val TAG = "SecurityKeys"
    private const val KEYSTORE_ALIAS = "earnzy_device_id"
    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val EXPECTED_SIGNATURE = "your_expected_sha256_signature" // Replace with your SHA-256 signature

    @JvmStatic
    fun getDeviceID(context: Context): String? {
        try {
            val storedId = getKeystoreDeviceID(context)
            if (storedId != null && storedId.matches("[a-f0-9]{64}".toRegex())) {
                return storedId
            }
        } catch (e: Exception) {
            Log.w(TAG, "Keystore device ID retrieval failed", e)
        }

        val brand = getObfuscatedBuildProp("BRAND")
        val manufacturer = getObfuscatedBuildProp("MANUFACTURER")
        val model = getObfuscatedBuildProp("MODEL")
        val device = getObfuscatedBuildProp("DEVICE")
        val product = getObfuscatedBuildProp("PRODUCT")
        val hardware = getObfuscatedBuildProp("HARDWARE")
        val serial = getObfuscatedBuildProp("SERIAL")
        val pkgSig = getPackageSignatureSha256(context) ?: ""

        return try {
            val combined = "$brand|$manufacturer|$model|$device|$product|$hardware|$serial|$pkgSig"
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(combined.toByteArray(Charsets.UTF_8))
            val deviceId = bytesToHex(digest)
            storeKeystoreDeviceID(context, deviceId)
            deviceId
        } catch (e: Exception) {
            Log.e(TAG, "Fallback device ID generation failed", e)
            null
        }
    }

    @JvmStatic
    fun computeDeviceID(input: String): String {
        return try {
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(input.toByteArray(Charsets.UTF_8))
            bytesToHex(digest)
        } catch (e: Exception) {
            Log.e(TAG, "computeDeviceID failed", e)
            ""
        }
    }

    @JvmStatic
    fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString("") { String.format(Locale.ROOT, "%02x", it) }
    }

    @JvmStatic
    fun isValidSignature(pkgSig: String?): Boolean {
        return EXPECTED_SIGNATURE == pkgSig
    }

    private fun getPackageSignatureSha256(context: Context): String? {
        return try {
            val pm = context.packageManager
            val pkg = context.packageName
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                PackageManager.GET_SIGNING_CERTIFICATES
            } else {
                @Suppress("DEPRECATION")
                PackageManager.GET_SIGNATURES
            }
            val info = pm.getPackageInfo(pkg, flags)
            val md = MessageDigest.getInstance("SHA-256")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                info.signingInfo?.apkContentsSigners?.firstOrNull()?.let { signer ->
                    return bytesToHex(md.digest(signer.toByteArray()))
                }
            } else {
                @Suppress("DEPRECATION")
                info.signatures?.firstOrNull()?.let { signature ->
                    return bytesToHex(md.digest(signature.toByteArray()))
                }
            }
            null
        } catch (e: Exception) {
            Log.w(TAG, "Package signature hash failed", e)
            null
        }
    }

    private fun getObfuscatedBuildProp(fieldName: String): String {
        return try {
            val buildClass = Class.forName("android.os.Build")
            val field = buildClass.getField(fieldName)
            field.get(null)?.toString() ?: ""
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get Build prop: $fieldName", e)
            ""
        }
    }

    private fun getKeystoreDeviceID(context: Context): String? {
        return try {
            val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
            keyStore.load(null)
            if (!keyStore.containsAlias(KEYSTORE_ALIAS)) return null
            val secretKey = keyStore.getKey(KEYSTORE_ALIAS, null) as SecretKey
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, ByteArray(12)))
            val encryptedId = context.getSharedPreferences("earnzy_secure", Context.MODE_PRIVATE)
                .getString("encrypted_device_id", null)?.toByteArray(Charsets.UTF_8) ?: return null
            val decryptedId = cipher.doFinal(encryptedId)
            String(decryptedId, Charsets.UTF_8)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to retrieve Keystore device ID", e)
            null
        }
    }

    private fun storeKeystoreDeviceID(context: Context, deviceId: String) {
        try {
            val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
            keyStore.load(null)
            if (!keyStore.containsAlias(KEYSTORE_ALIAS)) {
                val keyGenerator = KeyGenerator.getInstance("AES", KEYSTORE_PROVIDER)
                keyGenerator.init(256)
                keyGenerator.generateKey()
            }
            val secretKey = keyStore.getKey(KEYSTORE_ALIAS, null) as SecretKey
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val encryptedId = cipher.doFinal(deviceId.toByteArray(Charsets.UTF_8))
            context.getSharedPreferences("earnzy_secure", Context.MODE_PRIVATE)
                .edit()
                .putString("encrypted_device_id", String(encryptedId, Charsets.UTF_8))
                .apply()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to store Keystore device ID", e)
        }
    }
}