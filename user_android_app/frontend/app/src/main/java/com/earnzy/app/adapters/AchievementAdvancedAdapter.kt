package com.earnzy.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.earnzy.app.R
import com.earnzy.app.databinding.ItemAchievementAdvancedBinding
import com.earnzy.app.models.Achievement

/**
 * Adapter for displaying achievements with progress tracking
 */
class AchievementAdvancedAdapter(
    private var achievements: MutableList<Achievement>,
    private val isHorizontal: Boolean = false,
    private val onItemClick: (Achievement) -> Unit
) : RecyclerView.Adapter<AchievementAdvancedAdapter.AchievementViewHolder>() {

    inner class AchievementViewHolder(
        private val binding: ItemAchievementAdvancedBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(achievement: Achievement) {
            binding.achievementTitle.text = achievement.title
            binding.achievementDescription.text = achievement.description
            binding.rewardAmount.text = achievement.reward.toString()
            
            // Progress
            if (achievement.isCompleted) {
                binding.progressSection.visibility = View.GONE
                binding.completionBadge.visibility = View.VISIBLE
            } else {
                binding.progressSection.visibility = View.VISIBLE
                binding.completionBadge.visibility = View.GONE
                binding.achievementProgress.progress = achievement.progressPercentage
                binding.progressText.text = achievement.progressText
            }

            // Icon animation
            binding.achievementIcon.setAnimation(getAnimationForCategory(achievement.category))
            binding.achievementIcon.playAnimation()

            // Click listener
            binding.root.setOnClickListener {
                onItemClick(achievement)
            }
        }

        private fun getAnimationForCategory(category: String): Int {
            return when (category) {
                "earning" -> R.raw.coin
                "social" -> R.raw.referral_success
                "milestones" -> R.raw.trophy_animation
                "special" -> R.raw.reward_celebration
                else -> R.raw.coin
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementViewHolder {
        val binding = ItemAchievementAdvancedBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        
        // Adjust width for horizontal layout
        if (isHorizontal) {
            val layoutParams = binding.root.layoutParams
            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
            binding.root.layoutParams = layoutParams
        }
        
        return AchievementViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {
        holder.bind(achievements[position])
    }

    override fun getItemCount(): Int = achievements.size

    fun updateAchievements(newAchievements: List<Achievement>) {
        achievements.clear()
        achievements.addAll(newAchievements)
        notifyDataSetChanged()
    }
}
