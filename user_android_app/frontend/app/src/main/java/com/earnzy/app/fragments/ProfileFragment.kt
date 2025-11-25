package com.earnzy.app.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.earnzy.app.R
import com.earnzy.app.Activity.AchievementsActivity
import com.earnzy.app.Activity.LeaderboardActivity
import com.earnzy.app.Activity.ReferralActivity
import com.earnzy.app.Activity.SupportChatActivity
import com.earnzy.app.network.FeaturesApiClient
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.concurrent.CancellationException

class ProfileFragment : Fragment() {

    // Views
    private var profileImage: ImageView? = null
    private var nameText: MaterialTextView? = null
    private var emailText: MaterialTextView? = null
    private var referralCodeText: MaterialTextView? = null
    private var referralCard: MaterialCardView? = null
    private var supportCard: MaterialCardView? = null
    private var settingsCard: MaterialCardView? = null
    private var achievementsCard: MaterialCardView? = null
    private var leaderboardCard: MaterialCardView? = null
    private var logoutButton: MaterialButton? = null

    private lateinit var securePrefs: SharedPreferences
    private var referralCode = ""
    private var referralLink = ""

    private var loadJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile_professional, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeSecureStorage()
        setupViews(view)
        animateCardsEntrance()
        animateProfileWithAnimationUtils(view)
        loadProfileDataSafely()
    }
    
    private fun animateProfileWithAnimationUtils(view: View) {
        try {
            val headerBg = view.findViewById<View>(R.id.profile_avatar_text)?.parent?.parent
            val statsCards = view.findViewById<View>(R.id.profile_name_text)?.parent
            val menuSection = view.findViewById<View>(R.id.btn_logout)
            
            if (headerBg != null) com.earnzy.app.utils.AnimationUtils.slideUpIn(headerBg, delay = 0)
            if (statsCards != null) com.earnzy.app.utils.AnimationUtils.slideUpIn(statsCards, delay = 100)
            if (menuSection != null) com.earnzy.app.utils.AnimationUtils.slideUpIn(menuSection, delay = 200)
        } catch (e: Exception) {
            android.util.Log.e("ProfileFragment", "Animation error", e)
        }
    }

    override fun onResume() {
        super.onResume()
        loadProfileDataSafely()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadJob?.cancel() // Cancel any ongoing network call
        clearViews()
    }

    private fun initializeSecureStorage() {
        try {
            val masterKey = MasterKey.Builder(requireContext())
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            securePrefs = EncryptedSharedPreferences.create(
                requireContext(),
                "SecureEarnzyPrefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Failed to initialize secure storage", e)
        }
    }

    private fun setupViews(view: View) {
        profileImage = view.findViewById(R.id.profile_image)
        nameText = view.findViewById(R.id.profile_name)
        emailText = view.findViewById(R.id.profile_email)
        referralCodeText = view.findViewById(R.id.referral_code_text)
        referralCard = view.findViewById(R.id.referral_card)
        supportCard = view.findViewById(R.id.support_card)
        settingsCard = view.findViewById(R.id.settings_card)
        achievementsCard = view.findViewById(R.id.achievements_card)
        leaderboardCard = view.findViewById(R.id.leaderboard_card)
        logoutButton = view.findViewById(R.id.logout_button)

        referralCard?.setOnClickListener {
            copyReferralCode()
        }

        supportCard?.setOnClickListener {
            startActivity(Intent(requireContext(), SupportChatActivity::class.java))
        }

        settingsCard?.setOnClickListener {
            showSettingsDialog()
        }

        achievementsCard?.setOnClickListener {
            startActivity(Intent(requireContext(), AchievementsActivity::class.java))
        }

        leaderboardCard?.setOnClickListener {
            startActivity(Intent(requireContext(), LeaderboardActivity::class.java))
        }

        logoutButton?.setOnClickListener {
            showLogoutDialog()
        }
    }
    
    private fun animateCardsEntrance() {
        val cards = listOf(referralCard, achievementsCard, leaderboardCard, supportCard, settingsCard)
        cards.forEachIndexed { index, card ->
            card?.alpha = 0f
            card?.translationY = 50f
            card?.animate()
                ?.alpha(1f)
                ?.translationY(0f)
                ?.setDuration(400)
                ?.setStartDelay(index * 50L)
                ?.setInterpolator(AccelerateDecelerateInterpolator())
                ?.start()
        }
    }

    private fun loadProfileDataSafely() {
        if (!isAdded) return
        val ctx = requireContext()

        loadJob?.cancel()
        loadJob = viewLifecycleOwner.lifecycleScope.launch {
            if (!viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) return@launch
            
            try {
                val response = withContext(Dispatchers.IO) {
                    FeaturesApiClient.getUserProfile(
                        ctx,
                        getIdToken(),
                        getDeviceID(),
                        getDeviceToken(),
                        isVpn(),
                        isSslProxy()
                    )
                }

                if (!isAdded) return@launch

                if (response.optString("status") == "success") {
                    val profile = response.optJSONObject("user")
                    if (profile == null) {
                        Log.w("ProfileFragment", "User object is null in successful response")
                        loadDefaultProfile()
                        return@launch
                    }

                    nameText?.text = profile.optString("name", "User")
                    emailText?.text = profile.optString("email", "email@example.com")
                    referralCode = profile.optString("personalReferralCode", "")
                    referralLink = profile.optString("referralLink", "")
                    referralCodeText?.text = referralCode

                    val photoUrl = profile.optString("photo", "")
                    profileImage?.let {
                        if (photoUrl.isNotEmpty()) {
                            Glide.with(this@ProfileFragment)
                                .load(photoUrl)
                                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                                .placeholder(R.drawable.ic_launcher_background)
                                .into(it)
                        } else {
                            it.setImageResource(R.drawable.ic_launcher_background)
                        }
                    }
                } else {
                    val message = response.optString("message", "Failed to load profile.")
                    Log.e("ProfileFragment", "API Error: $message")
                    Toast.makeText(ctx, "Error: $message", Toast.LENGTH_SHORT).show()
                    loadDefaultProfile()
                }
            } catch (e: CancellationException) {
                // This is expected when the view is destroyed; do nothing.
                Log.d("ProfileFragment", "Profile load cancelled.")
            } catch (e: Exception) {
                if (isAdded) {
                    Log.e("ProfileFragment", "Exception while loading profile", e)
                    loadDefaultProfile()
                }
            }
        }
    }

    private fun loadDefaultProfile() {
        if (!isAdded) return

        val user = FirebaseAuth.getInstance().currentUser
        nameText?.text = user?.displayName ?: "User"
        emailText?.text = user?.email ?: "email@example.com"

        profileImage?.let { imageView ->
            user?.photoUrl?.let { uri ->
                Glide.with(this@ProfileFragment)
                    .load(uri.toString())
                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(imageView)
            } ?: run {
                imageView.setImageResource(R.drawable.ic_launcher_background)
            }
        }
    }
    
    private fun copyReferralCode() {
        if (!isAdded) return
        val ctx = requireContext()

        if (referralCode.isEmpty()) {
            Toast.makeText(ctx, "Referral code not available.", Toast.LENGTH_SHORT).show()
            return
        }

        val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Referral Code", referralCode)
        clipboard.setPrimaryClip(clip)

        Toast.makeText(ctx, "Referral code copied!", Toast.LENGTH_SHORT).show()

        referralCard?.animate()
            ?.scaleX(1.05f)
            ?.scaleY(1.05f)
            ?.setDuration(150)
            ?.withEndAction {
                referralCard?.animate()
                    ?.scaleX(1f)
                    ?.scaleY(1f)
                    ?.setDuration(150)
                    ?.start()
            }
            ?.start()
    }

    private fun showSettingsDialog() {
        if (!isAdded) return
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Settings")
            .setItems(
                arrayOf(
                    "Notifications",
                    "Theme",
                    "Privacy",
                    "About App",
                    "Terms & Privacy Policy",
                    "Help & FAQ"
                )
            ) { _, which ->
                if (!isAdded) return@setItems
                when (which) {
                    0 -> Toast.makeText(requireContext(), "Notification settings", Toast.LENGTH_SHORT).show()
                    1 -> Toast.makeText(requireContext(), "Theme settings", Toast.LENGTH_SHORT).show()
                    2 -> Toast.makeText(requireContext(), "Privacy settings", Toast.LENGTH_SHORT).show()
                    3 -> showAboutDialog()
                    4 -> Toast.makeText(requireContext(), "Opening T&C...", Toast.LENGTH_SHORT).show()
                    5 -> startActivity(Intent(requireContext(), SupportChatActivity::class.java))
                }
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showAboutDialog() {
        if(!isAdded) return
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("About Earnzy")
            .setMessage("Earnzy - Earn Rewards Daily\n\nVersion: 1.0.0\n\n© 2024 Earnzy. All rights reserved.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showLogoutDialog() {
        if(!isAdded) return
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                FirebaseAuth.getInstance().signOut()
                // You might want to navigate to a login screen instead of just finishing
                activity?.let {
                    it.finish()
                    // val intent = Intent(it, LoginActivity::class.java)
                    // intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    // startActivity(intent)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun clearViews() {
        profileImage = null
        nameText = null
        emailText = null
        referralCodeText = null
        referralCard = null
        supportCard = null
        settingsCard = null
        achievementsCard = null
        leaderboardCard = null
        logoutButton = null
    }

    // Helper methods
    private suspend fun getIdToken(): String = withContext(Dispatchers.IO) {
        try {
            FirebaseAuth.getInstance().currentUser?.getIdToken(false)?.await()?.token ?: ""
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Failed to get ID token", e)
            ""
        }
    }

    private fun getDeviceID(): String = securePrefs.getString("deviceID", "") ?: ""
    private fun getDeviceToken(): String = securePrefs.getString("deviceToken", "") ?: ""
    private fun isVpn(): Boolean = securePrefs.getBoolean("isVpn", false)
    private fun isSslProxy(): Boolean = securePrefs.getBoolean("isSslProxy", false)
}