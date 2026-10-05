package com.example.donaka100.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.donaka100.ui.theme.*

@Composable
fun DonakaCard(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    onClick: (() -> Unit)? = null,
    containerColor: Color = SurfaceBlanche,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Animation tactile GPU ultra-fluide au press
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null && !isLoading) 0.985f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessHigh),
        label = "cardPressScale"
    )

    val forme = RoundedCornerShape(16.dp)
    val couleurs = CardDefaults.cardColors(containerColor = containerColor)
    val elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)

    val cardModifier = modifier
        .fillMaxWidth()
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }

    val interieur: @Composable ColumnScope.() -> Unit = {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isLoading) {
                SkeletonLine(largeur = 0.4f, hauteur = 12.dp)
                SkeletonLine(largeur = 0.8f, hauteur = 22.dp)
                SkeletonLine(largeur = 0.6f, hauteur = 12.dp)
            } else {
                content()
            }
        }
    }

    if (onClick != null && !isLoading) {
        Card(
            onClick = onClick,
            interactionSource = interactionSource,
            modifier = cardModifier,
            shape = forme,
            colors = couleurs,
            elevation = elevation,
            content = interieur
        )
    } else {
        Card(
            modifier = cardModifier,
            shape = forme,
            colors = couleurs,
            elevation = elevation,
            content = interieur
        )
    }
}

/** Carte « chiffre clé » (CA, trésorerie, créances...) */
@Composable
fun DonakaStatCard(
    titre: String,
    valeur: String,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    couleurValeur: Color = TexteFonce,
    onClick: (() -> Unit)? = null
) {
    DonakaCard(modifier = modifier, isLoading = isLoading, onClick = onClick) {
        Text(titre.uppercase(), fontSize = 11.sp, color = TexteGris, fontWeight = FontWeight.SemiBold)
        Text(valeur, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = couleurValeur)
    }
}