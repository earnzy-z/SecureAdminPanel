package com.earnzy.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.earnzy.api.ApiClient
import com.earnzy.databinding.FragmentReferralBinding
import kotlinx.coroutines.launch

class ReferralFragment : BaseFragment() {
    private var _binding: FragmentReferralBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReferralBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadReferralData()
        setupClickListeners()
    }

    private fun loadReferralData() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val code = ApiClient.api.getReferralCode()
                val stats = ApiClient.api.getReferralStats()
                
                binding.referralCode.text = code.code
                binding.totalReferrals.text = stats.totalReferrals.toString()
                binding.earnedCoins.text = stats.earnedCoins.toString()
                binding.activeReferrals.text = stats.activeReferrals.toString()
            } catch (e: Exception) {
                showError("Failed to load referral data: ${e.message}")
            }
        }
    }

    private fun setupClickListeners() {
        binding.shareButton?.setOnClickListener {
            shareReferralCode()
        }
        binding.copyButton?.setOnClickListener {
            copyToClipboard(binding.referralCode.text.toString())
            showSuccess("Code copied!")
        }
    }

    private fun shareReferralCode() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Join Earnzy with my code: ${binding.referralCode.text}")
        }
        startActivity(Intent.createChooser(shareIntent, "Share Referral Code"))
    }

    private fun copyToClipboard(text: String) {
        val clipboard = context?.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("referral_code", text)
        clipboard.setPrimaryClip(clip)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
