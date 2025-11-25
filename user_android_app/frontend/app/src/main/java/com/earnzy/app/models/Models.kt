package com.earnzy.app.models

import org.json.JSONArray
import org.json.JSONObject

data class EarningFeature(
    val title: String,
    val icon: Int,
    val subtitle: String
)

data class EarnTask(
    val id: Int = 0,
    val title: String,
    val reward: String,
    val duration: String = "",
    val category: String = "",
    var completed: Boolean = false,
    val iconUrl: String = "",
    val actionUrl: String = ""
)

data class Transaction(
    val description: String,
    val amount: String,
    val timestamp: String,
    val type: String  // "credit" or "debit"
)

data class Offer(
    val title: String,
    val reward: String,
    val description: String,
    val link: String,
    val icon: Int
)

data class LeaderboardEntry(
    val rank: Int,
    val name: String,
    val coins: Int,
    val trophyIcon: Int
)

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val icon: Int,
    val unlocked: Boolean,
    val progress: Int,
    val maxProgress: Int,
    val isCompleted: Boolean,
    val reward: Int
) {
    val progressPercentage: Int
        get() = if (maxProgress > 0) (progress * 100 / maxProgress) else 0

    val progressText: String
        get() = "$progress / $maxProgress"
}



data class DashboardResponse(
    val user: UserProfile,
    val features: List<AdminFeatureItem>, // Admin controls this list
    val banners: List<BannerItem>
)

data class UserProfile(
    val name: String,
    val coins: Int,
    val profileLottieUrl: String?
)


data class BannerItem(
    val id: String,
    val imageUrl: String? = null,           // Admin remote URL (high priority)
    val drawableRes: Int? = null,           // Local fallback (admin configurable)
    val clickAction: String? = null,        // e.g., "OPEN_OFFER"
    val deepLink: String? = null,           // Navigation URL
    val title: String? = null,              // Overlay text
    val isActive: Boolean = true,           // Admin toggle
    val durationMs: Long = 5000L,           // Admin slide duration per banner (ms)
    val priority: Int = 1,                  // Admin priority (1=high for URL, 0=local only)
    val enableAnalytics: Boolean = false    // Admin flag for click tracking
)

data class AdminFeatureItem(
    val id: String,
    val title: String,
    val lottieUrl: String,
    val iconFallbackRes: Int? = null,
    val clickAction: String,
    val isVisible: Boolean = true,
    val bgColorStart: String,
    val bgColorEnd: String,
    val subtitle: String? = null,
    val rewardText: String? = null
)

data class HomeResponse(
    val status: String,
    val message: String? = null,
    val user: JSONObject? = null,
    val banners: List<BannerItem> = emptyList(),
    val bannerLimit: Int = 4,
    val adminFeatures: List<AdminFeatureItem> = emptyList()
) {
    companion object {
        fun fromJson(json: JSONObject?): HomeResponse? {
            json ?: return null
            val status = json.optString("status", "error")
            if (status != "success") return null

            val user = json.optJSONObject("user")
            val bannersJson = json.optJSONArray("banners")
            val bannerLimit = json.optInt("banner_limit", 4).coerceIn(3, 6)
            val featuresJson = json.optJSONArray("admin_features")

            val banners = mutableListOf<BannerItem>()
            bannersJson?.let { arr ->
                repeat(arr.length()) { i ->
                    val item = arr.getJSONObject(i)
                    banners.add(
                        BannerItem(
                            id = item.optString("id", "default_$i"),
                            imageUrl = item.optString("image_url"),
                            drawableRes = item.optInt("drawable_res").takeIf { it > 0 },
                            clickAction = item.optString("click_action"),
                            deepLink = item.optString("deep_link"),
                            title = item.optString("title"),
                            isActive = item.optBoolean("is_active", true)
                        )
                    )
                }
            }

            val features = mutableListOf<AdminFeatureItem>()
            featuresJson?.let { arr ->
                repeat(arr.length()) { i ->
                    val item = arr.getJSONObject(i)
                    features.add(
                        AdminFeatureItem(
                            id = item.optString("id", "default_$i"),
                            title = item.optString("title"),
                            lottieUrl = item.optString("lottie_url"),
                            iconFallbackRes = item.optInt("icon_fallback").takeIf { it > 0 },
                            clickAction = item.optString("click_action"),
                            isVisible = item.optBoolean("is_visible", true),
                            bgColorStart = item.optString("bg_start", "#FF9966"),
                            bgColorEnd = item.optString("bg_end", "#FF5E62"),
                            subtitle = item.optString("subtitle"),
                            rewardText = item.optString("reward_text")
                        )
                    )
                }
            }

            return HomeResponse(
                status = status,
                message = json.optString("message"),
                user = user,
                banners = banners.take(bannerLimit),
                bannerLimit = bannerLimit,
                adminFeatures = features
            )
        }
    }
}
