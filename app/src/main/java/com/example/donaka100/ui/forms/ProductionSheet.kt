package com.example.donaka100.ui.forms

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.donaka100.data.Besoin
import com.example.donaka100.data.LigneDemande
import com.example.donaka100.ui.LignePlan
import com.example.donaka100.ui.components.*
import com.example.donaka100.ui.theme.*

/** Le chef ajuste les quantités réellement produites avant de valider */
@Composable
fun ProductionSheet(
    plan: List<LignePlan>,
    besoinsPour: (Map<String, Int>) -> List<Besoin>,
    onDismiss: () -> Unit,
    onConfirm: (List<LigneDemande>) -> Unit
) {
    val saisie = remember {
        mutableStateMapOf<String, String>().apply { plan.forEach { put(it.produitId, it.quantite.toString()) } }
    }
    val quantites = plan.associate { it.produitId to (saisie[it.produitId]?.toIntOrNull() ?: 0) }
    val manquants = besoinsPour(quantites).filterNot { it.suffisant }
    val total = quantites.values.sum()
    var tentative by remember { mutableStateOf(false) }

    DonakaFormSheet(
        titre = "Lancer la production",
        labelValider = if (manquants.isEmpty()) "Valider la production" else "Lancer quand même",
        onDismiss = onDismiss,
        onValider = {
            tentative = true
            if (total > 0) onConfirm(quantites.map { LigneDemande(it.key, it.value) })
        }
    ) {
        Text(
            "Ajuste si tu fabriques plus ou moins que prévu. Les ingrédients sont déduits sur ces quantités.",
            fontSize = 13.sp, color = TexteGris
        )
        plan.forEach { l ->
            DonakaTextField(
                value = saisie[l.produitId].orEmpty(),
                onValueChange = { saisie[l.produitId] = it.filter(Char::isDigit).take(4) },
                label = l.nom,
                suffix = "pcs",
                helper = "Prévu : ${l.quantite}",
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
            )
        }
        if (tentative && total <= 0) Text("Indique au moins une quantité", color = Rouge, fontSize = 12.sp)

        if (manquants.isEmpty()) {
            DonakaBadge("Stock suffisant", type = TypeBadge.SUCCES)
        } else {
            Text("Stock insuffisant", color = Rouge, fontWeight = FontWeight.SemiBold)
            manquants.forEach {
                Text(
                    "• ${it.ingredient.nom} : il manque ${it.manque.avecUnite(it.ingredient.unite)}",
                    color = Rouge, fontSize = 13.sp
                )
            }
            Text("Le stock sera ramené à 0 pour ces ingrédients.", fontSize = 12.sp, color = TexteGris)
        }
    }
}