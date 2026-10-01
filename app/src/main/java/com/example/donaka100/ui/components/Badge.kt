package com.example.donaka100.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.donaka100.ui.theme.*

enum class TypeBadge { SUCCES, ALERTE, ERREUR, INFO, NEUTRE }

@Composable
fun DonakaBadge(
    texte: String,
    modifier: Modifier = Modifier,
    type: TypeBadge = TypeBadge.NEUTRE
) {
    val (fond, couleurTexte) = when (type) {
        TypeBadge.SUCCES -> VertClair to Vert
        TypeBadge.ALERTE -> AmbreClair to Ambre
        TypeBadge.ERREUR -> RougeClair to Rouge
        TypeBadge.INFO -> PrimaireClair to Primary
        TypeBadge.NEUTRE -> SurfaceMoyenne to TexteGris
    }
    Text(
        text = texte,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(fond)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        color = couleurTexte,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold
    )
}