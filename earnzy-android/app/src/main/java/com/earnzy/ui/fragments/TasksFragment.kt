package com.earnzy.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.earnzy.R
import com.earnzy.api.ApiClient
import com.earnzy.data.Task
import com.earnzy.databinding.FragmentTasksBinding
import com.earnzy.ui.adapters.TaskAdapter
import kotlinx.coroutines.launch

class TasksFragment : BaseFragment() {
    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!
    private lateinit var taskAdapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        showShimmer(true)
        loadTasks()
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(emptyList()) { task ->
            completeTask(task)
        }
        binding.tasksRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = taskAdapter
            setHasFixedSize(true)
        }
    }

    private fun loadTasks() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.api.getTasks()
                taskAdapter.updateTasks(response.tasks)
                showShimmer(false)
            } catch (e: Exception) {
                showShimmer(false)
                showError("Failed to load tasks: ${e.message}")
            }
        }
    }

    private fun completeTask(task: Task) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                ApiClient.api.completeTask(task.id, mapOf("reward" to task.reward))
                showSuccess("Task completed! +${task.reward} coins")
                loadTasks()
            } catch (e: Exception) {
                showError("Task already completed or failed")
            }
        }
    }

    private fun showShimmer(show: Boolean) {
        if (show) {
            binding.tasksRecyclerView.visibility = View.GONE
            val shimmerAnimation = AnimationUtils.loadAnimation(context, R.anim.shimmer)
            // Apply shimmer to placeholder items
        } else {
            binding.tasksRecyclerView.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
