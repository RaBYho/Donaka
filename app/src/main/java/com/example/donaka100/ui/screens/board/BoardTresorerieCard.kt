package com.example.donaka100.ui.screens.board

import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.donaka100.data.BoardData
import com.example.donaka100.ui.components.*
import com.example.donaka100.ui.theme.*

@Composable
fun BoardTresorerieCard(data: BoardData, isLoading: Boolean) {
    DonakaCard(isLoading = isLoading) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("TRÉSORERIE NETTE", fontSize = 11.sp, color = TexteGris, fontWeight = FontWeight.SemiBold)
            val v = data.variationVeille
            DonakaBadge(
                texte = "${if (v > 0) "+" else ""}$v% vs hier",
                type = when {
                    v > 0 -> TypeBadge.SUCCES
                    v < 0 -> TypeBadge.ERREUR
                    else -> TypeBadge.NEUTRE
                }
            )
        }
        Text(data.tresorerie.enMGA(), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TexteFonce)
        HorizontalDivider(color = SurfaceMoyenne)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Stat("CA Jour", data.chiffreAffaires, Vert, Modifier.weight(1f))
            Stat("Achats & Frais", data.achatsEtFrais, Rouge, Modifier.weight(1f))
        }
    }
}

@Composable
private fun Stat(titre: String, valeur: Long, couleur: androidx.compose.ui.graphics.Color, modifier: Modifier) {
    Column(modifier) {
        Text(titre, fontSize = 12.sp, color = couleur, fontWeight = FontWeight.Medium)
        Text(valeur.enMGA(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TexteFonce)
    }
}