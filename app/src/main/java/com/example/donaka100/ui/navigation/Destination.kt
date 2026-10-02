package com.example.donaka100.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.outlined.BakeryDining
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Destinations de la navigation principale.
 *
 * Chaque destination porte deux icônes :
 *  - [icone]        : version contour, utilisée pour l'état inactif.
 *  - [iconePleine]  : version pleine, utilisée pour l'état sélectionné.
 *
 * Ce double jeu est la recommandation Material (et le Tip 8 des guidelines
 * UX de navigation basse) : la forme change en plus de la couleur, ce qui
 * rend l'onglet actif identifiable d'un simple coup d'œil, même en plissant
 * les yeux ou sous forte luminosité.
 */
enum class Destination(
    val titre: String,
    val icone: ImageVector,
    val iconePleine: ImageVector,
) {
    BOARD(
        titre = "Board",
        icone = Icons.Outlined.Dashboard,
        iconePleine = Icons.Filled.Dashboard,
    ),
    COMMANDE(
        titre = "Commande",
        icone = Icons.Outlined.Receipt,
        iconePleine = Icons.Filled.Receipt,
    ),
    FOURNEAUX(
        titre = "Fourneaux",
        icone = Icons.Outlined.BakeryDining,
        iconePleine = Icons.Filled.BakeryDining,
    ),
    STOCK(
        titre = "Stock",
        icone = Icons.Outlined.Inventory2,
        iconePleine = Icons.Filled.Inventory2,
    ),
    DEPENSES(
        titre = "Dépenses",
        icone = Icons.Outlined.Payments,
        iconePleine = Icons.Filled.Payments,
    ),
}