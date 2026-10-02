package com.example.donaka100.ui.layout

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.donaka100.ui.navigation.Destination

/**
 * Barre de navigation principale Donaka.
 *
 * Choix de design (Artisan Warmth) :
 *  - Pas d'indicateur de sélection (respect des maquettes) — l'état actif
 *    est signifié par la couleur terracotta + un léger scale de l'icône.
 *  - Hairline supérieure plutôt qu'ombre portée : plus sobre, cohérent
 *    avec la direction "crisp hairlines, warm tactile" du DESIGN.md.
 *  - Animation spring courte (~250 ms) : confirme le tap sans distraire.
 *
 * Contrainte d'usage : boulanger, une main, écran souvent en plein soleil.
 * Cibles tactiles ≥ 48dp garanties par NavigationBarItem (M3).
 */
@Composable
fun BottomBar(
    courant: Destination,
    onSelection: (Destination) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme

    Column(modifier = Modifier.fillMaxWidth()) {
        // Séparation fine entre le contenu et la barre. Sans elle, la barre
        // se fond dans le fond oatmeal et l'œil ne perçoit pas la frontière.
        HorizontalDivider(
            thickness = 1.dp,
            color = scheme.outlineVariant.copy(alpha = 0.5f),
        )

        NavigationBar(
            containerColor = scheme.background,
            tonalElevation = 0.dp,
        ) {
            Destination.entries.forEach { dest ->
                val selected = courant == dest

                // Petit pop à la sélection. Amorti + rigide = rebond bref
                // et posé, pas ludique. Environ 250 ms perçu.
                val iconScale by animateFloatAsState(
                    targetValue = if (selected) 1.12f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium,
                    ),
                    label = "bottomNavIconScale",
                )

                NavigationBarItem(
                    selected = selected,
                    onClick = { if (!selected) onSelection(dest) },
                    icon = {
                        Icon(
                            imageVector = if (selected) dest.iconePleine else dest.icone,
                            contentDescription = null,
                            modifier = Modifier.scale(iconScale),
                        )
                    },
                    label = {
                        Text(
                            text = dest.titre,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            softWrap = false,
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
        }
    }
}