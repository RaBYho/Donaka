package com.example.donaka100.ui.forms

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import com.example.donaka100.data.*
import com.example.donaka100.ui.components.*
import com.example.donaka100.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// ---------- Briques communes ----------

@Composable
private fun ChampsQuantites(
    produits: List<Produit>,
    quantites: SnapshotStateMap<String, String>,
    prixFiges: Map<String, Long>
) {
    produits.forEach { p ->
        val prix = prixFiges[p.id] ?: p.prixGros
        val q = quantites[p.id]?.toIntOrNull() ?: 0
        DonakaTextField(
            value = quantites[p.id].orEmpty(),
            onValueChange = { quantites[p.id] = it.filter(Char::isDigit).take(4) },
            label = "${p.nom} · ${prix.enMGA()}",
            suffix = "pcs",
            helper = if (q > 0) "= ${(q * prix).enMGA()}" else null,
            keyboardType = KeyboardType.Number
        )
    }
}

private fun totalDe(
    produits: List<Produit>,
    quantites: Map<String, String>,
    prixFiges: Map<String, Long>
): Long = produits.sumOf { p ->
    (quantites[p.id]?.toIntOrNull() ?: 0) * (prixFiges[p.id] ?: p.prixGros)
}

private fun lignesDe(quantites: Map<String, String>): List<LigneDemande> =
    quantites.mapNotNull { (id, q) -> q.toIntOrNull()?.takeIf { it > 0 }?.let { LigneDemande(id, it) } }

@Composable
private fun CarteTotal(total: Long, nbArticles: Int, libelle: String = "Total (prix de gros)") {
    DonakaCard(containerColor = PrimaireClair) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(libelle, color = Primary, fontWeight = FontWeight.Medium)
                Text("$nbArticles article${if (nbArticles > 1) "s" else ""}", fontSize = 12.sp, color = Primary)
            }
            Text(total.enMGA(), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Primary)
        }
    }
}

private val REGEX_HEURE = Regex("^([01]\\d|2[0-3]):[0-5]\\d$")

// ---------- Commande J+1 (création et modification) ----------

@Composable
fun CommandeFormSheet(
    produits: List<Produit>,
    clients: List<Client>,
    date: LocalDate,
    onDismiss: () -> Unit,
    onSave: (NouvelleCommande) -> Unit,
    initial: Commande? = null,
    clientIdInitial: String? = null
) {
    var clientId by remember { mutableStateOf(initial?.clientId ?: clientIdInitial) }
    var heure by remember { mutableStateOf(initial?.heure.orEmpty()) }
    val quantites = remember {
        mutableStateMapOf<String, String>().apply {
            initial?.lignes?.forEach { put(it.produitId, it.quantite.toString()) }
        }
    }
    val prixFiges = remember { initial?.lignes?.associate { it.produitId to it.prixUnitaire }.orEmpty() }
    var tentative by remember { mutableStateOf(false) }

    val total = totalDe(produits, quantites, prixFiges)
    val nbArticles = quantites.values.sumOf { it.toIntOrNull() ?: 0 }
    val heureValide = heure.isBlank() || REGEX_HEURE.matches(heure)

    val erreurClient = if (tentative && clientId == null) "Choisis un client" else null
    val erreurHeure = if (tentative && !heureValide) "Format attendu : 08:00" else null
    val erreurQuantite = tentative && total <= 0

    val jour = date.format(DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.FRENCH))

    DonakaFormSheet(
        titre = if (initial == null) "Nouvelle commande" else "Modifier la commande",
        labelValider = "Enregistrer la commande",
        onDismiss = onDismiss,
        onValider = {
            tentative = true
            val id = clientId
            if (id != null && heureValide && total > 0) {
                onSave(NouvelleCommande(id, date, heure, lignesDe(quantites)))
            }
        }
    ) {
        Text("Livraison prévue : $jour", fontSize = 13.sp, color = TexteGris)

        if (initial != null) {
            DonakaTextField(value = initial.clientNom, onValueChange = {}, label = "Client", enabled = false)
        } else if (clients.isEmpty()) {
            Text(
                "Aucun client. Crée-en un d'abord dans l'onglet « Clients & Créances ».",
                color = Rouge, fontSize = 13.sp
            )
        } else {
            DonakaDropdownField(
                label = "Client",
                options = clients.map { it.nom },
                selected = clients.firstOrNull { it.id == clientId }?.nom.orEmpty(),
                onSelect = { nom -> clientId = clients.firstOrNull { it.nom == nom }?.id },
                error = erreurClient
            )
        }

        DonakaTextField(
            value = heure,
            onValueChange = { heure = it.filter { c -> c.isDigit() || c == ':' }.take(5) },
            label = "Heure de livraison (facultatif)",
            helper = "Ex : 08:00",
            keyboardType = KeyboardType.Number,
            error = erreurHeure
        )

        if (produits.isEmpty()) {
            Text("Aucun produit. Ajoute-en dans la page Fourneaux.", color = Rouge, fontSize = 13.sp)
        } else {
            ChampsQuantites(produits, quantites, prixFiges)
        }

        if (erreurQuantite) Text("Ajoute au moins un produit", color = Rouge, fontSize = 12.sp)
        CarteTotal(total, nbArticles)
    }
}

// ---------- Client non prévu : toujours payé comptant ----------

@Composable
fun VenteNonPrevueSheet(
    produits: List<Produit>,
    onDismiss: () -> Unit,
    onSave: (List<LigneDemande>, ModeReglement) -> Unit
) {
    val quantites = remember { mutableStateMapOf<String, String>() }
    var mode by remember { mutableStateOf(ModeReglement.ESPECES) }
    var tentative by remember { mutableStateOf(false) }

    val total = totalDe(produits, quantites, emptyMap())
    val nbArticles = quantites.values.sumOf { it.toIntOrNull() ?: 0 }

    DonakaFormSheet(
        titre = "Client non prévu",
        labelValider = "Encaisser et livrer",
        onDismiss = onDismiss,
        onValider = {
            tentative = true
            if (total > 0) onSave(lignesDe(quantites), mode)
        }
    ) {
        Text(
            "Payé comptant. Pour vendre à crédit, ajoute d'abord la personne comme client.",
            fontSize = 13.sp, color = TexteGris
        )
        ChampsQuantites(produits, quantites, emptyMap())
        if (tentative && total <= 0) Text("Ajoute au moins un produit", color = Rouge, fontSize = 12.sp)

        DonakaDropdownField(
            label = "Mode de paiement",
            options = ModeReglement.entries.map { it.libelle },
            selected = mode.libelle,
            onSelect = { choix -> mode = ModeReglement.entries.first { it.libelle == choix } }
        )
        CarteTotal(total, nbArticles, libelle = "À encaisser")
    }
}