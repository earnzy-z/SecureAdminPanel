package com.earnzy.app.Activity

import android.animation.ValueAnimator
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsetsController
import android.view.animation.BounceInterpolator
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.airbnb.lottie.LottieAnimationView
import com.earnzy.app.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

class ScratchCardActivity : AppCompatActivity() {

    private lateinit var scratchView: MaterialCardView
    private lateinit var resetButton: MaterialButton
    private lateinit var prizeText: TextView
    private lateinit var securePrefs: SharedPreferences

    private var isScratched = false
    private var dailyChances = 3
    private var chancesUsed = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupTransparentStatusBar()
        setContentView(R.layout.activity_scratch_card)

        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        initializeSecureStorage()
        initViews()
        loadScratchCardStatus()

        scratchView.setOnClickListener {
            if (!isScratched && chancesUsed < dailyChances) {
                scratchCard()
            } else if (chancesUsed >= dailyChances) {
                Toast.makeText(this, "No chances left today! Come back tomorrow", Toast.LENGTH_SHORT).show()
            }
        }

        resetButton.setOnClickListener {
            if (chancesUsed < dailyChances) {
                resetCard()
            } else {
                Toast.makeText(this, "No more chances today!", Toast.LENGTH_SHORT).show()
            }
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
            Log.e("ScratchCard", "Security setup failed", e)
        }
    }

    private fun initViews() {
        scratchView = findViewById(R.id.scratch_view)
        resetButton = findViewById(R.id.reset_button)
        prizeText = findViewById(R.id.prize_text)
    }

    private fun loadScratchCardStatus() {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        val lastScratchDate = securePrefs.getString("lastScratchDate", "")

        if (lastScratchDate != today) {
            chancesUsed = 0
            securePrefs.edit().putString("lastScratchDate", today).putInt("scratchChancesUsed", 0).apply()
        } else {
            chancesUsed = securePrefs.getInt("scratchChancesUsed", 0)
        }

        updateChancesText()
    }

    private fun updateChancesText() {
        val remaining = dailyChances - chancesUsed
        resetButton.text = if (remaining > 0) "Get New Card ($remaining left)" else "Come Back Tomorrow"
        resetButton.isEnabled = remaining > 0
    }

    private fun scratchCard() {
        isScratched = true
        scratchView.isClickable = false

        val prize = generatePrize()

        scratchView.animate()
            .alpha(0.3f)
            .setDuration(1000)
            .withEndAction {
                prizeText.text = "🎉 You Won\n₹$prize"
                prizeText.visibility = View.VISIBLE

                prizeText.scaleX = 0f
                prizeText.scaleY = 0f
                prizeText.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(600)
                    .setInterpolator(BounceInterpolator())
                    .start()

                animatePrizeCounter(prize)
                vibrateDevice(300)

                chancesUsed++
                securePrefs.edit().putInt("scratchChancesUsed", chancesUsed).apply()
                updateChancesText()

                android.os.Handler(mainLooper).postDelayed({
                    showCollectDialog(prize)
                }, 1500)
            }
            .start()
    }

    private fun generatePrize(): Int {
        val random = Random.nextInt(100)
        return when {
            random < 50 -> Random.nextInt(10, 50)
            random < 80 -> Random.nextInt(50, 100)
            random < 95 -> Random.nextInt(100, 250)
            else -> Random.nextInt(250, 1000)
        }
    }

    private fun animatePrizeCounter(prize: Int) {
        val animator = ValueAnimator.ofInt(0, prize)
        animator.duration = 1000
        animator.addUpdateListener { animation ->
            prizeText.text = "🎉 You Won\n₹${animation.animatedValue}"
        }
        animator.start()
    }

    private fun vibrateDevice(duration: Long) {
        try {
            val vibrator = getSystemService(android.content.Context.VIBRATOR_SERVICE) as android.os.Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(android.os.VibrationEffect.createOneShot(duration, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(duration)
            }
        } catch (e: Exception) {
            Log.e("ScratchCard", "Vibration failed", e)
        }
    }

    private fun showCollectDialog(prize: Int) {
        MaterialAlertDialogBuilder(this)
            .setTitle("💰 Congratulations!")
            .setMessage("You won ₹$prize coins!\n\nCoins have been added to your wallet.")
            .setPositiveButton("Collect") { dialog, _ ->
                dialog.dismiss()
                if (chancesUsed < dailyChances) {
                    resetCard()
                }
            }
            .setCancelable(false)
            .show()
    }

    private fun resetCard() {
        isScratched = false
        scratchView.isClickable = true
        scratchView.alpha = 1f
        prizeText.visibility = View.GONE
        prizeText.text = "Scratch to reveal\nyour prize!"

        scratchView.scaleX = 0.8f
        scratchView.scaleY = 0.8f
        scratchView.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(300)
            .setInterpolator(BounceInterpolator())
            .start()
    }

    private suspend fun getIdToken(): String {
        return try {
            FirebaseAuth.getInstance().currentUser?.getIdToken(false)?.await()?.token ?: ""
        } catch (e: Exception) {
            Log.e("ScratchCard", "Failed to get ID token", e)
            ""
        }
    }

    private fun getDeviceID(): String = securePrefs.getString("deviceID", "") ?: ""
    private fun getDeviceToken(): String = securePrefs.getString("deviceToken", "") ?: ""
    private fun isVpn(): Boolean = securePrefs.getBoolean("isVpn", false)
    private fun isSslProxy(): Boolean = securePrefs.getBoolean("isSslProxy", false)
}
