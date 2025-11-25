
package com.earnzy.app.adapters

import android.animation.ValueAnimator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.earnzy.app.R
import com.earnzy.app.models.AdminFeatureItem

class AdminFeatureAdapter(private val onClick: (AdminFeatureItem) -> Unit) :
    ListAdapter<AdminFeatureItem, AdminFeatureAdapter.FeatureViewHolder>(DiffCallback()) {

    inner class FeatureViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val title: TextView = view.findViewById(R.id.feature_title)
        private val lottie: LottieAnimationView = view.findViewById(R.id.feature_lottie)
        private val card: ConstraintLayout = view.findViewById(R.id.feature_card_root)

        fun bind(item: AdminFeatureItem) {
            if (!item.isVisible) return

            title.text = item.title

            lottie.apply {
                cancelAnimation()
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.RESTART
                enableMergePathsForKitKatAndAbove(true)

                if (item.lottieUrl.startsWith("http")) {
                    setAnimationFromUrl(item.lottieUrl)
                    addLottieOnCompositionLoadedListener { playAnimation() }
                    setFailureListener { e ->
                        Log.e("LottieLoad", "Failed for ${item.title}: ${e.message}")
                        item.iconFallbackRes?.let { setAnimation(it); playAnimation() }
                    }
                } else if (item.iconFallbackRes != null) {
                    setAnimation(item.iconFallbackRes)
                    playAnimation()
                }
            }

            card.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeatureViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_feature_card, parent, false)
        return FeatureViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeatureViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<AdminFeatureItem>() {
        override fun areItemsTheSame(old: AdminFeatureItem, new: AdminFeatureItem) = old.id == new.id
        override fun areContentsTheSame(old: AdminFeatureItem, new: AdminFeatureItem) = old == new
    }
}