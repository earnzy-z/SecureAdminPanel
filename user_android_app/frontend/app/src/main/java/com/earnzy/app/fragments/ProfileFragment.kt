package com.earnzy.app.fragments

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.earnzy.app.Activity.AchievementsActivity
import com.earnzy.app.Activity.LeaderboardActivity
import com.earnzy.app.Activity.ReferralActivity
import com.earnzy.app.Activity.SupportChatActivity
import com.earnzy.app.R
import com.earnzy.app.network.FeaturesApiClient
import com.earnzy.app.utils.AnimationUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {

    private lateinit var profileNameText: MaterialTextView
    private lateinit var profileEmailText: MaterialTextView
    private lateinit var avatarText: MaterialTextView
    private lateinit var coinsText: MaterialTextView
    private lateinit var referralText: MaterialTextView
    private lateinit var btnLogout: MaterialButton
    private lateinit var achievementsCard: MaterialCardView
    private lateinit var leaderboardCard: MaterialCardView
    private lateinit var referralCard: MaterialCardView
    private lateinit var supportCard: MaterialCardView
    private lateinit var settingsCard: MaterialCardView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile_professional, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupClickListeners()
        animateEntrance()
        loadProfileData()
    }

    private fun initViews(view: View) {
        profileNameText = view.findViewById(R.id.profile_name_text)
        profileEmailText = view.findViewById(R.id.profile_email_text)
        avatarText = view.findViewById(R.id.profile_avatar_text)
        coinsText = view.findViewById(R.id.profile_coins_text)
        referralText = view.findViewById(R.id.profile_referral_text)
        btnLogout = view.findViewById(R.id.btn_logout)
        achievementsCard = view.findViewById(R.id.achievements_menu_card)
        leaderboardCard = view.findViewById(R.id.leaderboard_menu_card)
        referralCard = view.findViewById(R.id.referral_menu_card)
        supportCard = view.findViewById(R.id.support_menu_card)
        settingsCard = view.findViewById(R.id.settings_menu_card)
    }

    private fun setupClickListeners() {
        achievementsCard.setOnClickListener {
            AnimationUtils.pressAnimation(it)
            try {
                startActivity(Intent(context, AchievementsActivity::class.java))
            } catch (e: Exception) {
                Toast.makeText(context, "Activity not available", Toast.LENGTH_SHORT).show()
            }
        }

        leaderboardCard.setOnClickListener {
            AnimationUtils.pressAnimation(it)
            try {
                startActivity(Intent(context, LeaderboardActivity::class.java))
            } catch (e: Exception) {
                Toast.makeText(context, "Activity not available", Toast.LENGTH_SHORT).show()
            }
        }

        referralCard.setOnClickListener {
            AnimationUtils.pressAnimation(it)
            try {
                startActivity(Intent(context, ReferralActivity::class.java))
            } catch (e: Exception) {
                Toast.makeText(context, "Activity not available", Toast.LENGTH_SHORT).show()
            }
        }

        supportCard.setOnClickListener {
            AnimationUtils.pressAnimation(it)
            try {
                startActivity(Intent(context, SupportChatActivity::class.java))
            } catch (e: Exception) {
                Toast.makeText(context, "Activity not available", Toast.LENGTH_SHORT).show()
            }
        }

        settingsCard.setOnClickListener {
            AnimationUtils.pressAnimation(it)
            Toast.makeText(context, "Settings - Coming Soon!", Toast.LENGTH_SHORT).show()
        }

        btnLogout.setOnClickListener {
            AnimationUtils.pressAnimation(it)
            showLogoutDialog()
        }
    }

    private fun animateEntrance() {
        try {
            AnimationUtils.slideUpIn(avatarText, delay = 0)
            AnimationUtils.slideUpIn(profileNameText, delay = 50)
            AnimationUtils.slideUpIn(profileEmailText, delay = 100)
            AnimationUtils.slideUpIn(btnLogout, delay = 150)
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Animation error: ${e.message}")
        }
    }

    private fun loadProfileData() {
        lifecycleScope.launch {
            try {
                val auth = FirebaseAuth.getInstance()
                val user = auth.currentUser ?: run {
                    Toast.makeText(context, "Not logged in", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val ctx = context ?: return@launch
                val idToken = try { user.getIdToken(false).await().token ?: "" } catch (e: Exception) { "" }
                val deviceID = Settings.Secure.getString(ctx.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"
                val deviceToken = ""
                val isVpn = false
                val isSslProxy = false

                val response = withContext(Dispatchers.IO) {
                    FeaturesApiClient.getUserProfile(ctx, idToken, deviceID, deviceToken, isVpn, isSslProxy)
                }

                if (isAdded && response != null) {
                    val status = response.optString("status")
                    if (status == "success") {
                        val userObj = response.optJSONObject("user")
                        val name = userObj?.optString("name", user.displayName ?: "User") ?: "User"
                        val email = userObj?.optString("email", user.email ?: "user@example.com") ?: "user@example.com"
                        val coins = userObj?.optInt("coins", 0) ?: 0
                        val referralCount = userObj?.optInt("referral_count", 0) ?: 0

                        profileNameText.text = name
                        profileEmailText.text = email
                        coinsText.text = "₹ $coins"
                        referralText.text = "$referralCount Referrals"
                        avatarText.text = name.firstOrNull()?.toString()?.uppercase() ?: "U"
                    } else {
                        setupMockProfile(user)
                    }
                } else {
                    setupMockProfile(user)
                }
            } catch (e: Exception) {
                Log.e("ProfileFragment", "Error loading profile: ${e.message}")
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null && isAdded) {
                    setupMockProfile(user)
                }
            }
        }
    }

    private fun setupMockProfile(user: com.google.firebase.auth.FirebaseUser) {
        profileNameText.text = user.displayName ?: "User"
        profileEmailText.text = user.email ?: "user@example.com"
        avatarText.text = (user.displayName ?: "U").firstOrNull()?.toString()?.uppercase() ?: "U"
        coinsText.text = "₹ 0"
        referralText.text = "0 Referrals"
    }

    private fun showLogoutDialog() {
        try {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Logout?")
                .setMessage("Are you sure you want to logout from Earnzy?")
                .setPositiveButton("Yes") { _, _ -> performLogout() }
                .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
                .setCancelable(true)
                .show()
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Dialog error: ${e.message}")
        }
    }

    private fun performLogout() {
        FirebaseAuth.getInstance().signOut()
        Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
        requireActivity().finish()
    }

    override fun onResume() {
        super.onResume()
        loadProfileData()
    }
}
