package com.earnzy.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.earnzy.databinding.ItemLeaderboardBinding

data class LeaderboardEntry(
    val rank: Int,
    val name: String,
    val coins: Int,
    val level: Int
)

class LeaderboardAdapter(
    private var entries: List<LeaderboardEntry>
) : RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder>() {

    inner class LeaderboardViewHolder(private val binding: ItemLeaderboardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(entry: LeaderboardEntry) {
            binding.apply {
                rankText.text = "#${entry.rank}"
                userNameText.text = entry.name
                levelText.text = "Level ${entry.level}"
                coinsText.text = "${entry.coins.toLocaleString()}"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val binding = ItemLeaderboardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LeaderboardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        holder.bind(entries[position])
    }

    override fun getItemCount() = entries.size

    fun updateEntries(newEntries: List<LeaderboardEntry>) {
        this.entries = newEntries
        notifyDataSetChanged()
    }

    private fun Int.toLocaleString(): String {
        return "%,d".format(this)
    }
}
