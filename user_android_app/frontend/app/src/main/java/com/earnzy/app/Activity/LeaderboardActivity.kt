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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.earnzy.app.R
import com.earnzy.app.adapters.LeaderboardAdapter
import com.earnzy.app.models.LeaderboardEntry
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LeaderboardActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var leaderboardRecycler: RecyclerView
    private lateinit var loadingProgress: ProgressBar
    private lateinit var myRankCard: MaterialCardView
    private lateinit var myRankText: MaterialTextView
    private lateinit var myCoinsText: MaterialTextView
    private lateinit var securePrefs: SharedPreferences
    private val entries = mutableListOf<LeaderboardEntry>()
    private lateinit var adapter: LeaderboardAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setupTransparentStatusBar()
        setContentView(R.layout.activity_leaderboard)
        
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }
        
        initializeSecureStorage()
        initViews()
        setupLeaderboard("weekly")
        
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> setupLeaderboard("daily")
                    1 -> setupLeaderboard("weekly")
                    2 -> setupLeaderboard("monthly")
                    3 -> setupLeaderboard("alltime")
                    4 -> setupLeaderboard("referral")
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
        
        // Animate entrance
        animateCardEntrance()
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
            Log.e("Leaderboard", "Security setup failed", e)
        }
    }
    
    private fun initViews() {
        tabLayout = findViewById(R.id.tab_layout)
        leaderboardRecycler = findViewById(R.id.leaderboard_recycler)
        loadingProgress = findViewById(R.id.loading_progress)
        myRankCard = findViewById(R.id.my_rank_card)
        myRankText = findViewById(R.id.my_rank_text)
        myCoinsText = findViewById(R.id.my_coins_text)
        
        leaderboardRecycler.layoutManager = LinearLayoutManager(this)
        adapter = LeaderboardAdapter(entries)
        leaderboardRecycler.adapter = adapter
    }
    
    private fun animateCardEntrance() {
        myRankCard.alpha = 0f
        myRankCard.scaleX = 0.9f
        myRankCard.scaleY = 0.9f
        myRankCard.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(500)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    private fun setupLeaderboard(period: String) {
        loadingProgress.visibility = View.VISIBLE
        
        // For now, use sample data. Can be enhanced with backend API
        entries.clear()
        
        // Generate sample leaderboard with realistic data
        if (period == "referral") {
            // Referral-specific leaderboard
            entries.addAll(listOf(
                LeaderboardEntry(1, "👑 Refer Champion", 156, R.drawable.ic_launcher_background),
                LeaderboardEntry(2, "🥈 Network Master", 128, R.drawable.ic_launcher_background),
                LeaderboardEntry(3, "🥉 Social Star", 98, R.drawable.ic_launcher_background),
                LeaderboardEntry(4, "Influencer Pro ⭐", 87, 0),
                LeaderboardEntry(5, "Share Expert 📱", 76, 0),
                LeaderboardEntry(6, "Viral Marketer 💎", 65, 0),
                LeaderboardEntry(7, "Friend Magnet 👥", 58, 0),
                LeaderboardEntry(8, "Link Sharer 🔗", 52, 0),
                LeaderboardEntry(9, "Growth Hacker 📈", 47, 0),
                LeaderboardEntry(10, "Ambassador 🎖️", 42, 0),
                LeaderboardEntry(11, "Promoter 📢", 38, 0),
                LeaderboardEntry(12, "Connector 🤝", 34, 0),
                LeaderboardEntry(13, "Networker 🌐", 30, 0),
                LeaderboardEntry(14, "Inviter 💌", 27, 0),
                LeaderboardEntry(15, "Spreader ⚡", 24, 0)
            ))
        } else {
            entries.addAll(listOf(
                LeaderboardEntry(1, "👑 Champion Player", 25000, R.drawable.ic_launcher_background),
                LeaderboardEntry(2, "🥈 Silver Star", 18500, R.drawable.ic_launcher_background),
                LeaderboardEntry(3, "🥉 Bronze Winner", 15000, R.drawable.ic_launcher_background),
                LeaderboardEntry(4, "Rising Star ⭐", 12500, 0),
                LeaderboardEntry(5, "Pro Gamer 🎮", 11200, 0),
                LeaderboardEntry(6, "Elite Earner 💎", 10800, 0),
                LeaderboardEntry(7, "Coin Master 💰", 9500, 0),
                LeaderboardEntry(8, "Daily Warrior ⚔️", 8800, 0),
                LeaderboardEntry(9, "Task Hunter 🎯", 8200, 0),
                LeaderboardEntry(10, "Spin Expert 🎡", 7500, 0),
                LeaderboardEntry(11, "Bonus Collector 🎁", 7000, 0),
                LeaderboardEntry(12, "Scratch Master 🎫", 6500, 0),
                LeaderboardEntry(13, "Referral King 👥", 6000, 0),
                LeaderboardEntry(14, "Lucky Player 🍀", 5500, 0),
                LeaderboardEntry(15, "Earner Pro 💵", 5000, 0)
            ))
        }
        
        adapter.notifyDataSetChanged()
        
        // Animate list items with stagger effect
        animateListItemsStagger()
        
        // Set my rank (sample data)
        val myRank = kotlin.random.Random.nextInt(5, 50)
        val myCoins = if (period == "referral") {
            kotlin.random.Random.nextInt(10, 100)
        } else {
            kotlin.random.Random.nextInt(2000, 10000)
        }
        
        myRankText.text = "Your Rank: #$myRank"
        animateCounter(myCoinsText, myCoins, if (period == "referral") "Referrals: " else "Your Coins: ₹")
        
        loadingProgress.visibility = View.GONE
    }
    
    private fun animateListItems() {
        for (i in 0 until minOf(entries.size, 5)) {
            val viewHolder = leaderboardRecycler.findViewHolderForAdapterPosition(i)
            viewHolder?.itemView?.let { view ->
                view.alpha = 0f
                view.translationX = -100f
                view.animate()
                    .alpha(1f)
                    .translationX(0f)
                    .setDuration(300)
                    .setStartDelay(i * 50L)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .start()
            }
        }
    }
    
    private fun animateListItemsStagger() {
        leaderboardRecycler.post {
            for (i in 0 until minOf(entries.size, 10)) {
                val viewHolder = leaderboardRecycler.findViewHolderForAdapterPosition(i)
                viewHolder?.itemView?.let { view ->
                    view.alpha = 0f
                    view.translationY = 50f
                    view.scaleX = 0.9f
                    view.scaleY = 0.9f
                    view.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(400)
                        .setStartDelay(i * 60L)
                        .setInterpolator(AccelerateDecelerateInterpolator())
                        .start()
                }
            }
        }
    }
    
    private fun animateCounter(textView: MaterialTextView, value: Int, prefix: String = "") {
        val animator = ValueAnimator.ofInt(0, value)
        animator.duration = 1000
        animator.addUpdateListener { animation ->
            textView.text = "$prefix${animation.animatedValue}"
        }
        animator.start()
    }
    
    // Helper methods
    private suspend fun getIdToken(): String {
        return try {
            FirebaseAuth.getInstance().currentUser?.getIdToken(false)?.await()?.token ?: ""
        } catch (e: Exception) {
            Log.e("Leaderboard", "Failed to get ID token", e)
            ""
        }
    }
    
    private fun getDeviceID(): String = securePrefs.getString("deviceID", "") ?: ""
    private fun getDeviceToken(): String = securePrefs.getString("deviceToken", "") ?: ""
    private fun isVpn(): Boolean = securePrefs.getBoolean("isVpn", false)
    private fun isSslProxy(): Boolean = securePrefs.getBoolean("isSslProxy", false)
}
