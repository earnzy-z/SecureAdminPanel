package com.earnzy.app.Activity

import android.animation.ValueAnimator
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.earnzy.app.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class ReferralActivity : AppCompatActivity() {

    private lateinit var referralCodeText: TextView
    private lateinit var copyButton: MaterialButton
    private lateinit var shareButton: MaterialButton
    private lateinit var earnedText: TextView
    private lateinit var totalReferralsText: TextView
    private lateinit var referralCodeCard: MaterialCardView
    private lateinit var totalReferralsCard: MaterialCardView
    private lateinit var earnedCard: MaterialCardView
    private lateinit var referralAnimation: LottieAnimationView
    private val referralCode = "EARNZY12345"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_referral)
        
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }
        
        initViews()
        setupAnimations()
        
        copyButton.setOnClickListener {
            copyReferralCode()
        }
        
        shareButton.setOnClickListener {
            shareReferralCode()
        }
    }
    
    private fun initViews() {
        referralCodeText = findViewById(R.id.referral_code_text)
        copyButton = findViewById(R.id.copy_button)
        shareButton = findViewById(R.id.share_button)
        earnedText = findViewById(R.id.earned_text)
        totalReferralsText = findViewById(R.id.total_referrals)
        referralCodeCard = findViewById(R.id.referral_code_card)
        totalReferralsCard = findViewById(R.id.total_referrals_card)
        earnedCard = findViewById(R.id.earned_card)
        referralAnimation = findViewById(R.id.referral_animation)
        
        referralCodeText.text = referralCode
    }
    
    private fun setupAnimations() {
        // Animate main card entrance
        referralCodeCard.alpha = 0f
        referralCodeCard.scaleX = 0.9f
        referralCodeCard.scaleY = 0.9f
        referralCodeCard.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(500)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
        
        // Animate stats cards with stagger
        animateStatsCard(totalReferralsCard, 100L)
        animateStatsCard(earnedCard, 200L)
        
        // Animate counters
        val totalReferrals = 12 // Sample data
        val coinsEarned = 250 // Sample data
        
        animateCounter(totalReferralsText, totalReferrals)
        animateCounter(earnedText, coinsEarned)
    }
    
    private fun animateStatsCard(card: MaterialCardView, delay: Long) {
        card.alpha = 0f
        card.translationY = 50f
        card.scaleX = 0.9f
        card.scaleY = 0.9f
        card.animate()
            .alpha(1f)
            .translationY(0f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(500)
            .setStartDelay(delay)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }
    
    private fun animateCounter(textView: TextView, targetValue: Int) {
        val animator = ValueAnimator.ofInt(0, targetValue)
        animator.duration = 1000
        animator.addUpdateListener { animation ->
            textView.text = animation.animatedValue.toString()
        }
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.startDelay = 300
        animator.start()
    }

    private fun copyReferralCode() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Referral Code", referralCode)
        clipboard.setPrimaryClip(clip)
        
        // Play confetti animation
        referralAnimation.playAnimation()
        
        // Add bounce animation to copy button
        copyButton.animate()
            .scaleX(0.9f)
            .scaleY(0.9f)
            .setDuration(100)
            .withEndAction {
                copyButton.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start()
            }
            .start()
        
        Toast.makeText(this, "✨ Referral code copied!", Toast.LENGTH_SHORT).show()
    }

    private fun shareReferralCode() {
        val shareText = "🎉 Join Earnzy and start earning rewards! 💰\n\n" +
                "Use my referral code: $referralCode\n\n" +
                "📱 Download now: https://earnzy.app\n" +
                "🎁 Get bonus coins on signup!"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        
        // Add bounce animation to share button
        shareButton.animate()
            .scaleX(0.9f)
            .scaleY(0.9f)
            .setDuration(100)
            .withEndAction {
                shareButton.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start()
            }
            .start()
        
        startActivity(Intent.createChooser(intent, "Share via"))
    }
}
