package com.earnzy.app.models

data class AppConfig(
    val features: FeatureConfig,
    val spinWheel: SpinWheelConfig,
    val dailyBonus: DailyBonusConfig,
    val lottieAnimations: LottieAnimationConfig
)

data class FeatureConfig(
    val dailyBonusEnabled: Boolean = true,
    val spinWheelEnabled: Boolean = true,
    val scratchCardEnabled: Boolean = true,
    val offerwallEnabled: Boolean = true,
    val videoAdsEnabled: Boolean = true,
    val surveysEnabled: Boolean = true,
    val referralEnabled: Boolean = true,
    val supportChatEnabled: Boolean = true,
    val leaderboardEnabled: Boolean = true,
    val achievementsEnabled: Boolean = true
)

data class SpinWheelConfig(
    val enabled: Boolean = true,
    val wheelImageUrl: String = "",
    val backgroundImageUrl: String = "",
    val lottieAnimationUrl: String = "",
    val spinsPerDay: Int = 3,
    val rewards: List<Int> = listOf(10, 50, 20, 100, 30, 75, 25, 150)
)

data class DailyBonusConfig(
    val enabled: Boolean = true,
    val bonusAmount: Int = 100,
    val lottieAnimationUrl: String = "",
    val backgroundGradient: GradientConfig = GradientConfig()
)

data class LottieAnimationConfig(
    val homeAnimation: String = "",
    val dailyBonusAnimation: String = "",
    val spinWheelAnimation: String = "",
    val scratchCardAnimation: String = "",
    val successAnimation: String = "",
    val loadingAnimation: String = ""
)

data class GradientConfig(
    val startColor: String = "#F59E0B",
    val endColor: String = "#EC4899",
    val angle: Int = 135
)
