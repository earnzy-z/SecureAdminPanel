package com.earnzy.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.earnzy.app.R
import com.earnzy.app.models.EarningFeature
import com.google.android.material.card.MaterialCardView

class EarningFeatureAdapter(
    private val features: List<EarningFeature>,
    private val onItemClick: (EarningFeature) -> Unit
) : RecyclerView.Adapter<EarningFeatureAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: MaterialCardView = view.findViewById(R.id.feature_card)
        val icon: ImageView = view.findViewById(R.id.feature_icon)
        val title: TextView = view.findViewById(R.id.feature_title)
        val subtitle: TextView = view.findViewById(R.id.feature_subtitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_earning_feature_gradient, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val feature = features[position]
        holder.icon.setImageResource(feature.icon)
        holder.title.text = feature.title
        holder.subtitle.text = feature.subtitle
        
        // Add elastic spring animation on bind
        holder.card.startAnimation(
            android.view.animation.AnimationUtils.loadAnimation(
                holder.card.context,
                R.anim.elastic_spring_in
            ).apply {
                startOffset = (position * 50).toLong() // Stagger animation
            }
        )
        
        holder.card.setOnClickListener { 
            // Add press animation
            it.startAnimation(
                android.view.animation.AnimationUtils.loadAnimation(
                    it.context,
                    R.anim.card_press_scale
                )
            )
            onItemClick(feature) 
        }
    }

    override fun getItemCount() = features.size
}
