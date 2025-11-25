package com.earnzy.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.earnzy.databinding.ItemTransactionBinding
import com.earnzy.ui.fragments.Transaction

class TransactionAdapter(
    private var transactions: List<Transaction>
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(transaction: Transaction) {
            binding.apply {
                transactionTitle.text = transaction.title
                transactionDate.text = transaction.date
                transactionAmount.text = "${if (transaction.type == "credit") "+" else "-"}₹${transaction.amount}"
                
                if (transaction.type == "debit") {
                    transactionAmount.setTextColor(android.graphics.Color.parseColor("#FF6B6B"))
                } else {
                    transactionAmount.setTextColor(android.graphics.Color.parseColor("#6C5CE7"))
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    override fun getItemCount() = transactions.size

    fun updateTransactions(newTransactions: List<Transaction>) {
        this.transactions = newTransactions
        notifyDataSetChanged()
    }
}
