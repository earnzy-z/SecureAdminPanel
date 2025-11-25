package com.earnzy

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.earnzy.databinding.ActivityMainBinding
import com.earnzy.ui.fragments.DashboardFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
    }

    private fun setupNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    loadFragment(DashboardFragment(), "Dashboard")
                    true
                }
                R.id.nav_tasks -> {
                    loadFragment(com.earnzy.ui.fragments.TasksFragment(), "Tasks")
                    true
                }
                R.id.nav_offers -> {
                    loadFragment(com.earnzy.ui.fragments.OffersFragment(), "Offers")
                    true
                }
                R.id.nav_leaderboard -> {
                    loadFragment(com.earnzy.ui.fragments.LeaderboardFragment(), "Leaderboard")
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(com.earnzy.ui.fragments.ProfileFragment(), "Profile")
                    true
                }
                else -> false
            }
        }

        // Load dashboard by default
        if (savedInstanceState == null) {
            loadFragment(DashboardFragment(), "Dashboard")
        }
    }

    private fun loadFragment(fragment: androidx.fragment.app.Fragment, title: String) {
        binding.topAppBar.title = title
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
