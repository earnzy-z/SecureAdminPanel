package com.earnzy.app.Activity

import android.animation.ValueAnimator
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.airbnb.lottie.LottieAnimationView
import com.earnzy.app.R
import com.earnzy.app.adapters.OfferAdapter
import com.earnzy.app.models.Offer
import com.earnzy.app.network.FeaturesApiClient
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

class OfferwallActivity : AppCompatActivity() {

    private lateinit var offersRecycler: RecyclerView
    private lateinit var statsCard: MaterialCardView
    private lateinit var totalOffersText: MaterialTextView
    private lateinit var completedText: MaterialTextView
    private lateinit var earnedText: MaterialTextView
    private lateinit var loadingProgress: ProgressBar
    private lateinit var celebrationAnimation: LottieAnimationView
    private lateinit var securePrefs: SharedPreferences
    private val offers = mutableListOf<Offer>()
    private lateinit var adapter: OfferAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_offerwall)
        
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }
        
        initializeSecureStorage()
        initViews()
        loadOffersFromJson()
        animateEntrance()
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
            Log.e("OfferwallActivity", "Security setup failed", e)
        }
    }
    
    private fun initViews() {
        offersRecycler = findViewById(R.id.offers_recycler)
        statsCard = findViewById(R.id.stats_card)
        totalOffersText = findViewById(R.id.total_offers_text)
        completedText = findViewById(R.id.completed_text)
        earnedText = findViewById(R.id.earned_text)
        loadingProgress = findViewById(R.id.loading_progress)
        celebrationAnimation = findViewById(R.id.celebration_animation)
        
        offersRecycler.layoutManager = LinearLayoutManager(this)
        adapter = OfferAdapter(offers) { offer ->
            handleOfferClick(offer)
        }
        offersRecycler.adapter = adapter
    }
    
    private fun animateEntrance() {
        statsCard.alpha = 0f
        statsCard.scaleX = 0.9f
        statsCard.scaleY = 0.9f
        statsCard.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(500)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }
    
    private fun loadOffersFromJson() {
        loadingProgress.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                // Try to load from backend API first
                val response = FeaturesApiClient.getOfferwall(
                    this@OfferwallActivity,
                    getIdToken(),
                    getDeviceID(),
                    getDeviceToken(),
                    isVpn(),
                    isSslProxy()
                )
                
                if (response.getString("status") == "success") {
                    val offersArray = response.getJSONArray("offers")
                    parseOffers(offersArray)
                } else {
                    loadDefaultOffers()
                }
            } catch (e: Exception) {
                Log.e("OfferwallActivity", "Failed to load offers from API", e)
                loadDefaultOffers()
            } finally {
                loadingProgress.visibility = View.GONE
                updateStats()
                animateOffersList()
            }
        }
    }
    
    private fun parseOffers(offersArray: JSONArray) {
        offers.clear()
        for (i in 0 until offersArray.length()) {
            val offerObj = offersArray.getJSONObject(i)
            val offer = Offer(
                title = offerObj.getString("title"),
                reward = offerObj.getString("reward"),
                description = offerObj.getString("description"),
                link = offerObj.getString("link"),
                icon = getIconResource(offerObj.optString("iconType", "install"))
            )
            offers.add(offer)
        }
        adapter.notifyDataSetChanged()
    }
    
    private fun loadDefaultOffers() {
        offers.clear()
        offers.addAll(listOf(
            Offer("🎮 Install Game App", "500 ₹", "Complete Level 5", "https://example.com", R.drawable.ic_install),
            Offer("📝 Sign up for Service", "1000 ₹", "Complete Registration", "https://example.com", R.drawable.ic_signup),
            Offer("🎬 Watch Video Ad", "50 ₹", "Watch full video", "https://example.com", R.drawable.ic_video),
            Offer("📊 Take Survey", "300 ₹", "5 minutes survey", "https://example.com", R.drawable.ic_survey),
            Offer("🌐 Download Browser", "750 ₹", "Install and use", "https://example.com", R.drawable.ic_install),
            Offer("🛍️ Try Shopping App", "600 ₹", "Make first purchase", "https://example.com", R.drawable.ic_shopping),
            Offer("📧 Subscribe Newsletter", "100 ₹", "Verify email", "https://example.com", R.drawable.ic_email),
            Offer("👤 Complete Profile", "200 ₹", "Add all details", "https://example.com", R.drawable.ic_profile),
            Offer("⭐ Rate App", "150 ₹", "Give us 5 stars", "https://example.com", R.drawable.ic_star),
            Offer("📱 Install Social App", "400 ₹", "Create account", "https://example.com", R.drawable.ic_install)
        ))
        adapter.notifyDataSetChanged()
    }
    
    private fun getIconResource(iconType: String): Int {
        return when (iconType) {
            "install" -> R.drawable.ic_install
            "signup" -> R.drawable.ic_signup
            "video" -> R.drawable.ic_video
            "survey" -> R.drawable.ic_survey
            "shopping" -> R.drawable.ic_shopping
            "email" -> R.drawable.ic_email
            "profile" -> R.drawable.ic_profile
            "star" -> R.drawable.ic_star
            else -> R.drawable.ic_install
        }
    }
    
    private fun updateStats() {
        val totalOffers = offers.size
        val completed = 3 // Sample data - would come from backend
        val totalEarned = 1850 // Sample data
        
        animateCounter(totalOffersText, totalOffers, "")
        animateCounter(completedText, completed, "")
        animateCounter(earnedText, totalEarned, "₹")
        
        // Show celebration if good completion rate
        if (completed >= totalOffers / 2) {
            celebrationAnimation.visibility = View.VISIBLE
            celebrationAnimation.playAnimation()
        }
    }
    
    private fun animateCounter(textView: MaterialTextView, targetValue: Int, prefix: String) {
        val animator = ValueAnimator.ofInt(0, targetValue)
        animator.duration = 1000
        animator.addUpdateListener { animation ->
            textView.text = if (prefix.isEmpty()) {
                "${animation.animatedValue}"
            } else {
                "$prefix${animation.animatedValue}"
            }
        }
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.startDelay = 300
        animator.start()
    }
    
    private fun animateOffersList() {
        offersRecycler.post {
            for (i in 0 until minOf(offers.size, 8)) {
                val viewHolder = offersRecycler.findViewHolderForAdapterPosition(i)
                viewHolder?.itemView?.let { view ->
                    view.alpha = 0f
                    view.translationX = -50f
                    view.scaleX = 0.95f
                    view.scaleY = 0.95f
                    view.animate()
                        .alpha(1f)
                        .translationX(0f)
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
    
    private fun handleOfferClick(offer: Offer) {
        // Animate button press
        Toast.makeText(this, "Opening: ${offer.title}", Toast.LENGTH_SHORT).show()
        
        // Open offer link
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(offer.link))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to open offer", Toast.LENGTH_SHORT).show()
        }
    }
    
    // Helper methods
    private suspend fun getIdToken(): String {
        return try {
            FirebaseAuth.getInstance().currentUser?.getIdToken(false)?.await()?.token ?: ""
        } catch (e: Exception) {
            ""
        }
    }
    
    private fun getDeviceID(): String = securePrefs.getString("deviceID", "") ?: ""
    private fun getDeviceToken(): String = securePrefs.getString("deviceToken", "") ?: ""
    private fun isVpn(): Boolean = securePrefs.getBoolean("isVpn", false)
    private fun isSslProxy(): Boolean = securePrefs.getBoolean("isSslProxy", false)
}
