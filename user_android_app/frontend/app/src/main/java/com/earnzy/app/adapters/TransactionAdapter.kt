package com.earnzy.app.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.earnzy.app.R
import com.earnzy.app.models.Transaction

class TransactionAdapter(
    private val transactions: List<Transaction>
) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val description: TextView = view.findViewById(R.id.transaction_description)
        val amount: TextView = view.findViewById(R.id.transaction_amount)
        val timestamp: TextView = view.findViewById(R.id.transaction_timestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.description.text = transaction.description
        holder.amount.text = transaction.amount
        holder.timestamp.text = transaction.timestamp
        
        // Set color based on type
        if (transaction.type == "credit") {
            holder.amount.setTextColor(Color.parseColor("#4CAF50"))
        } else {
            holder.amount.setTextColor(Color.parseColor("#F44336"))
        }
    }

    override fun getItemCount() = transactions.size
}
