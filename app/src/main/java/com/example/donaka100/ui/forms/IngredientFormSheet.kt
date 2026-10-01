package com.example.donaka100.ui.forms

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import com.example.donaka100.data.*
import com.example.donaka100.ui.components.*
import com.example.donaka100.ui.theme.*

private fun String.decimalBrut() = filter { it.isDigit() || it == ',' || it == '.' }.take(9)

@Composable
fun IngredientFormSheet(
    initial: Ingredient? = null,
    autresNoms: List<String>,
    rayons: List<String>,
    fournisseurs: List<String>,
    uniteVerrouillee: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (NouvelIngredient) -> Unit
) {
    var nom by remember { mutableStateOf(initial?.nom.orEmpty()) }
    var unite by remember { mutableStateOf(initial?.unite ?: UniteStock.KG) }
    var quantite by remember { mutableStateOf(initial?.quantite?.takeIf { it > 0 }?.enQuantite().orEmpty()) }
    var seuil by remember { mutableStateOf(initial?.seuil?.takeIf { it > 0 }?.enQuantite().orEmpty()) }
    var rayon by remember { mutableStateOf(initial?.rayon.orEmpty()) }
    var fournisseur by remember { mutableStateOf(initial?.fournisseur.orEmpty()) }
    var tentative by remember { mutableStateOf(false) }

    val doublon = nom.isNotBlank() && autresNoms.any { it.cleNom() == nom.cleNom() }
    val quantiteOk = quantite.isBlank() || quantite.enDecimal() != null
    val seuilOk = seuil.isBlank() || seuil.enDecimal() != null
    val ok = nom.isNotBlank() && !doublon && quantiteOk && seuilOk

    DonakaFormSheet(
        titre = if (initial == null) "Nouvel ingrédient" else "Modifier l'ingrédient",
        onDismiss = onDismiss,
        onValider = {
            tentative = true
            if (ok) {
                onSave(
                    NouvelIngredient(
                        nom = nom.nettoyerNom(), unite = unite,
                        quantite = quantite.enDecimal() ?: 0.0, seuil = seuil.enDecimal() ?: 0.0,
                        rayon = rayon.nettoyerNom(), fournisseur = fournisseur.nettoyerNom()
                    )
                )
            }
        }
    ) {
        DonakaTextField(
            value = nom, onValueChange = { nom = it },
            label = "Nom de l'ingrédient *", placeholder = "Ex : Farine T55",
            error = when {
                tentative && nom.isBlank() -> "Le nom est obligatoire"
                doublon -> "Cet ingrédient existe déjà"
                else -> null
            }
        )

        if (uniteVerrouillee) {
            DonakaTextField(
                value = unite.libelle, onValueChange = {}, label = "Unité", enabled = false,
                helper = "Utilisée dans une recette : l'unité ne peut pas changer."
            )
        } else {
            DonakaDropdownField(
                label = "Unité",
                options = UniteStock.entries.map { it.libelle },
                selected = unite.libelle,
                onSelect = { choix -> unite = UniteStock.entries.first { it.libelle == choix } }
            )
        }

        DonakaTextField(
            value = quantite, onValueChange = { quantite = it.decimalBrut() },
            label = if (initial == null) "Quantité en stock (facultatif)" else "Quantité en stock",
            suffix = unite.libelle, keyboardType = KeyboardType.Decimal,
            error = if (tentative && !quantiteOk) "Nombre invalide" else null
        )
        DonakaTextField(
            value = seuil, onValueChange = { seuil = it.decimalBrut() },
            label = "Seuil d'alerte", suffix = unite.libelle, keyboardType = KeyboardType.Decimal,
            helper = "Une alerte s'affiche quand le stock passe sous cette quantité.",
            error = if (tentative && !seuilOk) "Nombre invalide" else null
        )

        DonakaTextField(
            value = rayon, onValueChange = { rayon = it },
            label = "Rayon (facultatif)", leadingIcon = Icons.Default.Place,
            helper = "Ex : Poudres & Farines, Produits frais"
        )
        if (rayons.isNotEmpty()) {
            DonakaFilterChips(options = rayons, selected = rayon.trim(), onSelect = { rayon = it }, label = { it })
        }

        DonakaTextField(
            value = fournisseur, onValueChange = { fournisseur = it },
            label = "Fournisseur (facultatif)", leadingIcon = Icons.Default.Storefront,
            imeAction = ImeAction.Done
        )
        if (fournisseurs.isNotEmpty()) {
            DonakaFilterChips(
                options = fournisseurs, selected = fournisseur.trim(),
                onSelect = { fournisseur = it }, label = { it }
            )
        }
        if (initial != null && quantite.enDecimal() != initial.quantite && quantiteOk) {
            Text(
                "La quantité modifiée sera enregistrée comme un ajustement de stock.",
                fontSize = 12.sp, color = TexteGris
            )
        }
    }
}