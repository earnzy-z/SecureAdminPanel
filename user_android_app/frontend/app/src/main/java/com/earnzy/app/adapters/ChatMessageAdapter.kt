package com.earnzy.app.adapters

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.earnzy.app.R
import com.earnzy.app.models.ChatMessage
import com.google.android.material.card.MaterialCardView

class ChatMessageAdapter(
    private val messages: List<ChatMessage>
) : RecyclerView.Adapter<ChatMessageAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val messageCard: MaterialCardView = view.findViewById(R.id.message_card)
        val messageText: TextView = view.findViewById(R.id.message_text)
        val container: LinearLayout = view.findViewById(R.id.message_container)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messages[position]
        holder.messageText.text = message.text
        
        if (message.isSent) {
            holder.container.gravity = Gravity.END
            holder.messageCard.setCardBackgroundColor(
                holder.itemView.context.getColor(R.color.primary)
            )
            holder.messageText.setTextColor(
                holder.itemView.context.getColor(android.R.color.white)
            )
        } else {
            holder.container.gravity = Gravity.START
            holder.messageCard.setCardBackgroundColor(
                holder.itemView.context.getColor(R.color.light_gray)
            )
            holder.messageText.setTextColor(
                holder.itemView.context.getColor(android.R.color.black)
            )
        }
    }

    override fun getItemCount() = messages.size
}
