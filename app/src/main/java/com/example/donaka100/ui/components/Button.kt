package com.example.donaka100.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.donaka100.ui.theme.*

enum class StyleBouton { PRIMAIRE, SECONDAIRE, CONTOUR, TEXTE, DANGER, SUCCES }

@Composable
fun DonakaButton(
    texte: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: StyleBouton = StyleBouton.PRIMAIRE,
    icone: ImageVector? = null,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    pleineLargeur: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Micro-interaction fluide au clic (scale réactif via GPU)
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled && !isLoading) 0.97f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessHigh),
        label = "buttonScale"
    )

    val m = modifier
        .heightIn(min = 48.dp)
        .then(if (pleineLargeur) Modifier.fillMaxWidth() else Modifier)
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }

    val forme = RoundedCornerShape(12.dp)
    val clic = { if (!isLoading) onClick() }
    val contenu: @Composable RowScope.() -> Unit = { ContenuBouton(texte, icone, isLoading) }

    when (style) {
        StyleBouton.PRIMAIRE -> Button(
            onClick = clic, modifier = m, enabled = enabled, shape = forme,
            interactionSource = interactionSource,
            colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = Color.White),
            content = contenu
        )
        StyleBouton.SECONDAIRE -> FilledTonalButton(
            onClick = clic, modifier = m, enabled = enabled, shape = forme,
            interactionSource = interactionSource,
            colors = ButtonDefaults.filledTonalButtonColors(containerColor = PrimaireClair, contentColor = Primary),
            content = contenu
        )
        StyleBouton.CONTOUR -> OutlinedButton(
            onClick = clic, modifier = m, enabled = enabled, shape = forme,
            interactionSource = interactionSource,
            border = BorderStroke(1.dp, Primary),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
            content = contenu
        )
        StyleBouton.TEXTE -> TextButton(
            onClick = clic, modifier = m, enabled = enabled, shape = forme,
            interactionSource = interactionSource,
            colors = ButtonDefaults.textButtonColors(contentColor = Primary),
            content = contenu
        )
        StyleBouton.DANGER -> Button(
            onClick = clic, modifier = m, enabled = enabled, shape = forme,
            interactionSource = interactionSource,
            colors = ButtonDefaults.buttonColors(containerColor = Rouge, contentColor = Color.White),
            content = contenu
        )
        StyleBouton.SUCCES -> Button(
            onClick = clic, modifier = m, enabled = enabled, shape = forme,
            interactionSource = interactionSource,
            colors = ButtonDefaults.buttonColors(containerColor = Vert, contentColor = Color.White),
            content = contenu
        )
    }
}

@Composable
private fun RowScope.ContenuBouton(texte: String, icone: ImageVector?, isLoading: Boolean) {
    if (isLoading) {
        CircularProgressIndicator(
            modifier = Modifier.size(18.dp),
            strokeWidth = 2.dp,
            color = LocalContentColor.current
        )
    } else if (icone != null) {
        Icon(icone, contentDescription = null, modifier = Modifier.size(18.dp))
    }
    if (isLoading || icone != null) Spacer(Modifier.width(8.dp))
    Text(texte, fontWeight = FontWeight.SemiBold)
}

/** Bouton rond avec icône seule (avec micro-interaction tactile) */
@Composable
fun DonakaIconButton(
    icone: ImageVector,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessHigh),
        label = "iconButtonScale"
    )

    FilledTonalIconButton(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier
            .size(48.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = PrimaireClair, contentColor = Primary
        )
    ) {
        Icon(icone, contentDescription = description)
    }
}

/** Bouton flottant « Ajouter » pour les écrans CRUD avec micro-interaction */
@Composable
fun DonakaFab(
    texte: String,
    icone: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
        label = "fabScale"
    )

    ExtendedFloatingActionButton(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        containerColor = Primary,
        contentColor = Color.White,
        icon = { Icon(icone, contentDescription = null) },
        text = { Text(texte, fontWeight = FontWeight.SemiBold) }
    )
}