package com.earnzy.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.earnzy.api.ApiClient
import com.earnzy.data.PromoCode
import kotlinx.coroutines.launch

@Composable
fun PromoScreen(
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var promoCodes by remember { mutableStateOf<List<PromoCode>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var showRedeemDialog by remember { mutableStateOf(false) }
    var selectedCode by remember { mutableStateOf<String>("") }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val response = ApiClient.api.getPromoCodes()
                promoCodes = response["promoCodes"] as? List<PromoCode> ?: emptyList()
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "Active Promo Codes",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(promoCodes.size) { index ->
                    val code = promoCodes[index]
                    PromoCard(
                        code = code,
                        onRedeem = {
                            selectedCode = code.code
                            showRedeemDialog = true
                        }
                    )
                }
            }
        }

        if (showRedeemDialog) {
            RedeemPromoDialog(
                code = selectedCode,
                onDismiss = { showRedeemDialog = false },
                onConfirm = {
                    scope.launch {
                        try {
                            ApiClient.api.redeemPromo(mapOf("code" to selectedCode))
                            showRedeemDialog = false
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
fun PromoCard(code: PromoCode, onRedeem: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.LocalOffer, contentDescription = "Promo", tint = MaterialTheme.colorScheme.primary)
                    Text(
                        code.code,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                code.description?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    "+${code.reward} Coins",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Button(
                onClick = onRedeem,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(40.dp)
            ) {
                Text("Redeem")
            }
        }
    }
}

@Composable
fun RedeemPromoDialog(code: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Redeem Promo Code") },
        text = { Text("Redeem code $code for bonus coins?") },
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
