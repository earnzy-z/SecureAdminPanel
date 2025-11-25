package com.earnzy.app

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.WindowInsetsCompat
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.FirebaseApp
import com.earnzy.app.R

class BanActivity : AppCompatActivity() {

    private lateinit var linear1: LinearLayout
    private lateinit var textview1: TextView
    private lateinit var lottie1: LottieAnimationView
    private lateinit var textview2: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ban)
        initialize(savedInstanceState)
        FirebaseApp.initializeApp(this)
        initializeLogic()
    }

    private fun initialize(savedInstanceState: Bundle?) {
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        linear1 = findViewById(R.id.linear1)
        textview1 = findViewById(R.id.textview1)
        lottie1 = findViewById(R.id.lottie1)
        textview2 = findViewById(R.id.textview2)
    }

    private fun initializeLogic() {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        }

        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = true

        @Suppress("DEPRECATION")
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)

        lottie1.repeatCount = 0
    }

    @Deprecated("Overrides deprecated method")
    override fun onBackPressed() {
        finishAffinity()
    }
}