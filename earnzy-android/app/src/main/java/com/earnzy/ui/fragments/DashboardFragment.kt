package com.earnzy.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.lifecycle.lifecycleScope
import com.earnzy.R
import com.earnzy.api.ApiClient
import com.earnzy.databinding.FragmentDashboardImprovedBinding
import kotlinx.coroutines.launch

class DashboardFragment : BaseFragment() {
    private var _binding: FragmentDashboardImprovedBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardImprovedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showShimmer(true)
        loadUserData()
        setupClickListeners()
    }

    private fun loadUserData() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val user = ApiClient.api.getUser()
                val balance = ApiClient.api.getBalance()

                binding.coinsText.text = "${balance.coins} Coins"
                updateProgressBar(balance.coins, balance.nextLevelCoins)

                showShimmer(false)
            } catch (e: Exception) {
                showShimmer(false)
                showError("Failed to load data: ${e.message}")
            }
        }
    }

    private fun updateProgressBar(coins: Int, nextLevel: Int) {
        val progress = if (nextLevel > 0) (coins * 100) / nextLevel else 0
        binding.progressBar?.progress = progress.coerceIn(0, 100)
    }

    private fun setupClickListeners() {
        binding.tasksButton?.setOnClickListener {
            navigateToFragment(TasksFragment())
        }
        binding.offersButton?.setOnClickListener {
            navigateToFragment(OffersFragment())
        }
        binding.referButton?.setOnClickListener {
            navigateToFragment(ReferralFragment())
        }
        binding.promosButton?.setOnClickListener {
            navigateToFragment(PromoFragment())
        }
    }

    private fun navigateToFragment(fragment: BaseFragment) {
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun showShimmer(show: Boolean) {
        if (show) {
            binding.shimmerContainer.visibility = View.VISIBLE
            binding.contentContainer.visibility = View.GONE
            val animation = AnimationUtils.loadAnimation(context, R.anim.shimmer)
            binding.shimmerContainer.startAnimation(animation)
        } else {
            binding.shimmerContainer.visibility = View.GONE
            binding.contentContainer.visibility = View.VISIBLE
            binding.shimmerContainer.clearAnimation()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
