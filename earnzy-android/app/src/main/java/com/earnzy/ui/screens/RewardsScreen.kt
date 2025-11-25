package com.earnzy.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.earnzy.api.ApiClient
import com.earnzy.data.Reward
import kotlinx.coroutines.launch

@Composable
fun RewardsScreen(
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var rewards by remember { mutableStateOf<List<Reward>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var showRedemptionDialog by remember { mutableStateOf(false) }
    var selectedReward by remember { mutableStateOf<Pair<Reward, Int>?>(null) }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val response = ApiClient.api.getRewards()
                rewards = response.values.flatten()
                loading = false
            } catch (e: Exception) {
                error = e.message
                loading = false
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (error != null) {
            Text("Error: $error", color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        "Withdraw Your Coins",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }

                items(rewards) { reward ->
                    RewardCategoryCard(
                        reward = reward,
                        onSelectReward = { amount ->
                            selectedReward = Pair(reward, amount)
                            showRedemptionDialog = true
                        }
                    )
                }
            }
        }

        if (showRedemptionDialog && selectedReward != null) {
            RedemptionDialog(
                reward = selectedReward!!.first,
                amount = selectedReward!!.second,
                onDismiss = { showRedemptionDialog = false },
                onConfirm = {
                    scope.launch {
                        try {
                            ApiClient.api.requestRedemption(mapOf(
                                "rewardId" to selectedReward!!.first.id,
                                "amount" to selectedReward!!.second,
                                "upiId" to ""
                            ))
                            showRedemptionDialog = false
                        } catch (e: Exception) {
                            // Show error
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun RewardCategoryCard(reward: Reward, onSelectReward: (Int) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.CardGiftcard,
                    contentDescription = reward.name,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(reward.name, style = MaterialTheme.typography.titleMedium)
                    Text("Min. ${reward.minCoins} coins", style = MaterialTheme.typography.labelSmall)
                }
            }

            reward.rewards.forEach { item ->
                RewardOptionButton(
                    label = item.name ?: "₹${item.amount}",
                    coins = item.coins,
                    onClick = { onSelectReward(item.coins) }
                )
            }
        }
    }
}

@Composable
fun RewardOptionButton(label: String, coins: Int, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label)
            Text("$coins coins", color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun RedemptionDialog(reward: Reward, amount: Int, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Redemption") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Reward: ${reward.name}")
                Text("Amount: $amount coins")
                Text("You will receive this within 24 hours.")
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(); onDismiss() }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
