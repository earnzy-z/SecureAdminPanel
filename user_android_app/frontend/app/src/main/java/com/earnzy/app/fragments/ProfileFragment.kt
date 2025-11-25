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
import com.earnzy.app.utils.AnimationUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.concurrent.CancellationException

class ProfileFragment : Fragment() {

    private lateinit var securePrefs: SharedPreferences
    private var referralCode = ""
    private var referralLink = ""
    private var loadJob: Job? = null
    
    // Main views
    private lateinit var profileNameText: MaterialTextView
    private lateinit var profileEmailText: MaterialTextView
    private lateinit var profileAvatarText: MaterialTextView
    private lateinit var btnLogout: MaterialButton
    
    // Menu cards
    private lateinit var achievementsCard: MaterialCardView
    private lateinit var leaderboardCard: MaterialCardView
    private lateinit var referralCard: MaterialCardView
    private lateinit var supportCard: MaterialCardView
    private lateinit var settingsCard: MaterialCardView

    override fun onCreateView(
        inflater: LayoutInflater, 
        container: ViewGroup?, 
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile_professional, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeSecureStorage()
        initViews(view)
        animateEntranceSmooth(view)
        loadProfileData()
    }
    
    private fun animateEntranceSmooth(view: View) {
        try {
            val headerSection = view.findViewById<MaterialTextView>(R.id.profile_avatar_text)
            val nameSection = view.findViewById<MaterialTextView>(R.id.profile_name_text)
            val logoutBtn = view.findViewById<MaterialButton>(R.id.btn_logout)
            
            if (headerSection != null) {
                AnimationUtils.slideUpIn(headerSection, delay = 0)
            }
            if (nameSection != null) {
                AnimationUtils.slideUpIn(nameSection, delay = 100)
            }
            if (logoutBtn != null) {
                AnimationUtils.slideUpIn(logoutBtn, delay = 200)
            }
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Animation setup error: ${e.message}")
        }
    }

    override fun onResume() {
        super.onResume()
        loadProfileData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadJob?.cancel()
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
            Log.e("ProfileFragment", "Security initialization error: ${e.message}")
        }
    }

    private fun initViews(view: View) {
        profileNameText = view.findViewById(R.id.profile_name_text)
        profileEmailText = view.findViewById(R.id.profile_email_text)
        profileAvatarText = view.findViewById(R.id.profile_avatar_text)
        btnLogout = view.findViewById(R.id.btn_logout)
        
        achievementsCard = view.findViewById(R.id.achievements_menu_card) ?: MaterialCardView(requireContext())
        leaderboardCard = view.findViewById(R.id.leaderboard_menu_card) ?: MaterialCardView(requireContext())
        referralCard = view.findViewById(R.id.referral_menu_card) ?: MaterialCardView(requireContext())
        supportCard = view.findViewById(R.id.support_menu_card) ?: MaterialCardView(requireContext())
        settingsCard = view.findViewById(R.id.settings_menu_card) ?: MaterialCardView(requireContext())
        
        setupMenuClickListeners()
    }

    private fun setupMenuClickListeners() {
        achievementsCard.setOnClickListener {
            AnimationUtils.pressAnimation(it)
            startActivity(Intent(context, AchievementsActivity::class.java))
        }
        
        leaderboardCard.setOnClickListener {
            AnimationUtils.pressAnimation(it)
            startActivity(Intent(context, LeaderboardActivity::class.java))
        }
        
        referralCard.setOnClickListener {
            AnimationUtils.pressAnimation(it)
            startActivity(Intent(context, ReferralActivity::class.java))
        }
        
        supportCard.setOnClickListener {
            AnimationUtils.pressAnimation(it)
            startActivity(Intent(context, SupportChatActivity::class.java))
        }
        
        settingsCard.setOnClickListener {
            AnimationUtils.pressAnimation(it)
            Toast.makeText(context, "Opening Settings...", Toast.LENGTH_SHORT).show()
        }
        
        btnLogout.setOnClickListener {
            AnimationUtils.pressAnimation(it)
            showLogoutConfirmation()
        }
    }

    private fun loadProfileData() {
        if (!isAdded) return
        
        loadJob = lifecycleScope.launch {
            try {
                val auth = FirebaseAuth.getInstance()
                val currentUser = auth.currentUser ?: return@launch
                
                val userId = currentUser.uid
                val response = FeaturesApiClient.getUserProfile(userId)
                
                if (response != null) {
                    val name = response.optString("name", "User")
                    val email = response.optString("email", "user@example.com")
                    
                    // Update UI
                    profileNameText.text = name
                    profileEmailText.text = email
                    profileAvatarText.text = name.firstOrNull()?.toString()?.uppercase() ?: "U"
                    
                    // Load referral data
                    referralCode = response.optString("referralCode", "")
                    referralLink = response.optString("referralLink", "")
                }
                
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("ProfileFragment", "Load profile error: ${e.message}")
                if (isAdded) {
                    Toast.makeText(context, "Failed to load profile", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showLogoutConfirmation() {
        try {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes") { _, _ ->
                    performLogout()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Dialog error: ${e.message}")
        }
    }

    private fun performLogout() {
        FirebaseAuth.getInstance().signOut()
        Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
        // Navigate back to login activity
        requireActivity().finish()
    }
}
