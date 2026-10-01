package com.example.donaka100.ui.components

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

@Composable
fun DonakaProgressBar(
    progression: Float,                       // entre 0f et 1f
    modifier: Modifier = Modifier,
    titre: String? = null,
    valeurTexte: String? = null,
    couleur: Color = couleurSelonNiveau(progression)
) {
    val animee by animateFloatAsState(
        targetValue = progression.coerceIn(0f, 1f),
        animationSpec = tween(600),
        label = "progression"
    )
    Column(modifier.fillMaxWidth()) {
        if (titre != null || valeurTexte != null) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(titre.orEmpty(), fontSize = 12.sp, color = TexteGris)
                Text(valeurTexte.orEmpty(), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = couleur)
            }
            Spacer(Modifier.height(6.dp))
        }
        LinearProgressIndicator(
            progress = { animee },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50)),
            color = couleur,
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
            .clip(RoundedCornerShape(50)),
        color = Primary,
        trackColor = SurfaceMoyenne
    )
}