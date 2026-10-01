package com.example.donaka100.ui.screens.commande

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.donaka100.ui.components.*
import com.example.donaka100.ui.theme.*

@Composable
fun CreancesBanner(total: Long, nbDebiteurs: Int, isLoading: Boolean) {
    DonakaCard(isLoading = isLoading) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Créances en cours", fontSize = 12.sp, color = TexteGris, fontWeight = FontWeight.Medium)
                Text(total.enMGA(), fontSize = 26.sp, fontWeight = FontWeight.Bold, color = TexteFonce)
            }
            if (nbDebiteurs > 0) {
                DonakaBadge(
                    "$nbDebiteurs client${if (nbDebiteurs > 1) "s" else ""}",
                    type = TypeBadge.ERREUR
                )
            }
        }
    }
}