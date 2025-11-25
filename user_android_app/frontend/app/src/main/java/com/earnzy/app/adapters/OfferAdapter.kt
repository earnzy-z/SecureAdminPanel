package com.earnzy.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.earnzy.app.R
import com.earnzy.app.models.Offer
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class OfferAdapter(
    private val offers: List<Offer>,
    private val onItemClick: (Offer) -> Unit
) : RecyclerView.Adapter<OfferAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.offer_icon)
        val title: TextView = view.findViewById(R.id.offer_title)
        val reward: TextView = view.findViewById(R.id.offer_reward)
        val description: TextView = view.findViewById(R.id.offer_description)
        val startButton: MaterialButton = view.findViewById(R.id.start_offer_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_offer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val offer = offers[position]
        holder.icon.setImageResource(offer.icon)
        holder.title.text = offer.title
        holder.reward.text = offer.reward
        holder.description.text = offer.description
        holder.startButton.setOnClickListener { onItemClick(offer) }
    }

    override fun getItemCount() = offers.size
}
