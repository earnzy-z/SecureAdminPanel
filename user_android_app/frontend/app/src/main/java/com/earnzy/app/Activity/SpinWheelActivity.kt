package com.earnzy.app.Activity

import android.animation.ObjectAnimator
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsetsController
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.earnzy.app.R
import com.earnzy.app.managers.ConfigManager
import com.earnzy.app.network.FeaturesApiClient
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

class SpinWheelActivity : AppCompatActivity() {

    private lateinit var wheelImage: ImageView
    private lateinit var spinButton: MaterialButton
    private lateinit var chancesText: TextView
    private lateinit var loadingProgress: ProgressBar
    private lateinit var rewardAnimation: LottieAnimationView
    private lateinit var spinCard: MaterialCardView
    private lateinit var securePrefs: SharedPreferences
    private lateinit var configManager: ConfigManager

    private var remainingSpins = 3
    private var isSpinning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupTransparentStatusBar()
        setContentView(R.layout.activity_spin_wheel)

        // Toolbar setup
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        configManager = ConfigManager.getInstance(this)
        initializeSecureStorage()

        // Bind views
        wheelImage = findViewById(R.id.spin_wheel_image)
        spinButton = findViewById(R.id.spin_button)
        chancesText = findViewById(R.id.spins_remaining)
        loadingProgress = findViewById(R.id.loading_progress)
        rewardAnimation = findViewById(R.id.reward_animation)
        spinCard = findViewById(R.id.spin_card)

        loadSpinWheelStatus()
        animateButtonEntrance()

        spinButton.setOnClickListener {
            if (!isSpinning && remainingSpins > 0) {
                spinWheelWithBackend()
            } else {
                Toast.makeText(this, "No spins left today!", Toast.LENGTH_SHORT).show()
            }
        }
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
            Log.e("SpinWheel", "Security setup failed", e)
            Toast.makeText(this, "Security setup failed", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupTransparentStatusBar() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                )
        }
        window.statusBarColor = Color.TRANSPARENT
    }

    private fun animateButtonEntrance() {
        spinButton.alpha = 0f
        spinButton.translationY = 100f
        spinButton.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(600)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    private fun loadSpinWheelStatus() {
        loadingProgress.visibility = View.VISIBLE
        spinButton.isEnabled = false
        spinButton.text = "Loading..."

        lifecycleScope.launch {
            try {
                val response = FeaturesApiClient.getSpinWheelStatus(
                    this@SpinWheelActivity,
                    getIdToken(),
                    getDeviceID(),
                    getDeviceToken(),
                    isVpn(),
                    isSslProxy()
                )

                if (response.getString("status") == "success") {
                    remainingSpins = response.getInt("spinsRemaining")
                    updateChancesText()
                    loadSpinWheelConfig()
                } else {
                    Toast.makeText(this@SpinWheelActivity, "Failed to load spin wheel", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("SpinWheel", "Failed to load status", e)
                Toast.makeText(this@SpinWheelActivity, "Network error. Try again.", Toast.LENGTH_SHORT).show()
            } finally {
                loadingProgress.visibility = View.GONE
                spinButton.isEnabled = remainingSpins > 0
                spinButton.text = if (remainingSpins > 0) "Spin Now" else "No Spins Left"
            }
        }
    }

    private fun loadSpinWheelConfig() {
        val config = configManager.getSpinWheelConfig()
        if (config.wheelImageUrl.isNotEmpty()) {
            Glide.with(this)
                .load(config.wheelImageUrl)
                .placeholder(R.drawable.ic_spin)
                .into(wheelImage)
        }

        if (!configManager.isFeatureEnabled("spin_wheel")) {
            spinButton.isEnabled = false
            spinButton.text = "Feature Disabled"
        }
    }

    private fun spinWheelWithBackend() {
        isSpinning = true
        spinButton.isEnabled = false
        spinButton.text = "Spinning..."

        val randomDegrees = Random.nextInt(1080, 2160).toFloat()
        val animator = ObjectAnimator.ofFloat(wheelImage, "rotation", 0f, randomDegrees)
        animator.duration = 3500
        animator.interpolator = DecelerateInterpolator(1.5f)
        animator.start()

        lifecycleScope.launch {
            try {
                val response = FeaturesApiClient.spinWheel(
                    this@SpinWheelActivity,
                    getIdToken(),
                    getDeviceID(),
                    getDeviceToken(),
                    isVpn(),
                    isSslProxy()
                )

                android.os.Handler(mainLooper).postDelayed({
                    if (response.getString("status") == "success") {
                        val reward = response.getInt("reward")
                        remainingSpins = response.getInt("spinsRemaining")
                        showRewardDialogAnimated(reward)
                        updateChancesText()
                    } else {
                        Toast.makeText(this@SpinWheelActivity, response.getString("message"), Toast.LENGTH_SHORT).show()
                    }
                    isSpinning = false
                    spinButton.isEnabled = remainingSpins > 0
                    spinButton.text = if (remainingSpins > 0) "Spin Now" else "No Spins Left"
                }, 3500)
            } catch (e: Exception) {
                Log.e("SpinWheel", "Spin failed", e)
                android.os.Handler(mainLooper).postDelayed({
                    Toast.makeText(this@SpinWheelActivity, "Failed to spin. Please try again.", Toast.LENGTH_SHORT).show()
                    isSpinning = false
                    spinButton.isEnabled = true
                    spinButton.text = "Spin Now"
                }, 3500)
            }
        }
    }

    private fun showRewardDialogAnimated(reward: Int) {
        rewardAnimation.visibility = View.VISIBLE
        rewardAnimation.playAnimation()

        try {
            val vibrator = getSystemService(android.content.Context.VIBRATOR_SERVICE) as android.os.Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(android.os.VibrationEffect.createOneShot(200, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(200)
            }
        } catch (e: Exception) {
            Log.e("SpinWheel", "Vibration failed", e)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("🎉 Congratulations!")
            .setMessage("You won $reward coins!\n\nKeep spinning daily for more rewards!")
            .setPositiveButton("Collect") { dialog, _ ->
                dialog.dismiss()
                rewardAnimation.visibility = View.GONE
            }
            .setCancelable(false)
            .show()
    }

    private fun updateChancesText() {
        chancesText.text = "Spins Left: $remainingSpins"
        chancesText.animate()
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(200)
            .withEndAction {
                chancesText.animate().scaleX(1f).scaleY(1f).setDuration(200).start()
            }.start()
    }

    // Helper functions
    private suspend fun getIdToken(): String {
        return try {
            FirebaseAuth.getInstance().currentUser?.getIdToken(false)?.await()?.token ?: ""
        } catch (e: Exception) {
            Log.e("SpinWheel", "Failed to get ID token", e)
            ""
        }
    }

    private fun getDeviceID(): String = securePrefs.getString("deviceID", "") ?: ""
    private fun getDeviceToken(): String = securePrefs.getString("deviceToken", "") ?: ""
    private fun isVpn(): Boolean = securePrefs.getBoolean("isVpn", false)
    private fun isSslProxy(): Boolean = securePrefs.getBoolean("isSslProxy", false)
}
