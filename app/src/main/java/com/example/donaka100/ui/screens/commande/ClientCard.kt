package com.example.donaka100.ui.screens.commande

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.donaka100.data.Client
import com.example.donaka100.data.TypeClient
import com.example.donaka100.ui.components.*
import com.example.donaka100.ui.theme.*

private fun TypeClient.icone(): ImageVector = when (this) {
    TypeClient.EPICERIE -> Icons.Default.Storefront
    TypeClient.BOULANGERIE -> Icons.Default.BakeryDining
    TypeClient.RESTAURANT -> Icons.Default.Restaurant
    TypeClient.AUTRE -> Icons.Default.Person
}

@Composable
fun ClientCard(
    client: Client,
    onEncaisser: () -> Unit,
    onOptions: () -> Unit,
    onAppeler: () -> Unit
) {
    DonakaCard {
        // En-tête : icône, nom, contact, menu
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Box(
                Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(PrimaireClair),
                contentAlignment = Alignment.Center
            ) { Icon(client.type.icone(), null, tint = Primary, modifier = Modifier.size(20.dp)) }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    client.nom, fontWeight = FontWeight.Bold, color = TexteFonce,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Row(
                    Modifier.clickable(onClick = onAppeler).heightIn(min = 32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Call, null, tint = TexteGris, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        buildString {
                            append(client.telephone.enTelephone())
                            if (client.quartier.isNotBlank()) append(" • ${client.quartier}")
                        },
                        fontSize = 12.sp, color = TexteGris,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }
            }

            IconButton(onClick = onOptions) {
                Icon(Icons.Default.MoreVert, contentDescription = "Options de ${client.nom}", tint = TexteGris)
            }
        }

        // Solde
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceMoyenne.copy(alpha = 0.5f))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (client.aJour) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, null, tint = Vert, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Solde à jour", color = Vert, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
                Text(0L.enMGA(), color = Vert, fontWeight = FontWeight.Bold)
            } else {
                Column(Modifier.weight(1f)) {
                    Text("Reste dû", fontSize = 11.sp, color = TexteGris)
                    if (client.detailDette.isNotBlank()) {
                        Text(client.detailDette, fontSize = 12.sp, color = TexteGris, maxLines = 2)
                    }
                }
                Spacer(Modifier.width(8.dp))
                Text(client.resteDu.enMGA(), color = Rouge, fontWeight = FontWeight.Bold)
            }
        }

        if (client.plafondAtteint) {
            DonakaBadge("Plafond de crédit atteint", type = TypeBadge.ALERTE)
        }

        if (!client.aJour) {
            DonakaButton(
                texte = "Encaisser Règlement", onClick = onEncaisser,
                style = StyleBouton.SUCCES, icone = Icons.Default.Payments,
                pleineLargeur = true
            )
        }
    }
}