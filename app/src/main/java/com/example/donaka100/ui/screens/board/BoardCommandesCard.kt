package com.example.donaka100.ui.screens.board

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.donaka100.data.LigneCommande
import com.example.donaka100.ui.components.*
import com.example.donaka100.ui.theme.*

@Composable
fun BoardCommandesCard(
    lignes: List<LigneCommande>,
    totalPieces: Int,
    heureLivraison: String,
    isLoading: Boolean
) {
    val liste = remember(lignes) { lignes }

    DonakaCard(isLoading = isLoading) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Commandes de Demain", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                DonakaBadge("$totalPieces pièces", type = TypeBadge.INFO)
            }
        }
        if (liste.isEmpty()) {
            Text("Aucune commande pour demain", fontSize = 13.sp, color = TexteGris)
        } else {
            liste.forEachIndexed { i, l ->
                key("${l.nom}-$i") {
                    if (i > 0) HorizontalDivider(color = SurfaceMoyenne)
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(l.nom, Modifier.weight(1f), fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${l.quantite} pcs", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Primary)
                    }
                }
            }
        }
    }
}