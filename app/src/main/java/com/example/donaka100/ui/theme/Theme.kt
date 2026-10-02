package com.example.donaka100.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

// =========================================================================
// Point d'entrée unique du thème Donaka.
//
// Décisions verrouillées :
//   • Palette figée (Artisan Warmth) — PAS de dynamic color.
//     Les couleurs sémantiques (vert = cash in, rouge = dette,
//     ambre = minuterie) doivent être stables d'un appareil à l'autre.
//   • Light only pour l'instant. Une variante sombre sera ajoutée
//     quand la palette sombre sera spécifiée dans DESIGN.md.
//   • Typographie tabulaire sur les montants (voir DonakaTextStyles).
// =========================================================================

@Composable
fun DonakaTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DonakaLightColorScheme,
        typography  = DonakaTypography,
        shapes      = DonakaShapes,
        content     = content,
    )
}