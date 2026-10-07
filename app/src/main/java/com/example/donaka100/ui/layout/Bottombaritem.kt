package com.example.donaka100.ui.layout

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import com.example.donaka100.ui.navigation.Destination

private const val SELECTED_ICON_SCALE = 1.12f

/**
 * Un onglet de la barre de navigation.
 * Extension de RowScope car NavigationBarItem en dépend.
 */
@Composable
internal fun RowScope.BottomBarItem(
    destination: Destination,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val haptic = LocalHapticFeedback.current

    // Petit pop à la sélection : rebond bref et posé, pas ludique.
    // La valeur est lue dans graphicsLayer (phase de dessin) :
    // l'animation ne déclenche donc aucune recomposition.
    val iconScale = animateFloatAsState(
        targetValue = if (selected) SELECTED_ICON_SCALE else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "bottomNavIconScale",
    )

    NavigationBarItem(
        selected = selected,
        onClick = {
            if (!selected) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onSelect()
            }
        },
        icon = {
            Icon(
                imageVector = if (selected) destination.iconePleine else destination.icone,
                contentDescription = null, // le label porte déjà le sens
                modifier = Modifier.graphicsLayer {
                    scaleX = iconScale.value
                    scaleY = iconScale.value
                },
            )
        },
        label = {
            Text(
                text = destination.titre,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
            )
        },
        alwaysShowLabel = true,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = scheme.primary,
            selectedTextColor = scheme.primary,
            unselectedIconColor = scheme.onSurfaceVariant,
            unselectedTextColor = scheme.onSurfaceVariant,
            indicatorColor = Color.Transparent,
        ),
    )
}