package com.example.donaka100.ui.theme

import androidx.compose.ui.unit.dp

// =========================================================================
// ARTISAN WARMTH — échelle 8pt
// Aucune valeur d'espacement ne doit sortir de cette grille.
// =========================================================================

object DonakaSpacing {
    val xs   = 4.dp
    val sm   = 8.dp
    val md   = 16.dp
    val lg   = 24.dp
    val xl   = 32.dp

    /** Gouttière extérieure (padding horizontal des écrans). */
    val margin = 16.dp

    /** Espacement entre colonnes d'une grille interne. */
    val gutter = 16.dp
}

// =========================================================================
// Cibles tactiles — plancher absolu pour un usage au fournil.
// =========================================================================

object DonakaTargets {
    /** Toute zone tapable fait au minimum 44dp. */
    val minTouch = 44.dp

    /** Boutons et champs de saisie principaux. */
    val controlHeight = 48.dp

    /** Bouton d'action primaire pleine largeur. */
    val ctaHeight = 52.dp
}