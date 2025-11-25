package com.earnzy.ui.screens

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.earnzy.api.ApiClient
import com.earnzy.data.BalanceResponse
import com.earnzy.data.User
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    var user by remember { mutableStateOf<User?>(null) }
    var balance by remember { mutableStateOf<BalanceResponse?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                user = ApiClient.api.getUser()
                balance = ApiClient.api.getBalance()
                loading = false
            } catch (e: Exception) {
                loading = false
            }
        }
    }

    if (loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Header
            user?.let {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Welcome back!",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Let's earn today",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = "Profile",
                            modifier = Modifier
                                .padding(12.dp)
                                .size(24.dp)
                        )
                    }
                }
            }
        }

        item {
            // Coins Card (Animated)
            AnimatedCoinsCard(balance)
        }

        item {
            // Quick Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatBox(
                    icon = Icons.Default.TrendingUp,
                    label = "Level",
                    value = "${balance?.level ?: 1}",
                    modifier = Modifier.weight(1f)
                )
                StatBox(
                    icon = Icons.Default.TaskAlt,
                    label = "Completed",
                    value = "12",
                    modifier = Modifier.weight(1f)
                )
                StatBox(
                    icon = Icons.Default.EmojiEvents,
                    label = "Streak",
                    value = "5 days",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Text(
                "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionButton(
                    icon = Icons.Default.AssignmentTurnedIn,
                    label = "Tasks",
                    modifier = Modifier.weight(1f),
                    onClick = {}
                )
                ActionButton(
                    icon = Icons.Default.CardGiftcard,
                    label = "Offers",
                    modifier = Modifier.weight(1f),
                    onClick = {}
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionButton(
                    icon = Icons.Default.People,
                    label = "Refer",
                    modifier = Modifier.weight(1f),
                    onClick = {}
                )
                ActionButton(
                    icon = Icons.Default.LocalOffer,
                    label = "Promos",
                    modifier = Modifier.weight(1f),
                    onClick = {}
                )
            }
        }
    }
}

@Composable
fun AnimatedCoinsCard(balance: BalanceResponse?) {
    val scale = animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(800, easing = EaseInOutCubic),
        label = "coin-scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .graphicsLayer { scaleX = scale.value; scaleY = scale.value },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Total Balance",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    )
                    Text(
                        "${balance?.coins ?: 0} Coins",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(
                    Icons.Default.Wallet,
                    contentDescription = "Wallet",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(48.dp)
                )
            }

            // Progress to next level
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Level ${balance?.level ?: 1}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        "${balance?.coins ?: 0}/${balance?.nextLevelCoins ?: 1000}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                LinearProgressIndicator(
                    progress = (balance?.coins?.toFloat() ?: 0f) / (balance?.nextLevelCoins?.toFloat() ?: 1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Composable
fun StatBox(
    icon: androidx.compose.material.icons.Icons.Filled,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(140.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(label, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun ActionButton(
    icon: androidx.compose.material.icons.Icons.Filled,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}
