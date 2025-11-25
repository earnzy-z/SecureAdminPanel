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
import kotlinx.coroutines.launch

@Composable
fun WithdrawScreen(modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    var selectedMethod by remember { mutableStateOf<String?>(null) }
    var selectedAmount by remember { mutableStateOf(0) }
    var upiId by remember { mutableStateOf("") }
    var showConfirmDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Withdraw Coins",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Choose your preferred withdrawal method",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            WithdrawalMethodCard(
                icon = Icons.Default.AttachMoney,
                name = "Paytm Wallet",
                selected = selectedMethod == "paytm",
                onSelect = { selectedMethod = "paytm" }
            )
        }

        item {
            WithdrawalMethodCard(
                icon = Icons.Default.AccountBalanceWallet,
                name = "Bank Transfer (UPI)",
                selected = selectedMethod == "upi",
                onSelect = { selectedMethod = "upi" }
            )
        }

        item {
            WithdrawalMethodCard(
                icon = Icons.Default.CardGiftcard,
                name = "Gift Cards",
                selected = selectedMethod == "gift",
                onSelect = { selectedMethod = "gift" }
            )
        }

        if (selectedMethod != null) {
            item {
                Text(
                    "Select Amount",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(100, 500, 1000, 2000).forEach { amount ->
                        AmountButton(
                            amount = amount,
                            selected = selectedAmount == amount,
                            onClick = { selectedAmount = amount }
                        )
                    }
                }
            }

            if (selectedMethod == "upi") {
                item {
                    TextField(
                        value = upiId,
                        onValueChange = { upiId = it },
                        label = { Text("UPI ID") },
                        placeholder = { Text("user@paytm") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }

            item {
                Button(
                    onClick = { showConfirmDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = selectedAmount > 0,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Withdraw ₹$selectedAmount", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    if (showConfirmDialog) {
        ConfirmWithdrawalDialog(
            amount = selectedAmount,
            method = selectedMethod ?: "",
            onDismiss = { showConfirmDialog = false },
            onConfirm = {
                scope.launch {
                    try {
                        ApiClient.api.requestRedemption(mapOf(
                            "rewardId" to (selectedMethod ?: ""),
                            "amount" to selectedAmount,
                            "upiId" to upiId
                        ))
                        showConfirmDialog = false
                        selectedAmount = 0
                        selectedMethod = null
                    } catch (e: Exception) {
                        // Handle error
                    }
                }
            }
        )
    }
}

@Composable
fun WithdrawalMethodCard(
    icon: androidx.compose.material.icons.Icons.Filled,
    name: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        onClick = onSelect
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = name, modifier = Modifier.size(32.dp))
                Text(name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            }
            if (selected) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun AmountButton(amount: Int, selected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text("₹$amount", fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun ConfirmWithdrawalDialog(
    amount: Int,
    method: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Withdrawal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Amount: ₹$amount")
                Text("Method: ${method.capitalize()}")
                Text("You will receive the amount within 24 hours.", style = MaterialTheme.typography.labelSmall)
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
