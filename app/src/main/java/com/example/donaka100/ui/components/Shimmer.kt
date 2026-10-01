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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.shimmer(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val decalage by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing)),
        label = "decalage"
    )
    background(
        Brush.linearGradient(
            colors = listOf(Color(0xFFE7EEFF), Color(0xFFF7F9FF), Color(0xFFE7EEFF)),
            start = Offset(decalage - 600f, 0f),
            end = Offset(decalage, 0f)
        )
    )
}

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