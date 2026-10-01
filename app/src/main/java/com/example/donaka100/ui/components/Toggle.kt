package com.example.donaka100.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.donaka100.ui.theme.*

/** Ligne avec interrupteur : toute la ligne est cliquable (zone de toucher large) */
@Composable
fun DonakaSwitchRow(
    titre: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Switch,
                onValueChange = onCheckedChange
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(titre, fontWeight = FontWeight.Medium, color = TexteFonce)
            if (description != null) {
                Text(description, fontSize = 12.sp, color = TexteGris)
            }
        }
        Spacer(Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = null,   // c'est la ligne entière qui gère le clic
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Vert,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = TexteGris.copy(alpha = 0.4f),
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

/** Sélecteur segmenté générique : Jour / Semaine / Mois, Entrées / Sorties... */
@Composable
fun <T> DonakaSegmentedToggle(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceMoyenne)
            .padding(4.dp)
    ) {
        options.forEach { option ->
            val actif = option == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 40.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (actif) SurfaceBlanche else Color.Transparent)
                    .selectable(selected = actif, role = Role.Tab, onClick = { onSelect(option) }),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label(option),
                    color = if (actif) Primary else TexteGris,
                    fontWeight = if (actif) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    lineHeight = 15.sp
                )
            }
        }
    }
}