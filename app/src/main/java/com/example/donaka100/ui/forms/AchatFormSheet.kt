package com.example.donaka100.ui.forms

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import com.example.donaka100.data.*
import com.example.donaka100.ui.components.*
import com.example.donaka100.ui.theme.*

@Composable
fun AchatFormSheet(
    ingredients: List<Ingredient>,
    fournisseurs: List<String>,
    nomInitial: String = "",
    fournisseurInitial: String = "",
    onDismiss: () -> Unit,
    onSave: (NouvelAchat) -> Unit
) {
    var nom by remember { mutableStateOf(nomInitial) }
    var unite by remember { mutableStateOf(UniteStock.KG) }
    var quantite by remember { mutableStateOf("") }
    var montant by remember { mutableStateOf("") }
    var fournisseur by remember { mutableStateOf(fournisseurInitial) }
    var tentative by remember { mutableStateOf(false) }
    var mode by remember { mutableStateOf(ModeReglement.ESPECES) }
    val ing = if (nom.isBlank()) null else ingredients.firstOrNull { it.nom.cleNom() == nom.cleNom() }
    val uniteEff = ing?.unite ?: unite
    val q = quantite.enDecimal() ?: 0.0
    val suggestions =
        if (nom.isBlank()) emptyList()
        else ingredients.map { it.nom }.filter {
            it.cleNom().contains(nom.cleNom()) && it.cleNom() != nom.cleNom()
        }.take(4)

    // Reprend le fournisseur habituel quand l'ingrédient est reconnu
    LaunchedEffect(ing?.id) {
        if (ing != null && fournisseur.isBlank()) fournisseur = ing.fournisseur
    }

    DonakaFormSheet(
        titre = "Effectuer un achat",
        labelValider = "Enregistrer l'achat",
        onDismiss = onDismiss,
        onValider = {
            tentative = true
            if (nom.isNotBlank() && q > 0) {
                onSave(
                    NouvelAchat(
                        nom = nom.nettoyerNom(), unite = uniteEff, quantite = q,
                        montant = montant.toLongOrNull()?.takeIf { it > 0 },
                        fournisseur = fournisseur.nettoyerNom()
                    )
                )
            }
        }
    ) {
        DonakaTextField(
            value = nom, onValueChange = { nom = it },
            label = "Ingrédient *", placeholder = "Ex : Farine T55",
            helper = when {
                nom.isBlank() -> null
                ing != null -> "Déjà en stock : ${ing.quantite.avecUnite(ing.unite)}"
                else -> "Nouveau : sera ajouté au stock"
            },
            error = if (tentative && nom.isBlank()) "Indique l'ingrédient acheté" else null
        )
        if (suggestions.isNotEmpty()) {
            DonakaFilterChips(options = suggestions, selected = "", onSelect = { nom = it }, label = { it })
        }
        if (nom.isNotBlank() && ing == null) {
            DonakaDropdownField(
                label = "Unité",
                options = UniteStock.entries.map { it.libelle },
                selected = unite.libelle,
                onSelect = { choix -> unite = UniteStock.entries.first { it.libelle == choix } }
            )
        }

        DonakaTextField(
            value = quantite,
            onValueChange = { quantite = it.filter { c -> c.isDigit() || c == ',' || c == '.' }.take(9) },
            label = "Quantité reçue *", suffix = uniteEff.libelle, keyboardType = KeyboardType.Decimal,
            error = if (tentative && q <= 0) "Saisis une quantité supérieure à 0" else null
        )
        DonakaAmountField(
            value = montant, onValueChange = { montant = it },
            label = "Montant payé (facultatif)",
            helper = "Déduit de la trésorerie. Facultatif."
        )
        if (montant.isNotBlank()) {
            DonakaDropdownField(
                label = "Mode de paiement",
                options = ModeReglement.entries.map { it.libelle },
                selected = mode.libelle,
                onSelect = { choix -> mode = ModeReglement.entries.first { it.libelle == choix } }
            )
        }
        DonakaTextField(
            value = fournisseur, onValueChange = { fournisseur = it },
            label = "Fournisseur (facultatif)"
        )
        if (fournisseurs.isNotEmpty()) {
            DonakaFilterChips(
                options = fournisseurs, selected = fournisseur.trim(),
                onSelect = { fournisseur = it }, label = { it }
            )
        }

        if (ing != null && q > 0) {
            Text(
                "Stock : ${ing.quantite.avecUnite(ing.unite)} → ${(ing.quantite + q).avecUnite(ing.unite)}",
                fontSize = 13.sp, color = Vert
            )
        }
    }
}