package com.earnzy.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.earnzy.app.R
import com.earnzy.app.models.EarnTask
import com.google.android.material.card.MaterialCardView
import com.google.android.material.button.MaterialButton

class EarnTaskAdapter(
    private val tasks: List<EarnTask>,
    private val onItemClick: (EarnTask) -> Unit
) : RecyclerView.Adapter<EarnTaskAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.task_title)
        val reward: TextView = view.findViewById(R.id.task_reward)
        val duration: TextView = view.findViewById(R.id.task_duration)
        val startButton: MaterialButton = view.findViewById(R.id.start_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_earn_task, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = tasks[position]
        holder.title.text = task.title
        holder.reward.text = task.reward
        holder.duration.text = task.duration
        holder.startButton.setOnClickListener { onItemClick(task) }
    }

    override fun getItemCount() = tasks.size
}
