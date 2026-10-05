package com.example.donaka100.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.donaka100.ui.theme.*

/** Niveau de stock : bas = rouge, moyen = ambre, bon = vert */
fun couleurSelonNiveau(progression: Float): Color = when {
    progression < 0.2f -> Rouge
    progression < 0.5f -> Ambre
    else -> Vert
}

/**
 * Barre de progression Donaka avec animation douce (FastOutSlowInEasing) et transition de couleur.
 */
@Composable
fun DonakaProgressBar(
    progression: Float, // entre 0f et 1f
    modifier: Modifier = Modifier,
    titre: String? = null,
    valeurTexte: String? = null,
    couleur: Color = couleurSelonNiveau(progression)
) {
    // Animation douce de la progression de 0 à la valeur finale
    val animee by animateFloatAsState(
        targetValue = progression.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "progressionDonaka"
    )

    // Smooth color change
    val couleurAnimee by animateColorAsState(
        targetValue = couleur,
        animationSpec = tween(durationMillis = 400),
        label = "couleurDonaka"
    )

    Column(modifier.fillMaxWidth().graphicsLayer { alpha = 0.99f }) {
        if (titre != null || valeurTexte != null) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(titre.orEmpty(), fontSize = 12.sp, color = TexteGris)
                Text(valeurTexte.orEmpty(), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = couleurAnimee)
            }
            Spacer(Modifier.height(6.dp))
        }
        LinearProgressIndicator(
            progress = { animee },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50)),
            color = couleurAnimee,
            trackColor = SurfaceMoyenne
        )
    }
}

/** Barre infinie : pour un chargement dont on ignore la durée */
@Composable
fun DonakaLoadingBar(modifier: Modifier = Modifier) {
    LinearProgressIndicator(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(RoundedCornerShape(50))
            .graphicsLayer { alpha = 0.99f },
        color = Primary,
        trackColor = SurfaceMoyenne
    )
}