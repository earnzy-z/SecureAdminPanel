package com.earnzy.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.earnzy.api.ApiClient
import com.earnzy.data.User
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    var user by remember { mutableStateOf<User?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                user = ApiClient.api.getUser()
                loading = false
            } catch (e: Exception) {
                loading = false
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            // Profile Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(80.dp)
                    ) {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = "Avatar",
                            modifier = Modifier.padding(16.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    user?.let {
                        Text(
                            it.email,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            Text(
                "Account Stats",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                ProfileStatItem(
                    icon = Icons.Default.AttachMoney,
                    label = "Total Coins Earned",
                    value = "${user?.totalEarned ?: 0}"
                )
                ProfileStatItem(
                    icon = Icons.Default.TrendingUp,
                    label = "Current Balance",
                    value = "${user?.coins ?: 0}"
                )
                ProfileStatItem(
                    icon = Icons.Default.EmojiEvents,
                    label = "Member Since",
                    value = "Jan 2024"
                )
            }
        }

        item {
            Text(
                "Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                SettingItem(
                    icon = Icons.Default.Notifications,
                    label = "Notifications",
                    value = "Enabled"
                )
                SettingItem(
                    icon = Icons.Default.Security,
                    label = "Two-Factor Auth",
                    value = "Disabled"
                )
                SettingItem(
                    icon = Icons.Default.Language,
                    label = "Language",
                    value = "English"
                )
                SettingItem(
                    icon = Icons.Default.Brightness4,
                    label = "Dark Mode",
                    value = "Auto"
                )
            }
        }

        item {
            Button(
                onClick = {
                    scope.launch {
                        ApiClient.api.logout()
                        ApiClient.clearToken()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Logout, contentDescription = "Logout")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout", fontWeight = FontWeight.SemiBold)
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ProfileStatItem(
    icon: androidx.compose.material.icons.Icons.Filled,
    label: String,
    value: String
) {
    Card(shape = RoundedCornerShape(12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    icon,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(label, style = MaterialTheme.typography.bodySmall)
                    Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                }
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SettingItem(
    icon: androidx.compose.material.icons.Icons.Filled,
    label: String,
    value: String
) {
    Card(shape = RoundedCornerShape(12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(icon, contentDescription = label)
                Text(label, style = MaterialTheme.typography.bodyMedium)
            }
            Text(value, style = MaterialTheme.typography.labelSmall)
        }
    }
}
