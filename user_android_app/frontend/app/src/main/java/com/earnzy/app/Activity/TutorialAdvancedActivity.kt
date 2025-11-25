package com.earnzy.app.Activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.earnzy.app.R
import com.earnzy.app.adapters.TutorialPagerAdapter
import com.earnzy.app.databinding.ActivityTutorialAdvancedBinding
import com.earnzy.app.models.TutorialPage

/**
 * Advanced Tutorial Activity with ViewPager2 and smooth animations
 * Uses ViewBinding for type-safe view access
 */
class TutorialAdvancedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTutorialAdvancedBinding
    private lateinit var tutorialAdapter: TutorialPagerAdapter
    private val tutorialPages = mutableListOf<TutorialPage>()
    private var currentPage = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTutorialAdvancedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupTutorialPages()
        setupViewPager()
        setupClickListeners()
        updateButtonStates()
    }

    private fun setupTutorialPages() {
        tutorialPages.addAll(
            listOf(
                TutorialPage(
                    title = "Earn Coins Daily",
                    description = "Complete tasks, watch videos, and participate in offers to earn coins every day.",
                    animationRes = R.raw.tutorial1,
                    showFeatures = false
                ),
                TutorialPage(
                    title = "Exciting Rewards",
                    description = "Spin the wheel, scratch cards, and unlock achievements for bonus rewards!",
                    animationRes = R.raw.tutorial2,
                    showFeatures = false
                ),
                TutorialPage(
                    title = "Instant Withdrawals",
                    description = "Cash out your earnings anytime with multiple payment methods available.",
                    animationRes = R.raw.tutorial3,
                    showFeatures = true,
                    features = listOf(
                        "Daily bonus rewards",
                        "Instant withdrawals",
                        "Referral bonuses"
                    )
                )
            )
        )
    }

    private fun setupViewPager() {
        tutorialAdapter = TutorialPagerAdapter(tutorialPages)
        binding.tutorialViewpager.adapter = tutorialAdapter
        
        binding.tutorialViewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentPage = position
                updateButtonStates()
                updateDots()
            }
        })
    }

    private fun setupClickListeners() {
        binding.btnSkip.setOnClickListener {
            finishTutorial()
        }

        binding.btnPrevious.setOnClickListener {
            if (currentPage > 0) {
                binding.tutorialViewpager.currentItem = currentPage - 1
            }
        }

        binding.btnNext.setOnClickListener {
            if (currentPage < tutorialPages.size - 1) {
                binding.tutorialViewpager.currentItem = currentPage + 1
            } else {
                finishTutorial()
            }
        }
    }

    private fun updateButtonStates() {
        // Previous button visibility
        binding.btnPrevious.visibility = if (currentPage > 0) View.VISIBLE else View.GONE

        // Next button text
        binding.btnNext.text = if (currentPage == tutorialPages.size - 1) {
            "Get Started"
        } else {
            "Next"
        }

        // Skip button visibility (hide on last page)
        binding.btnSkip.visibility = if (currentPage == tutorialPages.size - 1) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    private fun updateDots() {
        // TODO: Update dot indicators programmatically
        // For now, dots are static in XML
    }

    private fun finishTutorial() {
        // Mark tutorial as completed
        getSharedPreferences("app_prefs", MODE_PRIVATE)
            .edit()
            .putBoolean("tutorial_completed", true)
            .apply()

        // Navigate to main activity
        // TODO: Replace with actual main activity intent
        finish()
    }
}
