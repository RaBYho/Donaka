package com.example.donaka100.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.donaka100.ui.theme.*

/** Pastilles de filtre défilantes, une seule sélection à la fois */
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
            FilterChip(
                selected = actif,
                onClick = { onSelect(option) },
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