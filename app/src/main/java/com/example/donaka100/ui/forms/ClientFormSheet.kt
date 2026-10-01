package com.example.donaka100.ui.forms

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.example.donaka100.data.Client
import com.example.donaka100.data.NouveauClient
import com.example.donaka100.ui.components.*

/** [initial] = null pour une création, un Client pour une modification */
@Composable
fun ClientFormSheet(
    initial: Client? = null,
    onDismiss: () -> Unit,
    onSave: (NouveauClient) -> Unit
) {
    var nom by remember { mutableStateOf(initial?.nom.orEmpty()) }
    var tel by remember { mutableStateOf(initial?.telephone.orEmpty()) }
    var quartier by remember { mutableStateOf(initial?.quartier.orEmpty()) }
    var plafond by remember {
        mutableStateOf(initial?.plafondCreance?.takeIf { it > 0 }?.toString().orEmpty())
    }
    var tentative by remember { mutableStateOf(false) }

    val erreurNom = if (tentative && nom.isBlank()) "Le nom est obligatoire" else null
    val erreurTel = if (tentative && tel.length != 10) "Le numéro doit avoir 10 chiffres" else null

    DonakaFormSheet(
        titre = if (initial == null) "Nouveau compte client" else "Modifier le client",
        onDismiss = onDismiss,
        onValider = {
            tentative = true
            if (nom.isNotBlank() && tel.length == 10) {
                onSave(NouveauClient(nom.trim(), tel, quartier.trim(), plafond.toLongOrNull() ?: 0L))
            }
        }
    ) {
        DonakaTextField(
            value = nom, onValueChange = { nom = it },
            label = "Nom du client ou établissement *",
            placeholder = "Ex : Restaurant Le Glacier",
            leadingIcon = Icons.Default.Person,
            error = erreurNom
        )
        DonakaTextField(
            value = tel,
            onValueChange = { tel = it.filter(Char::isDigit).take(10) },
            label = "Numéro de téléphone *",
            helper = "Ex : 034 12 345 67",
            leadingIcon = Icons.Default.Phone,
            keyboardType = KeyboardType.Phone,
            error = erreurTel
        )
        DonakaTextField(
            value = quartier, onValueChange = { quartier = it },
            label = "Quartier / Adresse de livraison",
            placeholder = "Ex : Bazar Be, Tamatave",
            leadingIcon = Icons.Default.Place
        )
        DonakaAmountField(
            value = plafond, onValueChange = { plafond = it },
            label = "Plafond de créance autorisé",
            helper = "Laisser vide = pas de limite",
            imeAction = ImeAction.Done
        )
    }
}