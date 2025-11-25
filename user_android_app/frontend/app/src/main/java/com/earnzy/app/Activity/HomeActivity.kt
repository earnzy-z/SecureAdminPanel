package com.earnzy.app

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import android.view.animation.AnimationUtils
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import com.earnzy.app.R
import com.earnzy.app.fragments.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView

class HomeActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Make status bar transparent
        setupTransparentStatusBar()
        
        setContentView(R.layout.activity_home)
        
        bottomNav = findViewById(R.id.bottom_navigation)
        
        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }
        
        // Animate bottom navigation on start
        animateBottomNav()
        
        bottomNav.setOnItemSelectedListener(NavigationBarView.OnItemSelectedListener { item ->
            // Animate bottom nav on item change
            val scaleDown = AnimationUtils.loadAnimation(this, R.anim.scale_down)
            val scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up)
            
            bottomNav.startAnimation(scaleDown)
            
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_earn -> EarnFragment()
                R.id.nav_wallet -> WalletFragment()
                R.id.nav_profile -> ProfileFragment()
                else -> HomeFragment()
            }
            
            loadFragment(fragment)
            
            bottomNav.postDelayed({
                bottomNav.startAnimation(scaleUp)
            }, 100)
            
            true
        })
    }

    private fun setupTransparentStatusBar() {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    
    // Enable system bar drawing
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    }
    
    // Configure system bars appearance (light icons)
    WindowCompat.getInsetsController(window, window.decorView)?.apply {
        isAppearanceLightStatusBars = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            isAppearanceLightNavigationBars = true
        }
    }
    
    // Apply transparent system bars
    applyTransparentSystemBars()
}

@Suppress("DEPRECATION")
private fun applyTransparentSystemBars() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        window.statusBarColor = Color.TRANSPARENT
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.navigationBarColor = Color.TRANSPARENT
        }
    }
}

    private fun animateBottomNav() {
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_in_from_bottom)
        bottomNav.startAnimation(slideUp)
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}

