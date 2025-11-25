package com.earnzy.app.Activity

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.earnzy.app.R
import com.earnzy.app.databinding.ActivityScratchCardAdvancedBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Advanced Scratch Card Activity with reveal mechanics and progress tracking
 * Uses ViewBinding for type-safe view access
 */
class ScratchCardAdvancedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScratchCardAdvancedBinding
    private var remainingCards = 2
    private var scratchProgress = 0
    private var prizeAmount = 0
    private var isRevealed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScratchCardAdvancedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupClickListeners()
        generateNewCard()
    }

    private fun setupUI() {
        updateCardsRemaining()
        binding.claimButton.visibility = View.GONE
        binding.celebrationOverlay.visibility = View.GONE
        binding.scratchPercentage.visibility = View.GONE
        binding.scratchProgress.visibility = View.GONE
    }

    private fun setupClickListeners() {
        binding.backButtonScratch.setOnClickListener {
            finish()
        }

        binding.revealButton.setOnClickListener {
            revealPrize()
        }

        binding.claimButton.setOnClickListener {
            claimPrize()
        }

        // Setup scratch detection
        binding.scratchOverlay.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    handleScratch(event.x, event.y)
                    true
                }
                else -> false
            }
        }
    }

    private fun generateNewCard() {
        // Generate random prize amount
        prizeAmount = Random.nextInt(100, 2001)
        binding.prizeAmount.text = prizeAmount.toString()
        
        // Reset scratch state
        scratchProgress = 0
        isRevealed = false
        binding.scratchOverlay.visibility = View.VISIBLE
        binding.scratchPercentage.visibility = View.VISIBLE
        binding.scratchProgress.visibility = View.VISIBLE
        binding.claimButton.visibility = View.GONE
        
        updateScratchProgress()
    }

    private fun handleScratch(x: Float, y: Float) {
        if (isRevealed) return

        // Simulate scratch progress (in real app, would use custom view)
        scratchProgress += 5
        scratchProgress = scratchProgress.coerceAtMost(100)
        
        updateScratchProgress()

        // Auto-reveal when 70% scratched
        if (scratchProgress >= 70 && !isRevealed) {
            lifecycleScope.launch {
                delay(200)
                revealPrize()
            }
        }
    }

    private fun updateScratchProgress() {
        binding.scratchProgress.progress = scratchProgress
        binding.scratchPercentage.text = "$scratchProgress% Scratched"
    }

    private fun revealPrize() {
        if (isRevealed) return
        
        isRevealed = true
        scratchProgress = 100
        updateScratchProgress()

        // Hide overlay to reveal prize
        binding.scratchOverlay.animate()
            .alpha(0f)
            .setDuration(500)
            .withEndAction {
                binding.scratchOverlay.visibility = View.GONE
            }
            .start()

        // Show celebration
        binding.celebrationOverlay.apply {
            visibility = View.VISIBLE
            playAnimation()
        }

        // Show claim button
        binding.claimButton.visibility = View.VISIBLE
        binding.revealButton.visibility = View.GONE
        binding.scratchPercentage.visibility = View.GONE
        binding.scratchProgress.visibility = View.GONE

        // Hide celebration after animation
        lifecycleScope.launch {
            delay(2000)
            binding.celebrationOverlay.visibility = View.GONE
        }
    }

    private fun claimPrize() {
        Toast.makeText(
            this,
            "🎉 Claimed $prizeAmount coins!",
            Toast.LENGTH_LONG
        ).show()

        // TODO: Update user balance in backend

        remainingCards--
        updateCardsRemaining()

        if (remainingCards > 0) {
            // Load next card
            lifecycleScope.launch {
                delay(500)
                resetCard()
                generateNewCard()
            }
        } else {
            // No more cards
            Toast.makeText(this, "No more cards available!", Toast.LENGTH_SHORT).show()
            lifecycleScope.launch {
                delay(1500)
                finish()
            }
        }
    }

    private fun resetCard() {
        binding.scratchOverlay.alpha = 1f
        binding.scratchOverlay.visibility = View.VISIBLE
        binding.revealButton.visibility = View.VISIBLE
        binding.claimButton.visibility = View.GONE
    }

    private fun updateCardsRemaining() {
        binding.cardsRemaining.text = "$remainingCards Cards Left"
    }
}
