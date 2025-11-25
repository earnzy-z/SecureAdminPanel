package com.earnzy.app.models

data class ChatMessage(
    val text: String,
    val isSent: Boolean,
    val timestamp: Long
)
