package com.earnzy.app.Activity

import android.animation.ValueAnimator
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.airbnb.lottie.LottieAnimationView
import com.earnzy.app.R
import com.earnzy.app.adapters.AchievementAdapter
import com.earnzy.app.models.Achievement
import com.earnzy.app.network.FeaturesApiClient
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AchievementsActivity : AppCompatActivity() {

    private lateinit var achievementsRecycler: RecyclerView
    private lateinit var progressCard: MaterialCardView
    private lateinit var progressText: MaterialTextView
    private lateinit var progressPercentText: MaterialTextView
    private lateinit var celebrationAnimation: LottieAnimationView
    private lateinit var totalAchievementsText: MaterialTextView
    private lateinit var loadingProgress: ProgressBar
    private lateinit var securePrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievements)
        
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }
        
        achievementsRecycler = findViewById(R.id.achievements_recycler)
        progressCard = findViewById(R.id.progress_card)
        progressText = findViewById(R.id.progress_text)
        progressPercentText = findViewById(R.id.progress_percent)
        celebrationAnimation = findViewById(R.id.celebration_animation)
        totalAchievementsText = findViewById(R.id.total_achievements)
        loadingProgress = findViewById(R.id.loading_progress)
        
        initializeSecureStorage()
        loadAchievements()
        animateProgressCard()
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
            Log.e("AchievementsActivity", "Security setup failed", e)
        }
    }
    
    private fun loadAchievements() {
        loadingProgress.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    FeaturesApiClient.getAchievements(
                        this@AchievementsActivity,
                        getIdToken(),
                        getDeviceID(),
                        getDeviceToken(),
                        isVpn(),
                        isSslProxy()
                    )
                }
                
                if (response.getString("status") == "success") {
                    val achievementsArray = response.getJSONArray("achievements")
                    val summary = response.getJSONObject("summary")
                    
                    val achievements = mutableListOf<Achievement>()
                    for (i in 0 until achievementsArray.length()) {
                        val item = achievementsArray.getJSONObject(i)
                        achievements.add(
                         Achievement(
                           id = item.optString("id", "unknown_${i}"),
                           title = item.getString("name"),
                           description = item.getString("description"),
                           category = item.optString("category", "general"),
                           icon = R.drawable.ic_achievement,
                           unlocked = item.optBoolean("unlocked", false),
                           progress = item.optInt("progress", if (item.optBoolean("unlocked", false)) 1 else 0),
                           maxProgress = item.optInt("maxProgress", 1),
                           isCompleted = item.optBoolean("isCompleted", item.optBoolean("unlocked", false)),
                           reward = item.optInt("reward", 0)
                         )
                       )

                    }
                    
                    setupAchievements(achievements, summary)
                } else {
                    Toast.makeText(this@AchievementsActivity, "Failed to load achievements", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Log.e("AchievementsActivity", "Failed to load achievements", e)
                Toast.makeText(this@AchievementsActivity, "Error loading data", Toast.LENGTH_SHORT).show()
                finish()
            } finally {
                loadingProgress.visibility = View.GONE
            }
        }
    }
    
    private fun animateProgressCard() {
        progressCard.alpha = 0f
        progressCard.scaleX = 0.9f
        progressCard.scaleY = 0.9f
        progressCard.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(500)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    private fun setupAchievements(achievements: List<Achievement>, summary: org.json.JSONObject) {
        achievementsRecycler.layoutManager = GridLayoutManager(this, 2)
        
        val unlockedCount = summary.getInt("unlockedCount")
        val totalCount = summary.getInt("totalCount")
        val progressPercent = summary.getInt("progressPercent")
        val totalRewards = summary.getInt("totalRewards")
        
        // Animate progress counter
        animateCounter(progressText, unlockedCount, "$unlockedCount/$totalCount Unlocked")
        animateCounter(progressPercentText, progressPercent, "$progressPercent%")
        totalAchievementsText.text = "Total Rewards Earned: $totalRewards ₹"
        
        // Show celebration if high completion
        if (progressPercent >= 75) {
            celebrationAnimation.visibility = View.VISIBLE
            celebrationAnimation.playAnimation()
        }
        
        achievementsRecycler.adapter = AchievementAdapter(achievements)
        
        // Animate items with stagger
        achievementsRecycler.post {
            for (i in 0 until minOf(achievements.size, 6)) {
                val viewHolder = achievementsRecycler.findViewHolderForAdapterPosition(i)
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
                        .setStartDelay((i * 80).toLong())
                        .setInterpolator(AccelerateDecelerateInterpolator())
                        .start()
                }
            }
        }
    }
    
    private fun animateCounter(textView: MaterialTextView, targetValue: Int, finalText: String) {
        val animator = ValueAnimator.ofInt(0, targetValue)
        animator.duration = 1000
        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Int
            textView.text = if (finalText.contains("/")) {
                "$value/${finalText.split("/")[1]}"
            } else {
                "$value%"
            }
        }
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.startDelay = 300
        animator.start()
        
        // Set final text after animation
        textView.postDelayed({
            textView.text = finalText
        }, 1400)
    }
    
    // Helper methods for credentials
    private suspend fun getIdToken(): String = withContext(Dispatchers.IO) {
        FirebaseAuth.getInstance().currentUser?.getIdToken(false)?.await()?.token ?: ""
    }
    
    private fun getDeviceID(): String = securePrefs.getString("deviceID", "") ?: ""
    private fun getDeviceToken(): String = securePrefs.getString("deviceToken", "") ?: ""
    private fun isVpn(): Boolean = securePrefs.getBoolean("isVpn", false)
    private fun isSslProxy(): Boolean = securePrefs.getBoolean("isSslProxy", false)
}