package com.example.donaka100.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Modifier shimmer optimisé avec `graphicsLayer` pour solliciter le GPU
 * et offrir un balayage fluide et apaisant.
 */
fun Modifier.shimmer(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmerTransition")
    val decalage by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1300f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerDecalage"
    )
    this
        .graphicsLayer { alpha = 0.99f } // Rendu sur la couche GPU
        .background(
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFFE8EEF8),
                    Color(0xFFF8FAFF),
                    Color(0xFFE8EEF8)
                ),
                start = Offset(decalage - 650f, 0f),
                end = Offset(decalage, 0f)
            )
        )
}

/**
 * Ligne de squelette fluide pour l'état de chargement initial.
 */
@Composable
fun SkeletonLine(
    largeur: Float = 1f,
    hauteur: Dp = 14.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .fillMaxWidth(largeur)
            .height(hauteur)
            .clip(RoundedCornerShape(6.dp))
            .shimmer()
    )
}