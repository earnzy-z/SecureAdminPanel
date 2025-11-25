package com.earnzy.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.lifecycle.lifecycleScope
import com.earnzy.R
import com.earnzy.api.ApiClient
import com.earnzy.data.PromoCode
import com.earnzy.databinding.FragmentPromoBinding
import com.earnzy.databinding.ItemPromoBinding
import kotlinx.coroutines.launch

class PromoFragment : BaseFragment() {
    private var _binding: FragmentPromoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPromoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupInputField()
        showShimmer(true)
        loadPromoCodes()
    }

    private fun setupInputField() {
        binding.redeemButton?.setOnClickListener {
            val code = binding.promoInput?.text.toString().trim()
            if (code.isNotEmpty()) {
                redeemPromo(code)
            } else {
                showError("Enter a promo code")
            }
        }
    }

    private fun loadPromoCodes() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.api.getPromoCodes()
                displayPromoCodes(response["promoCodes"] as? List<PromoCode> ?: emptyList())
                showShimmer(false)
            } catch (e: Exception) {
                showShimmer(false)
                showError("Failed to load promo codes: ${e.message}")
            }
        }
    }

    private fun displayPromoCodes(codes: List<PromoCode>) {
        binding.codesContainer?.removeAllViews()
        codes.forEach { code ->
            val codeView = ItemPromoBinding.inflate(LayoutInflater.from(context), binding.codesContainer, false)
            codeView.codeText.text = code.code
            codeView.descriptionText?.text = code.description ?: "Bonus reward"
            codeView.rewardText.text = "+${code.reward} Coins"
            binding.codesContainer?.addView(codeView.root)
        }
    }

    private fun redeemPromo(code: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = ApiClient.api.redeemPromo(mapOf("code" to code))
                showSuccess("Promo redeemed! Check result")
                binding.promoInput?.text = null
                loadPromoCodes()
            } catch (e: Exception) {
                showError("Invalid or already used code")
            }
        }
    }

    private fun showShimmer(show: Boolean) {
        if (show) {
            binding.codesContainer?.visibility = View.GONE
            val animation = AnimationUtils.loadAnimation(context, R.anim.shimmer)
        } else {
            binding.codesContainer?.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
