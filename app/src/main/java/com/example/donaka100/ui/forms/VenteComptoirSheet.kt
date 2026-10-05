package com.example.donaka100.ui.forms

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import com.example.donaka100.data.*
import com.example.donaka100.ui.components.*
import com.example.donaka100.ui.theme.*

/** Vente au comptoir : prix public, payée comptant. Pour du crédit, passer par Commande. */
@Composable
fun VenteComptoirSheet(
    produits: List<Produit>,
    onDismiss: () -> Unit,
    onSave: (NouvelleVenteComptoir) -> Unit
) {
    val quantites = remember { mutableStateMapOf<String, String>() }
    var mode by remember { mutableStateOf(ModeReglement.ESPECES) }
    var tentative by remember { mutableStateOf(false) }

    val total = produits.sumOf { (quantites[it.id]?.toIntOrNull() ?: 0) * it.prixPublic }
    val nb = produits.sumOf { quantites[it.id]?.toIntOrNull() ?: 0 }

    DonakaFormSheet(
        titre = "Nouvelle vente",
        labelValider = "Encaisser la vente",
        onDismiss = onDismiss,
        onValider = {
            tentative = true
            if (total > 0) {
                onSave(
                    NouvelleVenteComptoir(
                        lignes = produits.mapNotNull { p ->
                            quantites[p.id]?.toIntOrNull()?.takeIf { it > 0 }?.let { LigneDemande(p.id, it) }
                        },
                        mode = mode
                    )
                )
            }
        }
    ) {
        Text(
            "Vente au comptoir, au prix public, payée comptant. " +
                    "Pour vendre à crédit, utilise l'onglet Commande.",
            fontSize = 13.sp, color = TexteGris
        )

        if (produits.isEmpty()) {
            Text("Aucun produit. Ajoute-en dans Fourneaux > Recettes.", color = Rouge, fontSize = 13.sp)
        } else {
            produits.forEach { p ->
                val q = quantites[p.id]?.toIntOrNull() ?: 0
                DonakaTextField(
                    value = quantites[p.id].orEmpty(),
                    onValueChange = { quantites[p.id] = it.filter(Char::isDigit).take(4) },
                    label = "${p.nom} · ${p.prixPublic.enMGA()}",
                    suffix = "pcs",
                    helper = if (q > 0) "= ${(q * p.prixPublic).enMGA()}" else null,
                    keyboardType = KeyboardType.Number
                )
            }
        }
        if (tentative && total <= 0) Text("Ajoute au moins un produit", color = Rouge, fontSize = 12.sp)

        DonakaDropdownField(
            label = "Mode de paiement",
            options = ModeReglement.entries.map { it.libelle },
            selected = mode.libelle,
            onSelect = { choix -> mode = ModeReglement.entries.first { it.libelle == choix } }
        )

        DonakaCard(containerColor = PrimaireClair) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("À encaisser", color = Primary, fontWeight = FontWeight.Medium)
                    Text("$nb article${if (nb > 1) "s" else ""}", fontSize = 12.sp, color = Primary)
                }
                Text(total.enMGA(), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Primary)
            }
        }
    }
}