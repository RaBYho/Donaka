package com.example.donaka100.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.donaka100.ui.theme.*

/* ---------------------------------------------------------------------------
 * Tokens privés à ce fichier (aucun risque de conflit avec d'autres objets)
 * ------------------------------------------------------------------------- */
private object ToggleTokens {
    val ContainerShape = RoundedCornerShape(12.dp)
    val SegmentShape = RoundedCornerShape(9.dp)
    val MinTouchTarget = 48.dp
    val ContainerPadding = 4.dp

    const val ColorAnimMs = 250
    const val DisabledAlpha = 0.38f
}

/* ---------------------------------------------------------------------------
 * Ligne avec interrupteur : toute la ligne est cliquable (zone de toucher large)
 * ------------------------------------------------------------------------- */
@Composable
fun DonakaSwitchRow(
    titre: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    enabled: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    val etat = if (checked) "Activé" else "Désactivé"
    val interaction = remember { MutableInteractionSource() }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .alpha(if (enabled) 1f else ToggleTokens.DisabledAlpha)
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Switch,
                interactionSource = interaction,
                indication = null, // pas d'effet de pression
                onValueChange = {
                    haptic.performHapticFeedback(
                        if (it) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff
                    )
                    onCheckedChange(it)
                }
            )
            .semantics { stateDescription = etat }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                titre,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = TexteFonce
            )
            description?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = TexteGris)
            }
        }
        Spacer(Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = null,
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

/* ---------------------------------------------------------------------------
 * Sélecteur segmenté générique avec pilule glissante
 * ------------------------------------------------------------------------- */
@Composable
fun <T> DonakaSegmentedToggle(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier
) {
    if (options.isEmpty()) return

    val haptic = LocalHapticFeedback.current
    val selectedIndex = options.indexOf(selected).coerceAtLeast(0)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .clip(ToggleTokens.ContainerShape)
            .background(SurfaceMoyenne)
            .padding(ToggleTokens.ContainerPadding)
            .selectableGroup()
    ) {
        val segmentWidth = maxWidth / options.size
        val indicatorOffset by animateDpAsState(
            targetValue = segmentWidth * selectedIndex,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            label = "indicatorOffset"
        )

        // Pilule glissante
        Box(
            Modifier
                .offset(x = indicatorOffset)
                .width(segmentWidth)
                .height(ToggleTokens.MinTouchTarget)
                .shadow(2.dp, ToggleTokens.SegmentShape)
                .background(SurfaceBlanche, ToggleTokens.SegmentShape)
        )

        Row {
            options.forEach { option ->
                key(option) {
                    val actif = option == selected
                    val interaction = remember { MutableInteractionSource() }
                    val textColor by animateColorAsState(
                        targetValue = if (actif) Primary else TexteGris,
                        animationSpec = tween(ToggleTokens.ColorAnimMs),
                        label = "segmentText"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(ToggleTokens.MinTouchTarget)
                            .clip(ToggleTokens.SegmentShape)
                            .selectable(
                                selected = actif,
                                role = Role.RadioButton,
                                interactionSource = interaction,
                                indication = null, // pas d'effet de pression
                                onClick = {
                                    // Haptique uniquement au changement réel,
                                    // mais onSelect reste appelé comme avant.
                                    if (!actif) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                    onSelect(option)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label(option),
                            color = textColor,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (actif) FontWeight.SemiBold else FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}