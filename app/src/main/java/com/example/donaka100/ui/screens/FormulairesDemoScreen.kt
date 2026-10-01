package com.example.donaka100.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.donaka100.ui.components.*
import com.example.donaka100.ui.forms.FormulaireVente
import com.example.donaka100.ui.forms.VenteFormSheet

@Composable
fun FormulairesDemoScreen() {
    var afficher by remember { mutableStateOf(false) }
    var derniere by remember { mutableStateOf<FormulaireVente?>(null) }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DonakaButton("Nouvelle vente", { afficher = true }, icone = Icons.Default.Add, pleineLargeur = true)

        DonakaCard {
            val v = derniere
            if (v == null) Text("Aucune vente enregistrée")
            else {
                Text("${v.quantite} × ${v.produit}")
                Text("Total : ${v.total.enMGA()}")
                Text("Paiement : ${v.mode.libelle}")
                Text("Client : ${v.client.ifBlank { "—" }}")
            }
        }
    }

    if (afficher) {
        VenteFormSheet(
            onDismiss = { afficher = false },
            onSave = { derniere = it; afficher = false }
        )
    }
}