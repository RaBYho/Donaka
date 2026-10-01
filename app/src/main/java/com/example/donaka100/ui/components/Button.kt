package com.example.donaka100.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    val m = modifier
        .heightIn(min = 48.dp)
        .then(if (pleineLargeur) Modifier.fillMaxWidth() else Modifier)
    val forme = RoundedCornerShape(12.dp)
    val clic = { if (!isLoading) onClick() }   // ignore les clics pendant le chargement
    val contenu: @Composable RowScope.() -> Unit = { ContenuBouton(texte, icone, isLoading) }

    when (style) {
        StyleBouton.PRIMAIRE -> Button(
            onClick = clic, modifier = m, enabled = enabled, shape = forme,
            colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = Color.White),
            content = contenu
        )
        StyleBouton.SECONDAIRE -> FilledTonalButton(
            onClick = clic, modifier = m, enabled = enabled, shape = forme,
            colors = ButtonDefaults.filledTonalButtonColors(containerColor = PrimaireClair, contentColor = Primary),
            content = contenu
        )
        StyleBouton.CONTOUR -> OutlinedButton(
            onClick = clic, modifier = m, enabled = enabled, shape = forme,
            border = BorderStroke(1.dp, Primary),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
            content = contenu
        )
        StyleBouton.TEXTE -> TextButton(
            onClick = clic, modifier = m, enabled = enabled, shape = forme,
            colors = ButtonDefaults.textButtonColors(contentColor = Primary),
            content = contenu
        )
        StyleBouton.DANGER -> Button(
            onClick = clic, modifier = m, enabled = enabled, shape = forme,
            colors = ButtonDefaults.buttonColors(containerColor = Rouge, contentColor = Color.White),
            content = contenu
        )
        StyleBouton.SUCCES -> Button(
            onClick = clic, modifier = m, enabled = enabled, shape = forme,
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

/** Bouton rond avec icône seule (toujours donner une description : accessibilité) */
@Composable
fun DonakaIconButton(
    icone: ImageVector,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledTonalIconButton(
        onClick = onClick,
        modifier = modifier.size(48.dp),
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = PrimaireClair, contentColor = Primary
        )
    ) {
        Icon(icone, contentDescription = description)
    }
}

/** Bouton flottant « Ajouter » pour les écrans CRUD */
@Composable
fun DonakaFab(
    texte: String,
    icone: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = Primary,
        contentColor = Color.White,
        icon = { Icon(icone, contentDescription = null) },
        text = { Text(texte, fontWeight = FontWeight.SemiBold) }
    )
}