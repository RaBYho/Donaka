package com.example.donaka100.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.donaka100.ui.theme.*

/** Pastilles de filtre défilantes avec micro-interactions et transitions fluides */
@Composable
fun <T> DonakaFilterChips(
    options: List<T>,
    selected: T?,
    onSelect: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            val actif = option == selected
            val scale by animateFloatAsState(
                targetValue = if (actif) 1.02f else 1.0f,
                animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                label = "chipScale"
            )

            FilterChip(
                selected = actif,
                onClick = { onSelect(option) },
                modifier = Modifier.graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
                label = {
                    Text(
                        label(option), fontSize = 12.sp,
                        fontWeight = if (actif) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                border = null,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = SurfaceBlanche,
                    labelColor = TexteFonce,
                    selectedContainerColor = Primary,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}