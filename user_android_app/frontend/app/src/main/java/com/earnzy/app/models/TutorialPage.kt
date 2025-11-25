package com.earnzy.app.models

import androidx.annotation.RawRes

/**
 * Tutorial page data model
 */
data class TutorialPage(
    val title: String,
    val description: String,
    @RawRes val animationRes: Int,
    val showFeatures: Boolean = false,
    val features: List<String> = emptyList()
)
