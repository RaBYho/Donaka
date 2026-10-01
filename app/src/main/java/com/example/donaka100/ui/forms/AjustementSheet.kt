package com.example.donaka100.ui.forms

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import com.example.donaka100.data.Ingredient
import com.example.donaka100.ui.components.*
import com.example.donaka100.ui.theme.*
import kotlin.math.abs

@Composable
fun AjustementSheet(
    ingredient: Ingredient,
    onDismiss: () -> Unit,
    onConfirm: (Double, String) -> Unit
) {
    var valeur by remember { mutableStateOf(ingredient.quantite.enQuantite()) }
    var motif by remember { mutableStateOf("") }
    var tentative by remember { mutableStateOf(false) }

    val nouvelle = valeur.enDecimal()
    val ecart = nouvelle?.minus(ingredient.quantite)

    DonakaFormSheet(
        titre = "Ajuster · ${ingredient.nom}",
        onDismiss = onDismiss,
        onValider = {
            tentative = true
            if (nouvelle != null && nouvelle >= 0) onConfirm(nouvelle, motif.trim())
        }
    ) {
        Text("Indique la quantité réellement présente après comptage.", fontSize = 13.sp, color = TexteGris)
        DonakaTextField(
            value = valeur,
            onValueChange = { valeur = it.filter { c -> c.isDigit() || c == ',' || c == '.' }.take(9) },
            label = "Quantité réelle", suffix = ingredient.unite.libelle,
            helper = "Actuellement : ${ingredient.quantite.avecUnite(ingredient.unite)}",
            keyboardType = KeyboardType.Decimal,
            error = if (tentative && nouvelle == null) "Saisis une quantité" else null
        )
        if (ecart != null && abs(ecart) > 0.0005) {
            Text(
                "Écart : " + (if (ecart > 0) "+" else "−") + abs(ecart).avecUnite(ingredient.unite),
                color = if (ecart > 0) Vert else Rouge, fontSize = 13.sp
            )
        }
        DonakaTextField(
            value = motif, onValueChange = { motif = it },
            label = "Motif (facultatif)", placeholder = "Ex : comptage, casse, perte",
            imeAction = ImeAction.Done
        )
    }
}