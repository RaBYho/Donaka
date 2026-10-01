package com.example.donaka100.ui.screens.board

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.donaka100.ui.components.*
import com.example.donaka100.ui.theme.*

@Composable
fun BoardCreancesCard(nombre: Int, total: Long, isLoading: Boolean, onRelancer: () -> Unit) {
    DonakaCard(isLoading = isLoading) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Créances Clients", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TexteFonce)
                    if (nombre > 0) DonakaBadge("$nombre en attente", type = TypeBadge.ERREUR)
                }
                Text(total.enMGA(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TexteFonce)
            }
            if (nombre > 0) DonakaButton("Relancer", onRelancer, style = StyleBouton.SECONDAIRE)
        }
    }
}