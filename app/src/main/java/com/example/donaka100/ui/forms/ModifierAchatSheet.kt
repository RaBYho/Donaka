package com.example.donaka100.ui.forms

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.unit.sp
import com.example.donaka100.data.*
import com.example.donaka100.ui.components.*
import com.example.donaka100.ui.theme.*

@Composable
fun ModifierAchatSheet(
    achat: MouvementStock,
    fournisseurs: List<String>,
    onDismiss: () -> Unit,
    onSave: (ModificationAchat) -> Unit
) {
    var montant by remember { mutableStateOf(achat.montant?.toString().orEmpty()) }
    var fournisseur by remember { mutableStateOf(achat.fournisseur) }
    var mode by remember { mutableStateOf(achat.mode ?: ModeReglement.ESPECES) }

    DonakaFormSheet(
        titre = "Modifier l'achat",
        onDismiss = onDismiss,
        onValider = {
            onSave(
                ModificationAchat(
                    montant = montant.toLongOrNull()?.takeIf { it > 0 },
                    fournisseur = fournisseur.nettoyerNom(),
                    mode = mode
                )
            )
        }
    ) {
        Text(
            "${achat.ingredientNom} · ${achat.quantite.avecUnite(achat.unite)}. " +
                    "Pour corriger la quantité, utilise « Ajuster » dans l'inventaire.",
            fontSize = 13.sp, color = TexteGris
        )
        DonakaAmountField(
            value = montant, onValueChange = { montant = it },
            label = "Montant payé", helper = "Laisser vide si inconnu."
        )
        if (montant.isNotBlank()) {
            DonakaDropdownField(
                label = "Mode de paiement",
                options = ModeReglement.entries.map { it.libelle },
                selected = mode.libelle,
                onSelect = { choix -> mode = ModeReglement.entries.first { it.libelle == choix } }
            )
        }
        DonakaTextField(value = fournisseur, onValueChange = { fournisseur = it }, label = "Fournisseur")
        if (fournisseurs.isNotEmpty()) {
            DonakaFilterChips(
                options = fournisseurs, selected = fournisseur.trim(),
                onSelect = { fournisseur = it }, label = { it }
            )
        }
    }
}