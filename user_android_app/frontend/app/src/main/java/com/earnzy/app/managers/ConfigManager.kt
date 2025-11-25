package com.earnzy.app.managers

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.earnzy.app.models.AppConfig
import com.earnzy.app.models.FeatureConfig
import com.earnzy.app.models.SpinWheelConfig
import com.earnzy.app.models.DailyBonusConfig
import com.earnzy.app.models.LottieAnimationConfig
import com.earnzy.app.util.SecurityUtil
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class ConfigManager private constructor(private val context: Context) {

    private val _appConfig = MutableStateFlow(getDefaultConfig())
    val appConfig: StateFlow<AppConfig> = _appConfig
    
    private lateinit var securePrefs: SharedPreferences
    private val gson = Gson()

    companion object {
        @Volatile
        private var instance: ConfigManager? = null
        
        // Unified backend - all config requests go through the unified worker
        private const val CONFIG_URL = "https://earnzy-unified.earnzy.workers.dev/"

        fun getInstance(context: Context): ConfigManager {
            return instance ?: synchronized(this) {
                instance ?: ConfigManager(context.applicationContext).also { 
                    instance = it
                    it.initializeSecureStorage()
                }
            }
        }

        private fun getDefaultConfig(): AppConfig {
            return AppConfig(
                features = FeatureConfig(),
                spinWheel = SpinWheelConfig(),
                dailyBonus = DailyBonusConfig(),
                lottieAnimations = LottieAnimationConfig()
            )
        }
    }

    private fun initializeSecureStorage() {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            securePrefs = EncryptedSharedPreferences.create(
                context,
                "SecureConfigPrefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            
            // Load cached config
            loadCachedConfig()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun initialize() {
        // Fetch config from Cloudflare Worker (D1 database)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = SecurityUtil.sendEncryptedPostSuspend(
                    context,
                    CONFIG_URL,
                    JSONObject().apply {
                        put("action", "getConfig")
                    }
                )

                if (response.getString("success") == "true") {
                    val configJson = response.getJSONObject("config").toString()
                    val config = gson.fromJson(configJson, AppConfig::class.java)
                    _appConfig.value = config
                    
                    // Cache config in encrypted storage
                    securePrefs.edit().putString("app_config", configJson).apply()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Use cached config if network fails
                loadCachedConfig()
            }
        }
    }

    private fun loadCachedConfig() {
        try {
            val cachedConfigJson = securePrefs.getString("app_config", null)
            if (cachedConfigJson != null) {
                val config = gson.fromJson(cachedConfigJson, AppConfig::class.java)
                _appConfig.value = config
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isFeatureEnabled(featureName: String): Boolean {
        return when (featureName) {
            "daily_bonus" -> _appConfig.value.features.dailyBonusEnabled
            "spin_wheel" -> _appConfig.value.features.spinWheelEnabled
            "scratch_card" -> _appConfig.value.features.scratchCardEnabled
            "offerwall" -> _appConfig.value.features.offerwallEnabled
            "video_ads" -> _appConfig.value.features.videoAdsEnabled
            "surveys" -> _appConfig.value.features.surveysEnabled
            "referral" -> _appConfig.value.features.referralEnabled
            "support_chat" -> _appConfig.value.features.supportChatEnabled
            "leaderboard" -> _appConfig.value.features.leaderboardEnabled
            "achievements" -> _appConfig.value.features.achievementsEnabled
            else -> true
        }
    }

    fun getSpinWheelConfig(): SpinWheelConfig {
        return _appConfig.value.spinWheel
    }

    fun getDailyBonusConfig(): DailyBonusConfig {
        return _appConfig.value.dailyBonus
    }

    fun getLottieUrl(animationType: String): String {
        return when (animationType) {
            "home" -> _appConfig.value.lottieAnimations.homeAnimation
            "daily_bonus" -> _appConfig.value.lottieAnimations.dailyBonusAnimation
            "spin_wheel" -> _appConfig.value.lottieAnimations.spinWheelAnimation
            "scratch_card" -> _appConfig.value.lottieAnimations.scratchCardAnimation
            "success" -> _appConfig.value.lottieAnimations.successAnimation
            "loading" -> _appConfig.value.lottieAnimations.loadingAnimation
            else -> ""
        }
    }
}
