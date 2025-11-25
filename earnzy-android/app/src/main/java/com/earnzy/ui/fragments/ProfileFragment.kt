package com.earnzy.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.lifecycle.lifecycleScope
import com.earnzy.R
import com.earnzy.api.ApiClient
import com.earnzy.databinding.FragmentProfileBinding
import kotlinx.coroutines.launch

class ProfileFragment : BaseFragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showShimmer(true)
        loadUserProfile()
        setupClickListeners()
    }

    private fun loadUserProfile() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val user = ApiClient.api.getUser()
                binding.userEmail.text = user.email
                binding.totalEarned.text = user.totalEarned.toString()
                
                // Load more profile data
                binding.userName.text = user.name ?: "User"
                binding.memberSince.text = "Member since ${user.createdAt ?: "Recently"}"
                
                showShimmer(false)
            } catch (e: Exception) {
                showShimmer(false)
                showError("Failed to load profile: ${e.message}")
            }
        }
    }

    private fun setupClickListeners() {
        binding.logoutButton.setOnClickListener {
            logout()
        }
        binding.editButton?.setOnClickListener {
            showSuccess("Edit profile coming soon")
        }
    }

    private fun logout() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                ApiClient.api.logout()
                ApiClient.clearToken()
                showSuccess("Logged out successfully")
            } catch (e: Exception) {
                showError("Logout failed: ${e.message}")
            }
        }
    }

    private fun showShimmer(show: Boolean) {
        if (show) {
            binding.profileContent.visibility = View.GONE
            val animation = AnimationUtils.loadAnimation(context, R.anim.shimmer)
            binding.profileHeader.startAnimation(animation)
        } else {
            binding.profileContent.visibility = View.VISIBLE
            binding.profileHeader.clearAnimation()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
