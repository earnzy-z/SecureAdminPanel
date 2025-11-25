package com.earnzy.app.Activity

import android.animation.ValueAnimator
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsetsController
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.earnzy.app.R
import com.earnzy.app.network.FeaturesApiClient
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.airbnb.lottie.LottieAnimationView
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class DailyBonusActivity : AppCompatActivity() {

    private lateinit var bonusAnimation: LottieAnimationView
    private lateinit var claimButton: MaterialButton
    private lateinit var loadingProgress: ProgressBar
    private lateinit var streakText: TextView
    private lateinit var bonusAmountText: TextView
    private lateinit var nextClaimText: TextView
    private lateinit var bonusCard: MaterialCardView
    private lateinit var securePrefs: SharedPreferences
    private var bonusClaimed = false
    private var currentStreak = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setupTransparentStatusBar()
        setContentView(R.layout.activity_daily_bonus)
        
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }
        
        initializeSecureStorage()
        
        bonusAnimation = findViewById(R.id.bonus_animation)
        claimButton = findViewById(R.id.claim_button)
        loadingProgress = findViewById(R.id.loading_progress)
        streakText = findViewById(R.id.streak_text)
        bonusAmountText = findViewById(R.id.bonus_amount_text)
        nextClaimText = findViewById(R.id.next_claim_text)
        bonusCard = findViewById(R.id.bonus_card)
        
        claimButton.setOnClickListener {
            if (!bonusClaimed) {
                claimDailyBonusWithBackend()
            }
        }
        
        // Animate card entrance
        animateCardEntrance()
        
        // Check bonus status
        checkBonusStatus()
    }
    
    private fun setupTransparentStatusBar() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let {
                it.setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            )
        }
        
        window.statusBarColor = Color.TRANSPARENT
    }
    
    private fun initializeSecureStorage() {
        try {
            val masterKey = MasterKey.Builder(this)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            securePrefs = EncryptedSharedPreferences.create(
                this,
                "SecureEarnzyPrefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e("DailyBonus", "Security setup failed", e)
            Toast.makeText(this, "Security setup failed", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    private fun animateCardEntrance() {
        bonusCard.alpha = 0f
        bonusCard.scaleX = 0.8f
        bonusCard.scaleY = 0.8f
        bonusCard.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(500)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }
    
    private fun checkBonusStatus() {
        loadingProgress.visibility = View.VISIBLE
        claimButton.isEnabled = false
        
        lifecycleScope.launch {
            try {
                val response = FeaturesApiClient.getDailyBonusStatus(
                    this@DailyBonusActivity,
                    getIdToken(),
                    getDeviceID(),
                    getDeviceToken(),
                    isVpn(),
                    isSslProxy()
                )
                
                if (response.getString("status") == "success") {
                    val canClaim = response.getBoolean("canClaim")
                    currentStreak = response.getInt("streak")
                    val baseReward = response.getInt("baseReward")
                    val streakBonus = Math.min(currentStreak - 1, 6) * 10
                    val totalReward = baseReward + streakBonus
                    
                    // Update UI
                    streakText.text = "🔥 $currentStreak Day Streak"
                    bonusAmountText.text = "+$totalReward Coins"
                    
                    // Animate streak
                    animateStreak()
                    
                    if (canClaim) {
                        claimButton.isEnabled = true
                        claimButton.text = "Claim Daily Bonus"
                        nextClaimText.visibility = View.GONE
                        bonusAnimation.playAnimation()
                    } else {
                        claimButton.isEnabled = false
                        claimButton.text = "Already Claimed"
                        bonusClaimed = true
                        
                        // Show next claim time
                        val nextClaimTime = response.optString("nextClaimTime", "")
                        if (nextClaimTime.isNotEmpty()) {
                            try {
                                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                                val date = sdf.parse(nextClaimTime)
                                val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                                nextClaimText.text = "Come back tomorrow at ${timeFormat.format(date)}"
                                nextClaimText.visibility = View.VISIBLE
                            } catch (e: Exception) {
                                nextClaimText.text = "Come back tomorrow!"
                                nextClaimText.visibility = View.VISIBLE
                            }
                        }
                    }
                } else {
                    Toast.makeText(
                        this@DailyBonusActivity,
                        "Failed to load bonus status",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("DailyBonus", "Failed to check status", e)
                Toast.makeText(
                    this@DailyBonusActivity,
                    "Network error. Please try again.",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                loadingProgress.visibility = View.GONE
            }
        }
    }
    
    private fun animateStreak() {
        streakText.scaleX = 0f
        streakText.scaleY = 0f
        streakText.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(600)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    private fun claimDailyBonusWithBackend() {
        bonusClaimed = true
        claimButton.isEnabled = false
        claimButton.text = "Claiming..."
        loadingProgress.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                val response = FeaturesApiClient.claimDailyBonus(
                    this@DailyBonusActivity,
                    getIdToken(),
                    getDeviceID(),
                    getDeviceToken(),
                    isVpn(),
                    isSslProxy()
                )
                
                if (response.getString("status") == "success") {
                    val reward = response.getInt("reward")
                    val streak = response.getInt("streak")
                    currentStreak = streak
                    
                    // Update streak display
                    streakText.text = "🔥 $streak Day Streak"
                    
                    // Play animation
                    bonusAnimation.playAnimation()
                    
                    // Animate reward amount
                    animateRewardCounter(reward)
                    
                    // Vibrate
                    try {
                        val vibrator = getSystemService(android.content.Context.VIBRATOR_SERVICE) as android.os.Vibrator
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(android.os.VibrationEffect.createOneShot(300, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator.vibrate(300)
                        }
                    } catch (e: Exception) {
                        Log.e("DailyBonus", "Vibration failed", e)
                    }
                    
                    android.os.Handler(mainLooper).postDelayed({
                        showSuccessDialog(reward, streak)
                    }, 1500)
                } else {
                    Toast.makeText(
                        this@DailyBonusActivity,
                        response.getString("message"),
                        Toast.LENGTH_SHORT
                    ).show()
                    bonusClaimed = false
                    claimButton.isEnabled = true
                    claimButton.text = "Claim Daily Bonus"
                }
            } catch (e: Exception) {
                Log.e("DailyBonus", "Claim failed", e)
                Toast.makeText(
                    this@DailyBonusActivity,
                    "Failed to claim bonus. Please try again.",
                    Toast.LENGTH_SHORT
                ).show()
                bonusClaimed = false
                claimButton.isEnabled = true
                claimButton.text = "Claim Daily Bonus"
            } finally {
                loadingProgress.visibility = View.GONE
            }
        }
    }
    
    private fun animateRewardCounter(reward: Int) {
        val animator = ValueAnimator.ofInt(0, reward)
        animator.duration = 1500
        animator.addUpdateListener { animation ->
            bonusAmountText.text = "+${animation.animatedValue} Coins"
        }
        animator.start()
    }
    
    private fun showSuccessDialog(reward: Int, streak: Int) {
        val message = buildString {
            append("You received $reward coins!\n\n")
            append("Current Streak: $streak day${if (streak > 1) "s" else ""}\n")
            if (streak < 7) {
                val nextBonus = 100 + (streak * 10)
                append("Tomorrow's bonus: $nextBonus coins")
            } else {
                append("Maximum streak bonus reached! 🎉")
            }
        }
        
        MaterialAlertDialogBuilder(this)
            .setTitle("🎉 Daily Bonus Claimed!")
            .setMessage(message)
            .setPositiveButton("Awesome") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
            
        claimButton.text = "Claimed"
    }
    
    // Helper methods
    private suspend fun getIdToken(): String {
        return try {
            FirebaseAuth.getInstance().currentUser?.getIdToken(false)?.await()?.token ?: ""
        } catch (e: Exception) {
            Log.e("DailyBonus", "Failed to get ID token", e)
            ""
        }
    }
    
    private fun getDeviceID(): String = securePrefs.getString("deviceID", "") ?: ""
    private fun getDeviceToken(): String = securePrefs.getString("deviceToken", "") ?: ""
    private fun isVpn(): Boolean = securePrefs.getBoolean("isVpn", false)
    private fun isSslProxy(): Boolean = securePrefs.getBoolean("isSslProxy", false)
}

