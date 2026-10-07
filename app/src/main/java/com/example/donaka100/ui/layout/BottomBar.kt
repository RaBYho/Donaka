package com.example.donaka100.ui.layout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.donaka100.ui.navigation.Destination

/**
 * Barre de navigation principale Donaka.
 *
 * Choix de design (Artisan Warmth) :
 *  - Pas d'indicateur de sélection (respect des maquettes) : l'état actif
 *    est signifié par la couleur terracotta, l'icône pleine et un léger scale.
 *  - Hairline supérieure plutôt qu'ombre portée : sobre, cohérent avec
 *    la direction "crisp hairlines, warm tactile" du DESIGN.md.
 *  - Pas de ripple au toucher : le changement d'état suffit comme retour.
 *
 * Contrainte d'usage : boulanger, une main, écran souvent en plein soleil.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomBar(
    courant: Destination,
    onSelection: (Destination) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme

    Column(modifier = Modifier.fillMaxWidth()) {
        // Sans cette ligne, la barre se fond dans le fond oatmeal.
        HorizontalDivider(
            thickness = 1.dp,
            color = scheme.outlineVariant.copy(alpha = 0.5f),
        )

        // null = aucun ripple sur les items de la barre
        CompositionLocalProvider(LocalRippleConfiguration provides null) {
            NavigationBar(
                containerColor = scheme.background,
                tonalElevation = 0.dp,
            ) {
                Destination.entries.forEach { dest ->
                    BottomBarItem(
                        destination = dest,
                        selected = courant == dest,
                        onSelect = { onSelection(dest) },
                    )
                }
            }
        }
    }
}