package com.earnzy.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.earnzy.api.ApiClient
import com.earnzy.data.ReferralStats
import kotlinx.coroutines.launch

@Composable
fun ReferralScreen(
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var stats by remember { mutableStateOf<ReferralStats?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val code = ApiClient.api.getReferralCode()
                stats = ApiClient.api.getReferralStats().copy(
                    // Add code info to stats
                )
                loading = false
            } catch (e: Exception) {
                error = e.message
                loading = false
            }
        }
    }

    if (loading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (error != null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Error: $error", color = MaterialTheme.colorScheme.error)
        }
    } else if (stats != null) {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Referral Code Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Your Referral Code",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surface,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "REF123456",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Button(
                                onClick = {
                                    // Copy to clipboard
                                },
                                modifier = Modifier.height(36.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                            }
                        }

                        Button(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "Join Earnzy with my referral code: REF123456\nhttps://earnzy.com/ref/REF123456")
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Referral Code"))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Share Code")
                        }
                    }
                }
            }

            item {
                // Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        label = "Total Referrals",
                        value = stats!!.totalReferrals.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "Earned Coins",
                        value = stats!!.earnedCoins.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "Active",
                        value = stats!!.activeReferrals.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Text(
                    "Recent Referrals",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            items(stats!!.referrals) { referral ->
                ReferralItemCard(referral)
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(value, style = MaterialTheme.typography.headlineSmall)
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun ReferralItemCard(referral: Any) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("User joined", style = MaterialTheme.typography.bodySmall)
                Text("+50 Coins", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
            Text("2 days ago", style = MaterialTheme.typography.labelSmall)
        }
    }
}
