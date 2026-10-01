package com.example.donaka100.ui.forms

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.sp
import com.example.donaka100.ui.components.*
import com.example.donaka100.ui.theme.*
import java.time.LocalDate

enum class ModePaiement(val libelle: String) {
    ESPECES("Espèces"), MOBILE_MONEY("Mobile Money"), CREDIT("Crédit")
}

data class FormulaireVente(
    val client: String = "",
    val produit: String = "",
    val quantite: Int = 1,
    val prixUnitaire: Long = 0,
    val mode: ModePaiement = ModePaiement.ESPECES,
    val date: LocalDate = LocalDate.now(),
    val note: String = ""
) {
    val total: Long get() = quantite * prixUnitaire
}

private val CATALOGUE = mapOf(
    "Baguette" to 1000L,
    "Brioche" to 2500L,
    "Pain au chocolat" to 1800L,
    "Gâteau chocolat" to 15000L
)

@Composable
fun VenteFormSheet(
    onDismiss: () -> Unit,
    onSave: (FormulaireVente) -> Unit
) {
    var client by remember { mutableStateOf("") }
    var produit by remember { mutableStateOf("") }
    var quantite by remember { mutableIntStateOf(1) }
    var prix by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf(ModePaiement.ESPECES) }
    var date by remember { mutableStateOf(LocalDate.now()) }
    var note by remember { mutableStateOf("") }
    var tentative by remember { mutableStateOf(false) }   // erreurs visibles après le 1er essai

    val prixLong = prix.toLongOrNull() ?: 0L

    val erreurProduit = if (tentative && produit.isBlank()) "Choisis un produit" else null
    val erreurPrix = if (tentative && prixLong <= 0) "Le prix doit être supérieur à 0" else null
    val erreurClient =
        if (tentative && mode == ModePaiement.CREDIT && client.isBlank())
            "Le nom du client est obligatoire pour une vente à crédit" else null

    DonakaFormSheet(
        titre = "Nouvelle vente",
        labelValider = "Enregistrer la vente",
        onDismiss = onDismiss,
        onValider = {
            tentative = true
            if (erreurProduit == null && erreurPrix == null && erreurClient == null
                && produit.isNotBlank() && prixLong > 0
                && !(mode == ModePaiement.CREDIT && client.isBlank())
            ) {
                onSave(FormulaireVente(client.trim(), produit, quantite, prixLong, mode, date, note.trim()))
            }
        }
    ) {
        DonakaDropdownField(
            label = "Produit",
            options = CATALOGUE.keys.toList(),
            selected = produit,
            onSelect = { produit = it; prix = CATALOGUE[it]?.toString().orEmpty() },
            error = erreurProduit
        )

        DonakaStepper("Quantité", quantite, { quantite = it })

        DonakaAmountField(
            value = prix,
            onValueChange = { prix = it },
            label = "Prix unitaire",
            error = erreurPrix
        )

        Text("Mode de paiement", fontSize = 12.sp, color = TexteGris)
        DonakaSegmentedToggle(
            options = ModePaiement.entries,
            selected = mode,
            onSelect = { mode = it },
            label = { it.libelle }
        )

        DonakaTextField(
            value = client,
            onValueChange = { client = it },
            label = if (mode == ModePaiement.CREDIT) "Client" else "Client (facultatif)",
            leadingIcon = Icons.Default.Person,
            error = erreurClient
        )

        DonakaDateField("Date", date, { date = it })

        DonakaTextField(
            value = note,
            onValueChange = { note = it },
            label = "Note (facultatif)",
            singleLine = false,
            minLines = 2,
            imeAction = ImeAction.Done
        )

        // Récapitulatif
        DonakaCard(containerColor = PrimaireClair) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total", color = Primary, fontWeight = FontWeight.Medium)
                Text(
                    (quantite * prixLong).enMGA(),
                    fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Primary
                )
            }
            if (mode == ModePaiement.CREDIT) {
                DonakaBadge("Sera ajouté aux créances clients", type = TypeBadge.ALERTE)
            }
        }
    }
}