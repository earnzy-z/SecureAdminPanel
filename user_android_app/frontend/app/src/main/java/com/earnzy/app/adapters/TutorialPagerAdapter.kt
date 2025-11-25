package com.earnzy.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.earnzy.app.databinding.ItemTutorialPageBinding
import com.earnzy.app.models.TutorialPage

/**
 * Adapter for tutorial pages with ViewPager2
 */
class TutorialPagerAdapter(
    private val pages: List<TutorialPage>
) : RecyclerView.Adapter<TutorialPagerAdapter.TutorialPageViewHolder>() {

    inner class TutorialPageViewHolder(
        private val binding: ItemTutorialPageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(page: TutorialPage) {
            binding.tutorialTitle.text = page.title
            binding.tutorialDescription.text = page.description
            
            // Set Lottie animation
            binding.tutorialAnimation.setAnimation(page.animationRes)
            binding.tutorialAnimation.playAnimation()

            // Show/hide features
            binding.featureHighlights.visibility = if (page.showFeatures) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TutorialPageViewHolder {
        val binding = ItemTutorialPageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TutorialPageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TutorialPageViewHolder, position: Int) {
        holder.bind(pages[position])
    }

    override fun getItemCount(): Int = pages.size
}
