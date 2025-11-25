package com.earnzy.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun AnimatedCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    content: @Composable () -> Unit
) {
    var isFlipped by remember { mutableStateOf(false) }
    
    val rotation = animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "card-flip"
    )

    Card(
        modifier = modifier
            .graphicsLayer {
                rotationY = rotation.value
                cameraDistance = 8 * density
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(),
        onClick = { isFlipped = !isFlipped }
    ) {
        content()
    }
}

@Composable
fun PulseAnimation(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse-scale"
    )

    Box(
        modifier = modifier.graphicsLayer { scaleX = scale; scaleY = scale }
    ) {
        content()
    }
}

@Composable
fun ShimmerAnimation(
    modifier: Modifier = Modifier
) {
    val shimmerColors = listOf(
        androidx.compose.ui.graphics.Color.LightGray.copy(alpha = 0.6f),
        androidx.compose.ui.graphics.Color.LightGray.copy(alpha = 0.2f),
        androidx.compose.ui.graphics.Color.LightGray.copy(alpha = 0.6f),
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer-translate"
    )

    Box(modifier = modifier.background(shimmerColors[0]))
}
