package com.example.donaka100.ui.screens.board

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.donaka100.data.StatutStock
import com.example.donaka100.data.StockSurveille
import com.example.donaka100.ui.components.*
import com.example.donaka100.ui.theme.*

@Composable
fun BoardStocksCard(stocks: List<StockSurveille>, isLoading: Boolean, onGerer: () -> Unit) {
    DonakaCard(isLoading = isLoading) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Stocks sous surveillance", fontWeight = FontWeight.Bold, color = TexteFonce)
            TextButton(onClick = onGerer) { Text("Gérer ›", color = Primary, fontSize = 12.sp) }
        }
        if (stocks.isEmpty()) {
            Text("Tous les stocks sont au niveau", fontSize = 13.sp, color = Vert)
        } else {
            stocks.forEachIndexed { i, s ->
                if (i > 0) HorizontalDivider(color = SurfaceMoyenne)
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(s.nom, fontWeight = FontWeight.SemiBold, color = TexteFonce)
                        val alerte = s.statut == StatutStock.ALERTE
                        Text(
                            text = if (alerte) "Reste ${s.quantiteKg} kg (min. ${s.seuilKg} kg)"
                            else "Reste ${s.quantiteKg} kg (seuil ${s.seuilKg} kg)",
                            fontSize = 11.sp,
                            color = if (alerte) Rouge else TexteGris
                        )
                    }
                    DonakaBadge(
                        texte = if (s.statut == StatutStock.ALERTE) "Alerte" else "Seuil juste",
                        type = if (s.statut == StatutStock.ALERTE) TypeBadge.ERREUR else TypeBadge.ALERTE
                    )
                }
            }
        }
    }
}