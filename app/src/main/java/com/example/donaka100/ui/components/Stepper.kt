package com.example.donaka100.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.donaka100.ui.theme.*

@Composable
fun DonakaStepper(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    min: Int = 1,
    max: Int = 999
) {
    Row(
        modifier = modifier.fillMaxWidth().heightIn(min = 56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, Modifier.weight(1f), color = TexteFonce, fontWeight = FontWeight.Medium)

        FilledTonalIconButton(
            onClick = { onValueChange((value - 1).coerceAtLeast(min)) },
            enabled = value > min,
            modifier = Modifier.size(48.dp),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = PrimaireClair, contentColor = Primary
            )
        ) { Icon(Icons.Default.Remove, contentDescription = "Diminuer") }

        Text(
            text = value.toString(),
            modifier = Modifier.width(56.dp),
            textAlign = TextAlign.Center,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TexteFonce
        )

        FilledTonalIconButton(
            onClick = { onValueChange((value + 1).coerceAtMost(max)) },
            enabled = value < max,
            modifier = Modifier.size(48.dp),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = PrimaireClair, contentColor = Primary
            )
        ) { Icon(Icons.Default.Add, contentDescription = "Augmenter") }
    }
}