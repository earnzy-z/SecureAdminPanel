package com.earnzy.app.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {

    private lateinit var securePrefs: SharedPreferences
    private var loadJob: Job? = null
    
    private lateinit var profileNameText: MaterialTextView
    private lateinit var profileEmailText: MaterialTextView
    private lateinit var profileAvatarText: MaterialTextView
    private lateinit var btnLogout: MaterialButton

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
            view.findViewById<MaterialTextView?>(R.id.profile_avatar_text)?.let {
                AnimationUtils.slideUpIn(it, delay = 0)
            }
            view.findViewById<MaterialTextView?>(R.id.profile_name_text)?.let {
                AnimationUtils.slideUpIn(it, delay = 100)
            }
            view.findViewById<MaterialButton?>(R.id.btn_logout)?.let {
                AnimationUtils.slideUpIn(it, delay = 200)
            }
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Animation error: ${e.message}")
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
            Log.e("ProfileFragment", "Security error: ${e.message}")
        }
    }

    private fun initViews(view: View) {
        profileNameText = view.findViewById(R.id.profile_name_text)
        profileEmailText = view.findViewById(R.id.profile_email_text)
        profileAvatarText = view.findViewById(R.id.profile_avatar_text)
        btnLogout = view.findViewById(R.id.btn_logout)

        setupMenuClickListeners(view)
    }

    private fun setupMenuClickListeners(view: View) {
        view.findViewById<MaterialCardView?>(R.id.achievements_menu_card)?.setOnClickListener {
            AnimationUtils.pressAnimation(it)
            startActivity(Intent(context, AchievementsActivity::class.java))
        }

        view.findViewById<MaterialCardView?>(R.id.leaderboard_menu_card)?.setOnClickListener {
            AnimationUtils.pressAnimation(it)
            startActivity(Intent(context, LeaderboardActivity::class.java))
        }

        view.findViewById<MaterialCardView?>(R.id.referral_menu_card)?.setOnClickListener {
            AnimationUtils.pressAnimation(it)
            startActivity(Intent(context, ReferralActivity::class.java))
        }

        view.findViewById<MaterialCardView?>(R.id.support_menu_card)?.setOnClickListener {
            AnimationUtils.pressAnimation(it)
            startActivity(Intent(context, SupportChatActivity::class.java))
        }

        view.findViewById<MaterialCardView?>(R.id.settings_menu_card)?.setOnClickListener {
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
                val ctx = context ?: return@launch
                val idToken = try { currentUser.getIdToken(false).await().token ?: "" } catch (e: Exception) { "" }
                val deviceID = Settings.Secure.getString(ctx.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"
                val deviceToken = ""
                val isVpn = false
                val isSslProxy = false

                val response = withContext(Dispatchers.IO) {
                    FeaturesApiClient.getUserProfile(ctx, idToken, deviceID, deviceToken, isVpn, isSslProxy)
                }

                if (isAdded && response != null) {
                    val name = response.optString("name", "User")
                    val email = response.optString("email", "user@example.com")
                    profileNameText.text = name
                    profileEmailText.text = email
                    profileAvatarText.text = name.firstOrNull()?.toString()?.uppercase() ?: "U"
                }
            } catch (e: Exception) {
                Log.e("ProfileFragment", "Load error: ${e.message}")
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
                .setPositiveButton("Yes") { _, _ -> performLogout() }
                .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
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
}
