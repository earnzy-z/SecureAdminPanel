package com.earnzy.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.earnzy.api.ApiClient
import com.earnzy.databinding.FragmentLeaderboardBinding
import com.earnzy.ui.adapters.LeaderboardAdapter
import com.earnzy.ui.adapters.LeaderboardEntry
import kotlinx.coroutines.launch

class LeaderboardFragment : BaseFragment() {
    private var _binding: FragmentLeaderboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: LeaderboardAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLeaderboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadLeaderboard()
    }

    private fun setupRecyclerView() {
        adapter = LeaderboardAdapter(emptyList())
        binding.leaderboardRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@LeaderboardFragment.adapter
            setHasFixedSize(true)
        }
    }

    private fun loadLeaderboard() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val leaderboardData = listOf(
                    LeaderboardEntry(1, "Player 1", 50000, 50),
                    LeaderboardEntry(2, "Player 2", 45000, 48),
                    LeaderboardEntry(3, "Player 3", 40000, 45),
                    LeaderboardEntry(4, "You", 35000, 42),
                    LeaderboardEntry(5, "Player 5", 30000, 40),
                    LeaderboardEntry(6, "Player 6", 25000, 38),
                    LeaderboardEntry(7, "Player 7", 20000, 35),
                    LeaderboardEntry(8, "Player 8", 15000, 32),
                )
                adapter.updateEntries(leaderboardData)
            } catch (e: Exception) {
                showError("Failed to load leaderboard: ${e.message}")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
