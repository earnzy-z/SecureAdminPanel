package com.earnzy.api

import com.earnzy.data.*
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface EarnzyApiService {
    // Auth
    @POST("auth/register")
    suspend fun register(@Body body: Map<String, Any>): AuthResponse

    @GET("auth/me")
    suspend fun getUser(): User

    @POST("auth/logout")
    suspend fun logout(): ApiResponse<Nothing>

    // Tasks
    @GET("tasks")
    suspend fun getTasks(): TasksResponse

    @GET("tasks/{id}")
    suspend fun getTask(@Path("id") taskId: String): Task

    @POST("tasks/{id}/complete")
    suspend fun completeTask(@Path("id") taskId: String, @Body body: Map<String, Any>): ApiResponse<Map<String, Any>>

    // Offers
    @GET("offers")
    suspend fun getOffers(@Query("category") category: String? = null): OffersResponse

    @GET("offers/wall/list")
    suspend fun getOfferWall(): Map<String, List<Offer>>

    @POST("offers/{id}/claim")
    suspend fun claimOffer(@Path("id") offerId: String): ApiResponse<Nothing>

    // Coins
    @GET("coins/balance")
    suspend fun getBalance(): BalanceResponse

    @GET("coins/history")
    suspend fun getCoinHistory(): Map<String, Any>

    // Referral
    @GET("referral/code")
    suspend fun getReferralCode(): ReferralCode

    @GET("referral/stats")
    suspend fun getReferralStats(): ReferralStats

    @POST("referral/accept")
    suspend fun acceptReferral(@Body body: Map<String, String>): ApiResponse<Map<String, Any>>

    // Promo Codes
    @GET("promos")
    suspend fun getPromoCodes(): Map<String, List<PromoCode>>

    @POST("promos/redeem")
    suspend fun redeemPromo(@Body body: Map<String, String>): ApiResponse<Map<String, Any>>

    // Rewards
    @GET("rewards")
    suspend fun getRewards(): Map<String, List<Reward>>

    @POST("rewards/request")
    suspend fun requestRedemption(@Body body: Map<String, Any>): ApiResponse<Map<String, String>>

    @GET("rewards/history")
    suspend fun getRedemptionHistory(): Map<String, List<RedemptionRequest>>
}

object ApiClient {
    private const val BASE_URL = "https://api.earnzy.com/"
    private var token: String = ""

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val originalRequest = chain.request()
            val requestBuilder = originalRequest.newBuilder()

            if (token.isNotEmpty()) {
                requestBuilder.header("Authorization", "Bearer $token")
            }

            requestBuilder.header("Content-Type", "application/json")
            val newRequest = requestBuilder.build()
            chain.proceed(newRequest)
        }
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: EarnzyApiService = retrofit.create(EarnzyApiService::class.java)

    fun setToken(newToken: String) {
        token = newToken
    }

    fun getToken(): String = token

    fun clearToken() {
        token = ""
    }
}
