package com.earnzy.data

// Task Model
data class Task(
    val id: String,
    val title: String,
    val description: String,
    val reward: Int,
    val category: String,
    val imageUrl: String? = null,
    val completedAt: String? = null
)

data class TasksResponse(
    val tasks: List<Task>,
    val total: Int
)

// Offer Model
data class Offer(
    val id: String,
    val title: String,
    val description: String,
    val reward: Int,
    val imageUrl: String? = null,
    val category: String,
    val claimedAt: String? = null
)

// Promo Code Model
data class PromoCode(
    val id: String,
    val code: String,
    val reward: Int,
    val description: String? = null,
    val expiresAt: String? = null
)

// User Model
data class User(
    val id: String,
    val email: String,
    val name: String? = null,
    val totalEarned: Int = 0,
    val createdAt: String? = null
)

// Balance Model
data class Balance(
    val coins: Int,
    val level: Int,
    val nextLevelCoins: Int,
    val totalEarned: Int = 0,
    val totalWithdrawn: Int = 0,
    val pendingWithdrawal: Int = 0
)

// Referral Models
data class ReferralCode(
    val code: String,
    val expiresAt: String? = null
)

data class ReferralStats(
    val totalReferrals: Int = 0,
    val earnedCoins: Int = 0,
    val activeReferrals: Int = 0
)

// Transaction Model
data class TransactionRecord(
    val id: String,
    val title: String,
    val amount: Int,
    val date: String,
    val type: String // "credit" or "debit"
)

// Withdrawal Model
data class WithdrawalRequest(
    val id: String,
    val amount: Int,
    val method: String,
    val status: String,
    val createdAt: String
)

// API Response Models
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null
)

data class AuthResponse(
    val token: String,
    val user: User
)

data class OfferWallResponse(
    val offers: Map<String, List<Offer>>
)

data class PromoCodesResponse(
    val promoCodes: List<PromoCode>
)
