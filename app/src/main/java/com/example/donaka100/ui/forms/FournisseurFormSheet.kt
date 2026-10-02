package com.example.donaka100.ui.forms

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import com.example.donaka100.data.NouveauFournisseur
import com.example.donaka100.data.cleNom
import com.example.donaka100.data.nettoyerNom
import com.example.donaka100.ui.Fournisseur
import com.example.donaka100.ui.components.*
import com.example.donaka100.ui.theme.*

/** initial = null : création. Sinon modification, ou « compléter la fiche » si le fournisseur n'en a pas. */
@Composable
fun FournisseurFormSheet(
    initial: Fournisseur? = null,
    autresNoms: List<String>,
    onDismiss: () -> Unit,
    onSave: (NouveauFournisseur) -> Unit
) {
    var nom by remember { mutableStateOf(initial?.nom.orEmpty()) }
    var tel by remember { mutableStateOf(initial?.telephone.orEmpty()) }
    var adresse by remember { mutableStateOf(initial?.adresse.orEmpty()) }
    var delai by remember { mutableStateOf(initial?.delai.orEmpty()) }
    var tentative by remember { mutableStateOf(false) }

    val doublon = nom.isNotBlank() && autresNoms.any { it.cleNom() == nom.cleNom() }
    val telOk = tel.isEmpty() || tel.length == 10
    val ok = nom.isNotBlank() && !doublon && telOk
    val renomme = initial != null && nom.isNotBlank() && nom.nettoyerNom() != initial.nom

    DonakaFormSheet(
        titre = when {
            initial == null -> "Nouveau fournisseur"
            initial.fiche == null -> "Compléter la fiche"
            else -> "Modifier le fournisseur"
        },
        onDismiss = onDismiss,
        onValider = {
            tentative = true
            if (ok) onSave(NouveauFournisseur(nom.nettoyerNom(), tel, adresse.nettoyerNom(), delai.nettoyerNom()))
        }
    ) {
        DonakaTextField(
            value = nom, onValueChange = { nom = it },
            label = "Nom du fournisseur *", leadingIcon = Icons.Default.Storefront,
            placeholder = "Ex : Grossiste Anosibe",
            error = when {
                tentative && nom.isBlank() -> "Le nom est obligatoire"
                doublon -> "Ce fournisseur existe déjà"
                else -> null
            }
        )
        if (renomme) {
            Text(
                "Le nouveau nom sera aussi appliqué à ses ingrédients et à ses achats passés.",
                fontSize = 12.sp, color = TexteGris
            )
        }
        DonakaTextField(
            value = tel, onValueChange = { tel = it.filter(Char::isDigit).take(10) },
            label = "Téléphone (facultatif)", leadingIcon = Icons.Default.Phone,
            helper = "Ex : 034 56 789 01", keyboardType = KeyboardType.Phone,
            error = if (!telOk) "Le numéro doit avoir 10 chiffres" else null
        )
        DonakaTextField(
            value = adresse, onValueChange = { adresse = it },
            label = "Adresse (facultatif)", leadingIcon = Icons.Default.Place,
            placeholder = "Ex : Marché Anosibe, Pavillon 12"
        )
        DonakaTextField(
            value = delai, onValueChange = { delai = it },
            label = "Délai de livraison (facultatif)", leadingIcon = Icons.Default.Schedule,
            helper = "Ex : 24h, Matin même, Retrait direct",
            imeAction = ImeAction.Done
        )
    }
}