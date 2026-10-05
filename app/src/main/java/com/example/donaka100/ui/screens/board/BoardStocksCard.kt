package com.example.donaka100.ui.screens.board

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.donaka100.data.Ingredient
import com.example.donaka100.data.StatutStock
import com.example.donaka100.ui.components.*
import com.example.donaka100.ui.theme.*

@Composable
fun BoardStocksCard(stocks: List<Ingredient>, isLoading: Boolean, onGerer: () -> Unit) {
    val listeStocks = remember(stocks) { stocks }

    DonakaCard(isLoading = isLoading) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Stocks sous surveillance", fontWeight = FontWeight.Bold, color = TexteFonce)
            TextButton(onClick = onGerer) { Text("Gérer ›", color = Primary, fontSize = 12.sp) }
        }
        if (listeStocks.isEmpty()) {
            Text("Tous les stocks sont au niveau", fontSize = 13.sp, color = Vert)
        } else {
            listeStocks.forEachIndexed { i, s ->
                key(s.id) {
                    if (i > 0) HorizontalDivider(color = SurfaceMoyenne)
                    val critique = s.statut == StatutStock.CRITIQUE
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(s.nom, fontWeight = FontWeight.SemiBold, color = TexteFonce)
                            Text(
                                "Reste ${s.quantite.avecUnite(s.unite)} " +
                                        (if (critique) "(min. " else "(seuil ") + s.seuil.avecUnite(s.unite) + ")",
                                fontSize = 11.sp, color = if (critique) Rouge else TexteGris
                            )
                        }
                        DonakaBadge(
                            texte = s.statut.libelle,
                            type = if (critique) TypeBadge.ERREUR else TypeBadge.ALERTE
                        )
                    }
                }
            }
        }
    }
}