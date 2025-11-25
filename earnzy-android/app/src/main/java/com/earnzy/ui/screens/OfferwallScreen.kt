package com.earnzy.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.earnzy.api.ApiClient
import com.earnzy.data.Offer
import kotlinx.coroutines.launch

@Composable
fun OfferwallScreen(
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var wall by remember { mutableStateOf<Map<String, List<Offer>>>(emptyMap()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                wall = ApiClient.api.getOfferWall()
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
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            wall.forEach { (category, offers) ->
                item {
                    Text(
                        category,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                items(offers) { offer ->
                    OfferCard(offer)
                }
            }
        }
    }
}

@Composable
fun OfferCard(offer: Offer) {
    val scope = rememberCoroutineScope()
    var claiming by remember { mutableStateOf(false) }
    var claimed by remember { mutableStateOf(offer.claimedAt != null) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Image
            if (offer.imageUrl != null) {
                AsyncImage(
                    model = offer.imageUrl,
                    contentDescription = offer.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            // Info
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    offer.title,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "+${offer.reward} Coins",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Button(
                        onClick = {
                            scope.launch {
                                claiming = true
                                try {
                                    ApiClient.api.claimOffer(offer.id)
                                    claimed = true
                                } catch (e: Exception) {
                                    // Show error
                                }
                                claiming = false
                            }
                        },
                        enabled = !claimed && !claiming,
                        modifier = Modifier.height(28.dp),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        if (claimed) {
                            Icon(Icons.Default.Check, contentDescription = "Claimed", modifier = Modifier.size(16.dp))
                        } else {
                            Text("Claim", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}
