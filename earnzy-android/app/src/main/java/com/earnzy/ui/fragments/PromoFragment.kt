package com.earnzy.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.lifecycle.lifecycleScope
import androidx.appcompat.app.AlertDialog
import com.earnzy.api.ApiClient
import com.earnzy.data.PromoCode
import com.earnzy.databinding.FragmentPromoBinding
import com.earnzy.databinding.ItemPromoBinding
import com.google.android.material.textfield.TextInputEditText
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
            } catch (e: Exception) {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
