package com.example.donaka100.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Payments
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/** Affiche 1500000 comme "1 500 000" sans toucher à la valeur réelle */
private object MilliersTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val brut = text.text
        val affiche = brut.avecEspacesMilliers()
        val n = brut.length

        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val restant = n - offset
                val sepTotal = if (n == 0) 0 else (n - 1) / 3
                val sepApres = if (restant == 0) 0 else (restant - 1) / 3
                return offset + (sepTotal - sepApres)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val espaces = affiche.take(offset).count { it == ' ' }
                return offset - espaces
            }
        }
        return TransformedText(AnnotatedString(affiche), mapping)
    }
}

/**
 * [value] contient uniquement des chiffres ("1500000").
 * Récupère le nombre avec value.toLongOrNull().
 */
@Composable
fun DonakaAmountField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Montant",
    error: String? = null,
    helper: String? = null,
    imeAction: ImeAction = ImeAction.Next,
    maxChiffres: Int = 9
) {
    DonakaTextField(
        value = value,
        onValueChange = { saisie -> onValueChange(saisie.filter { it.isDigit() }.take(maxChiffres)) },
        label = label,
        modifier = modifier,
        error = error,
        helper = helper,
        leadingIcon = Icons.Default.Payments,
        suffix = "MGA",
        keyboardType = KeyboardType.Number,
        imeAction = imeAction,
        visualTransformation = MilliersTransformation
    )
}