package com.earnzy.app.Activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.earnzy.app.R
import com.earnzy.app.adapters.AchievementAdvancedAdapter
import com.earnzy.app.databinding.ActivityAchievementsAdvancedBinding
import com.earnzy.app.models.Achievement
import com.google.android.material.chip.Chip

/**
 * Advanced Achievements Activity with progress tracking and categories
 * Uses ViewBinding for type-safe view access
 */
class AchievementsAdvancedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAchievementsAdvancedBinding
    private lateinit var achievementsAdapter: AchievementAdvancedAdapter
    private lateinit var recentAchievementsAdapter: AchievementAdvancedAdapter
    
    private val allAchievements = mutableListOf<Achievement>()
    private var currentFilter = "all"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAchievementsAdvancedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupRecyclerViews()
        setupClickListeners()
        loadAchievements()
    }

    private fun setupUI() {
        // Set stats
        val completed = allAchievements.count { it.isCompleted }
        val inProgress = allAchievements.count { it.progress > 0 && !it.isCompleted }
        val total = allAchievements.size

        binding.completedCount.text = completed.toString()
        binding.inProgressCount.text = inProgress.toString()
        binding.totalCount.text = total.toString()
    }

    private fun setupRecyclerViews() {
        // Recent achievements (horizontal)
        recentAchievementsAdapter = AchievementAdvancedAdapter(
            mutableListOf(),
            isHorizontal = true
        ) { achievement ->
            onAchievementClicked(achievement)
        }
        
        binding.recentAchievementsRecycler.apply {
            layoutManager = LinearLayoutManager(
                this@AchievementsAdvancedActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = recentAchievementsAdapter
        }

        // All achievements (grid)
        achievementsAdapter = AchievementAdvancedAdapter(
            mutableListOf(),
            isHorizontal = false
        ) { achievement ->
            onAchievementClicked(achievement)
        }
        
        binding.achievementsRecycler.apply {
            layoutManager = GridLayoutManager(this@AchievementsAdvancedActivity, 1)
            adapter = achievementsAdapter
        }
    }

    private fun setupClickListeners() {
        binding.backButtonAchievements.setOnClickListener {
            finish()
        }

        binding.fabShareAchievements.setOnClickListener {
            Toast.makeText(this, "Share achievements feature", Toast.LENGTH_SHORT).show()
            // TODO: Implement share functionality
        }

        // Filter chips
        binding.achievementFilterChips.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chip = findViewById<Chip>(checkedIds[0])
                currentFilter = when (chip.id) {
                    R.id.chip_earning -> "earning"
                    R.id.chip_social -> "social"
                    R.id.chip_milestones -> "milestones"
                    R.id.chip_special -> "special"
                    else -> "all"
                }
                filterAchievements()
            }
        }
    }

 private fun loadAchievements() {
    // TODO: Load from backend
    // Sample data for demonstration
    allAchievements.clear()
    allAchievements.addAll(
        listOf(
            Achievement(
                id = "1",
                title = "First Steps",
                description = "Complete your first task",
                category = "earning",
                icon = R.drawable.ic_achievement, // ✅ added
                unlocked = true, // ✅ added
                reward = 100,
                progress = 100,
                maxProgress = 100,
                isCompleted = true
            ),
            Achievement(
                id = "2",
                title = "Early Bird",
                description = "Earn coins 7 days in a row",
                category = "milestones",
                icon = R.drawable.ic_achievement, // ✅ added
                unlocked = false, // ✅ added
                reward = 500,
                progress = 5,
                maxProgress = 7,
                isCompleted = false
            ),
            Achievement(
                id = "3",
                title = "Social Butterfly",
                description = "Refer 5 friends",
                category = "social",
                icon = R.drawable.ic_achievement, // ✅ added
                unlocked = false, // ✅ added
                reward = 1000,
                progress = 2,
                maxProgress = 5,
                isCompleted = false
            ),
            Achievement(
                id = "4",
                title = "High Roller",
                description = "Earn 10,000 coins",
                category = "earning",
                icon = R.drawable.ic_achievement, // ✅ added
                unlocked = false, // ✅ added
                reward = 2000,
                progress = 6500,
                maxProgress = 10000,
                isCompleted = false
            )
        )
    )

    updateUI()
}

    private fun filterAchievements() {
        val filtered = if (currentFilter == "all") {
            allAchievements
        } else {
            allAchievements.filter { it.category == currentFilter }
        }
        
        achievementsAdapter.updateAchievements(filtered)
    }

    private fun updateUI() {
        // Update stats
        val completed = allAchievements.count { it.isCompleted }
        val inProgress = allAchievements.count { it.progress > 0 && !it.isCompleted }
        
        binding.completedCount.text = completed.toString()
        binding.inProgressCount.text = inProgress.toString()
        binding.totalCount.text = allAchievements.size.toString()

        // Update recent (completed achievements)
        val recent = allAchievements.filter { it.isCompleted }.take(5)
        recentAchievementsAdapter.updateAchievements(recent)

        // Update main list
        filterAchievements()
    }

    private fun onAchievementClicked(achievement: Achievement) {
        Toast.makeText(
            this,
            "${achievement.title}: ${achievement.progress}/${achievement.maxProgress}",
            Toast.LENGTH_SHORT
        ).show()
    }
}
