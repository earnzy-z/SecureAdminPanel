package com.earnzy.app

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.FirebaseApp

class FullMaintenanceActivity : AppCompatActivity() {

    private lateinit var linear1: LinearLayout
    private lateinit var textview7: TextView
    private lateinit var tittle: TextView
    private lateinit var lottie1: LottieAnimationView
    private lateinit var message: TextView
    private lateinit var start_time: TextView
    private lateinit var end_time: TextView
    private lateinit var button1: Button
    private lateinit var textview5: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.full_maintenance)
        FirebaseApp.initializeApp(this)
        initialize()
        initializeLogic()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishAffinity()
            }
        })
    }

    private fun initialize() {
        linear1 = findViewById(R.id.linear1)
        textview7 = findViewById(R.id.textview7)
        tittle = findViewById(R.id.tittle)
        lottie1 = findViewById(R.id.lottie1)
        message = findViewById(R.id.message)
        start_time = findViewById(R.id.start_time)
        end_time = findViewById(R.id.end_time)
        button1 = findViewById(R.id.button1)
        textview5 = findViewById(R.id.textview5)

        button1.setOnClickListener {
            finishAffinity()
        }
    }

    @Suppress("DEPRECATION")
    private fun initializeLogic() {
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
            windowInsetsController.isAppearanceLightStatusBars = true
        }

        val d = resources.displayMetrics.density
        val colors = intArrayOf(0xFF3F51B5.toInt(), 0xFF5C6BC0.toInt())
        val gradientDrawable = android.graphics.drawable.GradientDrawable(
            android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT,
            colors
        )
        gradientDrawable.cornerRadius = d * 10
        button1.elevation = d * 9

        val rippleDrawable = android.graphics.drawable.RippleDrawable(
            android.content.res.ColorStateList.valueOf(0xFFE0E0E0.toInt()),
            gradientDrawable,
            null
        )
        button1.background = rippleDrawable
    }
}