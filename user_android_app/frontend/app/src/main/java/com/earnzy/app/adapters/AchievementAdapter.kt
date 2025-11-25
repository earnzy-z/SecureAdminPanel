package com.earnzy.app.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.earnzy.app.R
import com.earnzy.app.models.Achievement
import com.google.android.material.card.MaterialCardView

class AchievementAdapter(
    private val achievements: List<Achievement>
) : RecyclerView.Adapter<AchievementAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: MaterialCardView = view.findViewById(R.id.achievement_card)
        val icon: ImageView = view.findViewById(R.id.achievement_icon)
        val title: TextView = view.findViewById(R.id.achievement_title)
        val description: TextView = view.findViewById(R.id.achievement_description)
        val reward: TextView = view.findViewById(R.id.achievement_reward)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_achievement, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val achievement = achievements[position]
        holder.icon.setImageResource(achievement.icon)
        holder.title.text = achievement.title
        holder.description.text = achievement.description
        holder.reward.text = "+${achievement.reward} coins"
        
        if (!achievement.unlocked) {
            holder.card.alpha = 0.5f
            holder.icon.setColorFilter(Color.GRAY)
        } else {
            holder.card.alpha = 1.0f
            holder.icon.clearColorFilter()
        }
    }

    override fun getItemCount() = achievements.size
}
