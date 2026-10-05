package com.example.donaka100.ui.screens.board

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.donaka100.ui.components.DonakaBadge
import com.example.donaka100.ui.components.TypeBadge
import com.example.donaka100.ui.theme.TexteFonce

@Composable
fun BoardGreeting(nom: String, fournilOuvert: Boolean?) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (nom.isBlank()) "Bonjour 👋" else "Bonjour $nom 👋",
            modifier = Modifier.weight(1f, fill = false),
            fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TexteFonce,
            maxLines = 1, overflow = TextOverflow.Ellipsis
        )
        if (fournilOuvert != null) {
            Spacer(Modifier.width(8.dp))
            DonakaBadge(
                texte = if (fournilOuvert) "FOURNIL OUVERT" else "FOURNIL FERMÉ",
                type = if (fournilOuvert) TypeBadge.SUCCES else TypeBadge.NEUTRE
            )
        }
    }
}