package com.earnzy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.earnzy.ui.screens.*
import com.earnzy.ui.theme.EarnzyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EarnzyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "dashboard") {
                        composable("tasks") {
                            TasksScreen()
                        }
                        composable("offerwall") {
                            OfferwallScreen()
                        }
                        composable("referral") {
                            ReferralScreen()
                        }
                        composable("promo") {
                            PromoScreen()
                        }
                        composable("rewards") {
                            RewardsScreen()
                        }
                    }
                }
            }
        }
    }
}
