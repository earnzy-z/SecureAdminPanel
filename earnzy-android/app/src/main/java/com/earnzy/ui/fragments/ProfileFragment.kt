package com.earnzy.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
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
        loadUserProfile()
        setupClickListeners()
    }

    private fun loadUserProfile() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val user = ApiClient.api.getUser()
                binding.userEmail.text = user.email
                binding.totalEarned.text = user.totalEarned.toString()
            } catch (e: Exception) {
                showError("Failed to load profile: ${e.message}")
            }
        }
    }

    private fun setupClickListeners() {
        binding.logoutButton.setOnClickListener {
            logout()
        }
    }

    private fun logout() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                ApiClient.api.logout()
                ApiClient.clearToken()
                // Navigate to login
                showSuccess("Logged out successfully")
            } catch (e: Exception) {
                showError("Logout failed: ${e.message}")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
