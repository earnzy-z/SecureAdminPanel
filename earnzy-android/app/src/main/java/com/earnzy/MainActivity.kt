package com.earnzy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
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
                    var currentRoute by remember { mutableStateOf("dashboard") }

                    Scaffold(
                        bottomBar = {
                            NavigationBar {
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                                    label = { Text("Home") },
                                    selected = currentRoute == "dashboard",
                                    onClick = {
                                        currentRoute = "dashboard"
                                        navController.navigate("dashboard") {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            lazyRestoreState = true
                                        }
                                    }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.AssignmentTurnedIn, contentDescription = "Tasks") },
                                    label = { Text("Tasks") },
                                    selected = currentRoute == "tasks",
                                    onClick = {
                                        currentRoute = "tasks"
                                        navController.navigate("tasks") {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            lazyRestoreState = true
                                        }
                                    }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.CardGiftcard, contentDescription = "Offers") },
                                    label = { Text("Offers") },
                                    selected = currentRoute == "offerwall",
                                    onClick = {
                                        currentRoute = "offerwall"
                                        navController.navigate("offerwall") {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            lazyRestoreState = true
                                        }
                                    }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.EmojiEvents, contentDescription = "Leaderboard") },
                                    label = { Text("Ranks") },
                                    selected = currentRoute == "leaderboard",
                                    onClick = {
                                        currentRoute = "leaderboard"
                                        navController.navigate("leaderboard") {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            lazyRestoreState = true
                                        }
                                    }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Profile") },
                                    label = { Text("Profile") },
                                    selected = currentRoute == "profile",
                                    onClick = {
                                        currentRoute = "profile"
                                        navController.navigate("profile") {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            lazyRestoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    ) { padding ->
                        NavHost(
                            navController = navController,
                            startDestination = "dashboard",
                            modifier = Modifier.fillMaxSize()
                        ) {
                            composable("dashboard") {
                                DashboardScreen(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(bottom = padding.calculateBottomPadding())
                                )
                            }
                            composable("tasks") {
                                TasksScreen(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(bottom = padding.calculateBottomPadding())
                                )
                            }
                            composable("offerwall") {
                                OfferwallScreen(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(bottom = padding.calculateBottomPadding())
                                )
                            }
                            composable("referral") {
                                ReferralScreen(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(bottom = padding.calculateBottomPadding())
                                )
                            }
                            composable("promo") {
                                PromoScreen(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(bottom = padding.calculateBottomPadding())
                                )
                            }
                            composable("rewards") {
                                RewardsScreen(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(bottom = padding.calculateBottomPadding())
                                )
                            }
                            composable("withdraw") {
                                WithdrawScreen(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(bottom = padding.calculateBottomPadding())
                                )
                            }
                            composable("leaderboard") {
                                LeaderboardScreen(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(bottom = padding.calculateBottomPadding())
                                )
                            }
                            composable("profile") {
                                ProfileScreen(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(bottom = padding.calculateBottomPadding())
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
