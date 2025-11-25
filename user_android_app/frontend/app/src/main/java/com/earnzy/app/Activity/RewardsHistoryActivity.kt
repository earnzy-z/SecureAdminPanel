package com.earnzy.app.Activity

import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.earnzy.app.R
import com.earnzy.app.adapters.TransactionAdapter
import com.earnzy.app.models.Transaction
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textview.MaterialTextView

class RewardsHistoryActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var tabLayout: TabLayout
    private lateinit var historyRecycler: RecyclerView
    private lateinit var summaryCard: MaterialCardView
    private lateinit var totalEarnedText: MaterialTextView
    private lateinit var totalWithdrawnText: MaterialTextView
    private lateinit var balanceText: MaterialTextView
    private lateinit var celebrationAnimation: LottieAnimationView
    
    companion object {
        private const val CELEBRATION_THRESHOLD = 500
    }
    
    private val allTransactions = listOf(
        Transaction("🎁 Daily Bonus Claimed", "+100 ₹", "Today, 10:30 AM", "credit"),
        Transaction("🎡 Spin & Win", "+50 ₹", "Today, 09:15 AM", "credit"),
        Transaction("📊 Survey Completed", "+500 ₹", "Yesterday", "credit"),
        Transaction("💳 Withdrawal to PayPal", "-1000 ₹", "2 days ago", "debit"),
        Transaction("👥 Referral Bonus", "+200 ₹", "3 days ago", "credit"),
        Transaction("🎬 Video Ads Watched", "+75 ₹", "3 days ago", "credit"),
        Transaction("📱 App Install Reward", "+300 ₹", "4 days ago", "credit"),
        Transaction("🎰 Scratch Card Win", "+150 ₹", "5 days ago", "credit"),
        Transaction("💎 Offerwall Completed", "+450 ₹", "5 days ago", "credit"),
        Transaction("💸 Withdrawal to UPI", "-500 ₹", "6 days ago", "debit"),
        Transaction("🏆 Achievement Unlocked", "+250 ₹", "1 week ago", "credit"),
        Transaction("⭐ Rating Bonus", "+100 ₹", "1 week ago", "credit")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setupTransparentStatusBar()
        setContentView(R.layout.activity_rewards_history)
        
        toolbar = findViewById(R.id.toolbar)
        tabLayout = findViewById(R.id.tab_layout)
        historyRecycler = findViewById(R.id.history_recycler)
        summaryCard = findViewById(R.id.summary_card)
        totalEarnedText = findViewById(R.id.total_earned_text)
        totalWithdrawnText = findViewById(R.id.total_withdrawn_text)
        balanceText = findViewById(R.id.balance_text)
        celebrationAnimation = findViewById(R.id.celebration_animation)
        
        toolbar.setNavigationOnClickListener { finish() }
        
        setupTabs()
        updateSummary()
        loadTransactions("all")
        animateEntrance()
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
    
    private fun animateEntrance() {
        summaryCard.alpha = 0f
        summaryCard.scaleX = 0.9f
        summaryCard.scaleY = 0.9f
        summaryCard.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(500)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }
    
    private fun updateSummary() {
        val totalEarned = allTransactions.filter { it.type == "credit" }
            .sumOf { parseAmount(it.amount) }
        val totalWithdrawn = allTransactions.filter { it.type == "debit" }
            .sumOf { parseAmount(it.amount) }
        val balance = totalEarned - totalWithdrawn
        
        animateCounter(totalEarnedText, totalEarned, "₹")
        animateCounter(totalWithdrawnText, totalWithdrawn, "₹")
        animateCounter(balanceText, balance, "₹")
        
        // Show celebration if positive balance above threshold
        if (balance > CELEBRATION_THRESHOLD) {
            celebrationAnimation.visibility = View.VISIBLE
            celebrationAnimation.playAnimation()
        }
    }
    
    private fun parseAmount(amountStr: String): Int {
        return amountStr.replace("+", "")
            .replace("-", "")
            .replace(" ₹", "")
            .trim()
            .toIntOrNull() ?: 0
    }
    
    private fun animateCounter(textView: MaterialTextView, targetValue: Int, prefix: String) {
        val animator = ValueAnimator.ofInt(0, targetValue)
        animator.duration = 1000
        animator.addUpdateListener { animation ->
            textView.text = "$prefix${animation.animatedValue}"
        }
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.startDelay = 300
        animator.start()
    }

    private fun setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("All"))
        tabLayout.addTab(tabLayout.newTab().setText("Earned"))
        tabLayout.addTab(tabLayout.newTab().setText("Withdrawn"))
        
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> loadTransactions("all")
                    1 -> loadTransactions("earned")
                    2 -> loadTransactions("withdrawn")
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun loadTransactions(filter: String) {
        historyRecycler.layoutManager = LinearLayoutManager(this)
        
        val filteredTransactions = when (filter) {
            "earned" -> allTransactions.filter { it.type == "credit" }
            "withdrawn" -> allTransactions.filter { it.type == "debit" }
            else -> allTransactions
        }
        
        historyRecycler.adapter = TransactionAdapter(filteredTransactions)
        animateTransactionsList()
    }
    
    private fun animateTransactionsList() {
        historyRecycler.post {
            for (i in 0 until minOf(8, historyRecycler.adapter?.itemCount ?: 0)) {
                val viewHolder = historyRecycler.findViewHolderForAdapterPosition(i)
                viewHolder?.itemView?.let { view ->
                    view.alpha = 0f
                    view.translationX = -50f
                    view.animate()
                        .alpha(1f)
                        .translationX(0f)
                        .setDuration(400)
                        .setStartDelay(i * 50L)
                        .setInterpolator(AccelerateDecelerateInterpolator())
                        .start()
                }
            }
        }
    }
}
