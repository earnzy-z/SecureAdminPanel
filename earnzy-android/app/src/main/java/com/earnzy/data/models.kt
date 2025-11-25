package com.earnzy.data

import com.google.gson.annotations.SerializedName

// User
data class User(
    val uid: String,
    val email: String,
    val coins: Int = 0,
    val level: Int = 1,
    val totalEarned: Int = 0,
    val isBanned: Boolean = false
)

// Task
data class Task(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val reward: Int,
    val imageUrl: String? = null,
    val isActive: Boolean = true,
    val completedAt: String? = null
)

// Offer
data class Offer(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val reward: Int,
    val imageUrl: String? = null,
    val isActive: Boolean = true,
    val claimedAt: String? = null
)

// Promo Code
data class PromoCode(
    val id: String,
    val code: String,
    val reward: Int,
    val description: String? = null,
    val expiresAt: String? = null,
    val isActive: Boolean = true,
    val usedAt: String? = null
)

// Referral
data class ReferralCode(
    val code: String,
    val deeplink: String,
    val shareUrl: String
)

data class ReferralStats(
    val totalReferrals: Int,
    val earnedCoins: Int,
    val activeReferrals: Int,
    val referrals: List<ReferralItem> = emptyList()
)

data class ReferralItem(
    val uid: String,
    val email: String? = null,
    val bonusCoins: Int = 50,
    val referredAt: String
)

// Reward/Redemption
data class Reward(
    val id: String,
    val name: String,
    val icon: String,
    val minCoins: Int,
    val rewards: List<RewardItem>
)

data class RewardItem(
    val amount: Int? = null,
    val name: String? = null,
    val coins: Int
)

data class RedemptionRequest(
    val id: String,
    val rewardId: String,
    val amount: Int,
    val status: String, // pending, completed, failed
    val requestedAt: String,
    val completedAt: String? = null
)

// Coin History
data class Transaction(
    val id: String,
    val type: String, // earn, spend, bonus, referral
    val amount: Int,
    val description: String,
    val createdAt: String
)

// API Responses
data class ApiResponse<T>(
    val success: Boolean? = null,
    val data: T? = null,
    val error: String? = null,
    val message: String? = null
)

data class AuthResponse(
    val token: String,
    val user: User
)

data class BalanceResponse(
    val coins: Int,
    val level: Int,
    val nextLevelCoins: Int
)

data class TasksResponse(
    val tasks: List<Task>,
    val meta: Meta? = null
)

data class OffersResponse(
    val offers: List<Offer>,
    val meta: Meta? = null
)

data class Meta(
    val total: Int? = null,
    val page: Int? = null
)
