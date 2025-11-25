package com.earnzy.app

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.viewpager2.widget.ViewPager2

class TutorialActivity : AppCompatActivity() {

    private lateinit var tutorialViewPager: ViewPager2
    private lateinit var btnNext: Button
    private lateinit var tvSkip: TextView
    private lateinit var tvPageCounter: TextView
    private lateinit var pagerAdapter: TutorialPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        setContentView(R.layout.activity_tutorial)

        tutorialViewPager = findViewById(R.id.tutorialViewPager)
        btnNext = findViewById(R.id.btn_next)
        tvSkip = findViewById(R.id.tv_skip)
        tvPageCounter = findViewById(R.id.tv_page_counter)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT
        }

        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = !isNightMode()
        pagerAdapter = TutorialPagerAdapter(this)
        tutorialViewPager.adapter = pagerAdapter


        tutorialViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                tvPageCounter.text = "${position + 1} / ${pagerAdapter.itemCount}"

                if (position == pagerAdapter.itemCount - 1) {
                    btnNext.text = "Get Started"
                    animateButton()
                } else {
                    btnNext.text = "Next"
                }
            }
        })

        tvPageCounter.text = "1 / ${pagerAdapter.itemCount}"

        btnNext.setOnClickListener {
            if (tutorialViewPager.currentItem < pagerAdapter.itemCount - 1) {
                tutorialViewPager.currentItem += 1
            } else {
                navigateToLogin()
            }
        }

        tvSkip.setOnClickListener { navigateToLogin() }
    }

    private fun navigateToLogin() {
        val prefs: SharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        prefs.edit().putBoolean("isFirstLaunch", false).apply()

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun animateButton() {
        btnNext.apply {
            scaleX = 0.9f
            scaleY = 0.9f
            animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setDuration(300)
                .start()
        }
    }

    private fun isNightMode(): Boolean {
        val uiMode = resources.configuration.uiMode
        val nightMask = uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
        return nightMask == android.content.res.Configuration.UI_MODE_NIGHT_YES
    }
}