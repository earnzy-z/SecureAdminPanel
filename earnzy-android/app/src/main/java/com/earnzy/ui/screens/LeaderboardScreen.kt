package com.earnzy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.earnzy.api.ApiClient
import kotlinx.coroutines.launch

data class LeaderboardEntry(
    val rank: Int,
    val name: String,
    val coins: Int,
    val level: Int
)

@Composable
fun LeaderboardScreen(modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    var leaderboard by remember { mutableStateOf<List<LeaderboardEntry>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val data = ApiClient.api.getBalance() // Placeholder - would fetch real leaderboard
                leaderboard = listOf(
                    LeaderboardEntry(1, "Player 1", 50000, 50),
                    LeaderboardEntry(2, "Player 2", 45000, 48),
                    LeaderboardEntry(3, "Player 3", 40000, 45),
                    LeaderboardEntry(4, "You", 35000, 42),
                    LeaderboardEntry(5, "Player 5", 30000, 40),
                )
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
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            // Top 3 Podium
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // 2nd Place
                PodiumPlace(rank = 2, height = 120.dp, label = "2nd", coins = 45000)
                
                // 1st Place
                PodiumPlace(rank = 1, height = 160.dp, label = "1st", coins = 50000)
                
                // 3rd Place
                PodiumPlace(rank = 3, height = 90.dp, label = "3rd", coins = 40000)
            }
        }

        item {
            Text(
                "Global Rankings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        itemsIndexed(leaderboard.drop(3)) { index, entry ->
            LeaderboardCard(entry)
        }
    }
}

@Composable
fun PodiumPlace(
    rank: Int,
    height: androidx.compose.ui.unit.Dp,
    label: String,
    coins: Int
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Card(
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
            colors = CardDefaults.cardColors(
                containerColor = when (rank) {
                    1 -> Color(0xFFFFD700)
                    2 -> Color(0xFFC0C0C0)
                    3 -> Color(0xFFCD7F32)
                    else -> MaterialTheme.colorScheme.surface
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = "$rank place",
                    modifier = Modifier.size(32.dp),
                    tint = when (rank) {
                        1 -> Color(0xFF000000)
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    "$coins",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun LeaderboardCard(entry: LeaderboardEntry) {
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
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("#${entry.rank}", fontWeight = FontWeight.Bold)
                    }
                }
                Column {
                    Text(entry.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text("Level ${entry.level}", style = MaterialTheme.typography.labelSmall)
                }
            }
            Text(
                "${entry.coins} coins",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
