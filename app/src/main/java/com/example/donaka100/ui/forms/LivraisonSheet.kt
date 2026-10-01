package com.example.donaka100.ui.forms

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.donaka100.data.*
import com.example.donaka100.ui.components.*
import com.example.donaka100.ui.theme.*

/** Ouverte quand on coche « livré » : payé maintenant, en partie, ou tout à crédit */
@Composable
fun LivraisonSheet(
    commande: Commande,
    client: Client?,
    onDismiss: () -> Unit,
    onConfirm: (Long, ModeReglement?) -> Unit
) {
    val total = commande.total
    var montant by remember { mutableStateOf(total.toString()) }   // pré-rempli : tout payé
    var mode by remember { mutableStateOf(ModeReglement.ESPECES) }
    var tentative by remember { mutableStateOf(false) }

    val recu = montant.toLongOrNull() ?: 0L
    val reste = total - recu
    val erreur = if (tentative && recu > total) "Le montant dépasse le total de la commande" else null
    val depassePlafond = client != null && client.plafondCreance > 0 && reste > 0 &&
            client.resteDu + reste > client.plafondCreance

    DonakaFormSheet(
        titre = "Livraison · ${commande.clientNom}",
        labelValider = "Confirmer la livraison",
        onDismiss = onDismiss,
        onValider = {
            tentative = true
            if (recu in 0L..total) onConfirm(recu, if (recu > 0) mode else null)
        }
    ) {
        DonakaCard(containerColor = SurfaceMoyenne) {
            Text(commande.resume, fontSize = 13.sp, color = TexteGris)
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total à payer", fontWeight = FontWeight.Medium)
                Text(total.enMGA(), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Primary)
            }
        }

        DonakaAmountField(
            value = montant, onValueChange = { montant = it },
            label = "Montant reçu maintenant",
            helper = "0 = tout à crédit",
            error = erreur,
            imeAction = ImeAction.Done
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DonakaButton("Tout payé", { montant = total.toString() }, style = StyleBouton.TEXTE)
            DonakaButton("Tout à crédit", { montant = "0" }, style = StyleBouton.TEXTE)
        }

        if (recu > 0) {
            DonakaDropdownField(
                label = "Mode de paiement",
                options = ModeReglement.entries.map { it.libelle },
                selected = mode.libelle,
                onSelect = { choix -> mode = ModeReglement.entries.first { it.libelle == choix } }
            )
        }

        if (reste > 0 && recu <= total) {
            DonakaBadge("À crédit : ${reste.enMGA()}", type = TypeBadge.ALERTE)
            if (client != null && depassePlafond) {
                DonakaBadge(
                    "Dépasse le plafond de crédit (${client.plafondCreance.enMGA()})",
                    type = TypeBadge.ERREUR
                )
            }
        }
    }
}