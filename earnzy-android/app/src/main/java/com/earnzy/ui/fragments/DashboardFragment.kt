package com.earnzy.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.earnzy.R
import com.earnzy.api.ApiClient
import com.earnzy.databinding.FragmentDashboardBinding
import kotlinx.coroutines.launch

class DashboardFragment : BaseFragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadData()
        setupClickListeners()
    }

    private fun loadData() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val user = ApiClient.api.getUser()
                val balance = ApiClient.api.getBalance()

                binding.coinsText.text = "${balance.coins} Coins"

                setupUIWithData(balance.coins, balance.level, balance.nextLevelCoins)
            } catch (e: Exception) {
                showError("Failed to load data: ${e.message}")
            }
        }
    }

    private fun setupUIWithData(coins: Int, level: Int, nextLevel: Int) {
        // Update progress bar
        val progress = (coins * 100) / nextLevel
        binding.progressBar?.progress = progress.coerceIn(0, 100)
    }

    private fun setupClickListeners() {
        binding.tasksButton?.setOnClickListener {
            (parentFragment as? MainActivity)?.navigateTo(R.id.nav_tasks)
        }
        binding.offersButton?.setOnClickListener {
            (parentFragment as? MainActivity)?.navigateTo(R.id.nav_offers)
        }
        binding.referButton?.setOnClickListener {
            // Navigate to referral
        }
        binding.promosButton?.setOnClickListener {
            // Navigate to promos
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
