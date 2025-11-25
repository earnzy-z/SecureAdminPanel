package com.earnzy.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.earnzy.R
import com.earnzy.api.ApiClient
import com.earnzy.databinding.FragmentWalletBinding
import com.earnzy.ui.adapters.TransactionAdapter
import kotlinx.coroutines.launch

data class Transaction(
    val id: String,
    val title: String,
    val amount: Double,
    val date: String,
    val type: String
)

class WalletFragment : BaseFragment() {
    private var _binding: FragmentWalletBinding? = null
    private val binding get() = _binding!!
    private lateinit var transactionAdapter: TransactionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWalletBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        showShimmer(true)
        loadWalletData()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter(emptyList())
        binding.transactionRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = transactionAdapter
            setHasFixedSize(true)
        }
    }

    private fun loadWalletData() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val balance = ApiClient.api.getBalance()
                val transactions = ApiClient.api.getTransactions()

                binding.balanceText.text = "₹${balance.coins}"
                binding.totalEarned.text = "₹${balance.totalEarned}"
                binding.totalWithdrawn.text = "₹${balance.totalWithdrawn}"
                binding.totalPending.text = "₹${balance.pendingWithdrawal}"

                val txnList = transactions.map {
                    Transaction(
                        id = it.id,
                        title = it.title,
                        amount = it.amount.toDouble(),
                        date = it.date,
                        type = it.type
                    )
                }
                transactionAdapter.updateTransactions(txnList)

                showShimmer(false)
            } catch (e: Exception) {
                showShimmer(false)
                showError("Failed to load wallet: ${e.message}")
            }
        }
    }

    private fun setupClickListeners() {
        binding.withdrawButton.setOnClickListener {
            initiateWithdrawal()
        }
    }

    private fun initiateWithdrawal() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Show withdrawal dialog or navigate to withdrawal screen
                showSuccess("Withdrawal initiated")
            } catch (e: Exception) {
                showError("Failed to withdraw: ${e.message}")
            }
        }
    }

    private fun showShimmer(show: Boolean) {
        if (show) {
            binding.transactionRecyclerView.visibility = View.GONE
            val animation = AnimationUtils.loadAnimation(context, R.anim.shimmer)
            binding.balanceText.startAnimation(animation)
        } else {
            binding.transactionRecyclerView.visibility = View.VISIBLE
            binding.balanceText.clearAnimation()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
