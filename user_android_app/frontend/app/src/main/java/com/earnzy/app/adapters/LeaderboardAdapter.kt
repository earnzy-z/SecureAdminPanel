package com.earnzy.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.earnzy.app.R
import com.earnzy.app.models.LeaderboardEntry

class LeaderboardAdapter(
    private val entries: List<LeaderboardEntry>
) : RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rankText: TextView = view.findViewById(R.id.rank_text)
        val nameText: TextView = view.findViewById(R.id.name_text)
        val coinsText: TextView = view.findViewById(R.id.coins_text)
        val trophyIcon: ImageView = view.findViewById(R.id.trophy_icon)
        val winnerAnimation: LottieAnimationView = view.findViewById(R.id.winner_animation)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leaderboard_entry, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = entries[position]
        holder.rankText.text = "#${entry.rank}"
        holder.nameText.text = entry.name
        holder.coinsText.text = "${entry.coins} ₹"

        // Set text colors to white for better visibility on the glassy background
        holder.rankText.setTextColor(holder.itemView.context.getColor(R.color.white))
        holder.nameText.setTextColor(holder.itemView.context.getColor(R.color.white))
        holder.coinsText.setTextColor(holder.itemView.context.getColor(R.color.white))

        // Show trophy and animation for top 3
        if (entry.rank <= 3) {
            holder.trophyIcon.visibility = View.VISIBLE
            holder.winnerAnimation.visibility = View.VISIBLE
            if (entry.trophyIcon != 0) {
                holder.trophyIcon.setImageResource(entry.trophyIcon)
            }
        } else {
            holder.trophyIcon.visibility = View.GONE
            holder.winnerAnimation.visibility = View.GONE
        }
    }

    override fun getItemCount() = entries.size
}
