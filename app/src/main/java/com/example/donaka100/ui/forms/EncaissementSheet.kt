package com.example.donaka100.ui.forms

import androidx.compose.runtime.*
import androidx.compose.ui.text.input.ImeAction
import com.example.donaka100.data.Client
import com.example.donaka100.data.ModeReglement
import com.example.donaka100.ui.components.*

@Composable
fun EncaissementSheet(
    client: Client,
    onDismiss: () -> Unit,
    onConfirm: (Long, ModeReglement) -> Unit
) {
    var montant by remember { mutableStateOf(client.resteDu.toString()) }   // pré-rempli : tout payer
    var mode by remember { mutableStateOf(ModeReglement.ESPECES) }
    var tentative by remember { mutableStateOf(false) }
    val valeur = montant.toLongOrNull() ?: 0L

    val erreur = when {
        !tentative -> null
        valeur <= 0 -> "Saisis un montant supérieur à 0"
        valeur > client.resteDu -> "Le montant dépasse ce que le client doit"
        else -> null
    }

    DonakaFormSheet(
        titre = "Encaisser · ${client.nom}",
        labelValider = "Encaisser",
        onDismiss = onDismiss,
        onValider = {
            tentative = true
            if (valeur in 1..client.resteDu) onConfirm(valeur, mode)
        }
    ) {
        DonakaAmountField(
            value = montant, onValueChange = { montant = it },
            label = "Montant reçu",
            helper = "Reste dû : ${client.resteDu.enMGA()}",
            error = erreur,
            imeAction = ImeAction.Done
        )
        DonakaButton(
            texte = "Tout régler (${client.resteDu.enMGA()})",
            onClick = { montant = client.resteDu.toString() },
            style = StyleBouton.TEXTE
        )
        DonakaDropdownField(
            label = "Mode de paiement",
            options = ModeReglement.entries.map { it.libelle },
            selected = mode.libelle,
            onSelect = { choix -> mode = ModeReglement.entries.first { it.libelle == choix } }
        )
    }
}