package com.example.donaka100.ui.forms

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.sp
import com.example.donaka100.data.*
import com.example.donaka100.ui.components.*
import com.example.donaka100.ui.theme.*
import java.time.LocalDate

@Composable
fun DepenseFormSheet(
    initial: Depense? = null,
    categories: List<String>,
    onDismiss: () -> Unit,
    onSave: (NouvelleDepense) -> Unit
) {
    var categorie by remember { mutableStateOf(initial?.categorie.orEmpty()) }
    var montant by remember { mutableStateOf(initial?.montant?.toString().orEmpty()) }
    var note by remember { mutableStateOf(initial?.note.orEmpty()) }
    var mode by remember { mutableStateOf(initial?.mode ?: ModeReglement.ESPECES) }
    var date by remember { mutableStateOf(initial?.dateHeure?.toLocalDate() ?: LocalDate.now()) }
    var tentative by remember { mutableStateOf(false) }

    val montantL = montant.toLongOrNull() ?: 0L
    val reservee = categorie.isNotBlank() && categorie.cleNom() == CATEGORIE_ACHATS.cleNom()
    val categorieOk = categorie.isNotBlank() && !reservee
    val dateOk = !date.isAfter(LocalDate.now())
    val ok = categorieOk && montantL > 0 && dateOk

    DonakaFormSheet(
        titre = if (initial == null) "Ajouter une dépense" else "Modifier la dépense",
        labelValider = "Enregistrer la dépense",
        onDismiss = onDismiss,
        onValider = {
            tentative = true
            if (ok) onSave(NouvelleDepense(categorie.nettoyerNom(), montantL, note.trim(), date, mode))
        }
    ) {
        DonakaTextField(
            value = categorie, onValueChange = { categorie = it },
            label = "Catégorie *", placeholder = "Ex : Transport, JIRAMA, Emballages",
            error = when {
                reservee -> "Nom réservé aux achats de stock (voir l'onglet Stock)"
                tentative && categorie.isBlank() -> "Indique une catégorie"
                else -> null
            }
        )
        if (categories.isNotEmpty()) {
            DonakaFilterChips(
                options = categories, selected = categorie.trim(),
                onSelect = { categorie = it }, label = { it }
            )
        }

        DonakaAmountField(
            value = montant, onValueChange = { montant = it },
            label = "Montant décaissé *",
            error = if (tentative && montantL <= 0) "Saisis un montant supérieur à 0" else null
        )
        DonakaFilterChips(
            options = listOf(5_000L, 15_000L, 30_000L, 50_000L),
            selected = montantL,
            onSelect = { montant = it.toString() },
            label = { it.toString().avecEspacesMilliers() }
        )

        DonakaTextField(
            value = note, onValueChange = { note = it },
            label = "Note / justification (facultatif)",
            placeholder = "Ex : Réparation pétrin, achat sel fin…",
            imeAction = ImeAction.Done
        )

        DonakaDropdownField(
            label = "Mode de paiement",
            options = ModeReglement.entries.map { it.libelle },
            selected = mode.libelle,
            onSelect = { choix -> mode = ModeReglement.entries.first { it.libelle == choix } }
        )

        DonakaDateField("Date", date, { date = it })
        if (!dateOk) Text("La date ne peut pas être dans le futur.", color = Rouge, fontSize = 12.sp)
    }
}