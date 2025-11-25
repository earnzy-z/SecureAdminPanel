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
                taskCategory.text = task.category
                
                completeButton.isEnabled = task.completedAt == null
                completeButton.text = if (task.completedAt != null) "✓ Done" else "Do"

                completeButton.setOnClickListener {
                    onCompleteClick(task)
                }

                // Load image if available
                if (task.imageUrl != null) {
                    // Load with Coil
                    // Glide.with(itemView.context)
                    //     .load(task.imageUrl)
                    //     .into(taskImage)
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
}
