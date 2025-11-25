package com.earnzy.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.earnzy.data.Task
import com.earnzy.databinding.ItemTaskBinding

class TaskAdapter(
    private var tasks: List<Task>,
    private val onCompleteClick: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(private val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: Task) {
            binding.apply {
                taskTitle.text = task.title
                taskDescription.text = task.description
                taskReward.text = "+${task.reward} Coins"
                taskCategory.text = task.category.capitalize()
                
                completeButton.isEnabled = task.completedAt == null
                completeButton.text = if (task.completedAt != null) "✓ Done" else "Do"

                completeButton.setOnClickListener {
                    onCompleteClick(task)
                }

                // Load image if available with placeholder
                if (task.imageUrl != null) {
                    // Coil image loading
                    // Glide.with(itemView.context)
                    //     .load(task.imageUrl)
                    //     .placeholder(R.drawable.ic_placeholder)
                    //     .into(taskImage)
                } else {
                    // Set placeholder color
                    taskImage.setBackgroundColor(android.graphics.Color.parseColor("#6C5CE7"))
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(tasks[position])
    }

    override fun getItemCount() = tasks.size

    fun updateTasks(newTasks: List<Task>) {
        this.tasks = newTasks
        notifyDataSetChanged()
    }

    private fun String.capitalize() = this.replaceFirstChar { 
        if (it.isLowerCase()) it.titlecase() else it.toString() 
    }
}
