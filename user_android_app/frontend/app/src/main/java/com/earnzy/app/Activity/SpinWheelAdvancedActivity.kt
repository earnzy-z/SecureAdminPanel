package com.earnzy.app.Activity

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.earnzy.app.R
import com.earnzy.app.databinding.ActivitySpinWheelAdvancedBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Advanced Spin Wheel Activity with rotation animations and confetti celebrations
 * Uses ViewBinding for type-safe view access
 */
class SpinWheelAdvancedActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySpinWheelAdvancedBinding
    private var remainingSpins = 3
    private var isSpinning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySpinWheelAdvancedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupClickListeners()
    }

    private fun setupUI() {
        updateSpinsRemaining()
        binding.confettiAnimation.visibility = View.GONE
    }

    private fun setupClickListeners() {
        binding.backButtonSpin.setOnClickListener {
            finish()
        }

        binding.spinButton.setOnClickListener {
            if (!isSpinning && remainingSpins > 0) {
                performSpin()
            } else if (remainingSpins == 0) {
                Toast.makeText(this, "No spins remaining!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.watchAdButton.setOnClickListener {
            // TODO: Integrate ad SDK
            Toast.makeText(this, "Watch ad to earn extra spin", Toast.LENGTH_SHORT).show()
            // Simulate ad watched
            remainingSpins++
            updateSpinsRemaining()
        }
    }

    private fun performSpin() {
        if (isSpinning) return

        isSpinning = true
        remainingSpins--
        updateSpinsRemaining()

        binding.spinButton.isEnabled = false
        binding.spinButton.text = "SPINNING..."

        // Calculate random rotation (multiple full rotations + random segment)
        val randomDegrees = Random.nextInt(360)
        val totalRotation = 1800f + randomDegrees // 5 full rotations + random

        // Animate wheel rotation
        val rotationAnimator = ObjectAnimator.ofFloat(
            binding.spinWheel,
            View.ROTATION,
            0f,
            totalRotation
        ).apply {
            duration = 3000
            interpolator = DecelerateInterpolator(2f)
        }

        rotationAnimator.start()

        // Show reward after spin completes
        lifecycleScope.launch {
            delay(3200) // Wait for animation + small delay
            
            // Calculate prize based on final position
            val prizeAmount = calculatePrize(randomDegrees)
            showReward(prizeAmount)
            
            // Reset for next spin
            isSpinning = false
            binding.spinButton.isEnabled = remainingSpins > 0
            binding.spinButton.text = if (remainingSpins > 0) "SPIN NOW!" else "NO SPINS LEFT"
        }
    }

    private fun calculatePrize(degrees: Int): Int {
        // Simple prize calculation based on segment
        return when (degrees) {
            in 0..45 -> 50
            in 46..90 -> 100
            in 91..135 -> 150
            in 136..180 -> 200
            in 181..225 -> 250
            in 226..270 -> 500
            in 271..315 -> 1000
            else -> 2000
        }
    }

    private fun showReward(amount: Int) {
        // Show confetti animation
        binding.confettiAnimation.apply {
            visibility = View.VISIBLE
            playAnimation()
        }

        // Hide confetti after animation
        lifecycleScope.launch {
            delay(2000)
            binding.confettiAnimation.visibility = View.GONE
        }

        // Show reward dialog
        Toast.makeText(
            this,
            "🎉 Congratulations! You won $amount coins!",
            Toast.LENGTH_LONG
        ).show()

        // TODO: Update user balance in backend
    }

    private fun updateSpinsRemaining() {
        binding.spinsRemainingText.text = "$remainingSpins Spins Left"
    }

    override fun onDestroy() {
        super.onDestroy()
        // Reset wheel rotation
        binding.spinWheel.rotation = 0f
    }
}
