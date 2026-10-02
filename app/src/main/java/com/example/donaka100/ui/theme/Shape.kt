package com.example.donaka100.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// =========================================================================
// ARTISAN WARMTH — architecture des courbes
//
//   Cartes / panels           → large (16dp)
//   Champs, boutons, tabs     → small  (8dp) / medium (12dp)
//   Pills, chips              → CircleShape (à appliquer au cas par cas)
// =========================================================================

internal val DonakaShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),   // micro-badges
    small      = RoundedCornerShape(8.dp),   // inputs, boutons
    medium     = RoundedCornerShape(12.dp),  // chips étendus, tuiles
    large      = RoundedCornerShape(16.dp),  // cartes, modales, sheets
    extraLarge = RoundedCornerShape(24.dp),  // bottom sheets plein écran
)