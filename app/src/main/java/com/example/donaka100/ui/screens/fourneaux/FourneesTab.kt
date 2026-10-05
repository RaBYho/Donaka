package com.example.donaka100.ui.screens.fourneaux

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.donaka100.data.Fournee
import com.example.donaka100.data.UniteStock
import com.example.donaka100.ui.FourneauxUiState
import com.example.donaka100.ui.PeriodeFournee
import com.example.donaka100.ui.SyntheseMois
import com.example.donaka100.ui.components.*
import com.example.donaka100.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val formatHeure = DateTimeFormatter.ofPattern("HH:mm")
private val formatCourt = DateTimeFormatter.ofPattern("dd/MM")
private val formatJour = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH)

private fun libelleJour(d: LocalDate): String {
    val auj = LocalDate.now()
    return when (d) {
        auj -> "Aujourd'hui"
        auj.plusDays(1) -> "Demain"
        auj.minusDays(1) -> "Hier"
        else -> d.format(formatJour).replaceFirstChar { it.uppercase() }
    }
}

@Composable
fun FourneesTab(
    etat: FourneauxUiState,
    modifier: Modifier,
    onAnnuler: (Fournee) -> Unit,
    onRecherche: (String) -> Unit,
    onPeriode: (PeriodeFournee) -> Unit
) {
    val liste = etat.historiqueAffiche
    val groupes = remember(liste) { liste.groupBy { it.date } }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        userScrollEnabled = !etat.isLoading
    ) {
        when {
            etat.isLoading -> items(3) { DonakaCard(isLoading = true) {} }

            etat.fournees.isEmpty() -> item { Vide(filtre = false) }

            else -> {
                item {
                    DonakaSearchBar(
                        value = etat.rechercheFournee, onValueChange = onRecherche,
                        placeholder = "Rechercher…"
                    )
                }
                item {
                    DonakaFilterChips(
                        options = PeriodeFournee.entries,
                        selected = etat.periode,
                        onSelect = onPeriode,
                        label = { it.libelle }
                    )
                }
                item { SyntheseCard(etat.syntheseMois) }

                if (liste.isEmpty()) {
                    item { Vide(filtre = true) }
                } else {
                    groupes.forEach { (date, fournees) ->
                        item(key = "titre-$date") { TitreJour(date, fournees.size) }
                        items(fournees, key = { it.id }) { f -> CarteFournee(f) { onAnnuler(f) } }
                    }
                }
            }
        }
    }
}

@Composable
private fun SyntheseCard(s: SyntheseMois) {
    DonakaCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Insights, null, tint = Primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Synthèse de ${s.mois}", fontWeight = FontWeight.Bold, color = TexteFonce)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TuileStat("Fournées", s.nbFournees.toString(), Modifier.weight(1f))
            TuileStat("Unités", s.unites.toString().avecEspacesMilliers(), Modifier.weight(1f))
            TuileStat("Moy. / jour", s.moyenneParJour.toString().avecEspacesMilliers(), Modifier.weight(1f))
        }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Farine consommée ce mois", fontSize = 12.sp, color = TexteGris)
            Text(s.farineKg.avecUnite(UniteStock.KG), fontWeight = FontWeight.Bold, color = Primary)
        }
    }
}

@Composable
private fun TuileStat(titre: String, valeur: String, modifier: Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(10.dp)).background(SurfaceMoyenne.copy(alpha = 0.5f)).padding(10.dp)
    ) {
        Text(titre.uppercase(), fontSize = 10.sp, color = TexteGris, fontWeight = FontWeight.SemiBold)
        Text(valeur, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TexteFonce)
    }
}

@Composable
private fun TitreJour(date: LocalDate, nb: Int) {
    val auj = date == LocalDate.now()
    Row(
        Modifier.fillMaxWidth().padding(start = 4.dp, top = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.CalendarToday, null,
            tint = if (auj) Primary else TexteGris, modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(libelleJour(date), Modifier.weight(1f), fontWeight = FontWeight.SemiBold, color = TexteFonce)
        DonakaBadge("$nb fournée${if (nb > 1) "s" else ""}", type = TypeBadge.NEUTRE)
    }
}

@Composable
private fun CarteFournee(f: Fournee, onAnnuler: () -> Unit) {
    var ouvert by remember { mutableStateOf(false) }
    val annulee = f.annulee

    val sousTitre = remember(f.heure, f.date, f.annuleeA) {
        val validee =
            if (f.heure.toLocalDate() == f.date) "Validée à ${f.heure.format(formatHeure)}"
            else "Validée le ${f.heure.format(formatCourt)} à ${f.heure.format(formatHeure)}"
        f.annuleeA?.let { "$validee · annulée à ${it.format(formatHeure)}" } ?: validee
    }

    DonakaCard(containerColor = if (annulee) SurfaceMoyenne.copy(alpha = 0.6f) else SurfaceBlanche) {
        // En-tête
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(if (annulee) SurfaceMoyenne else PrimaireClair),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.LocalFireDepartment, null,
                    tint = if (annulee) TexteGris else Primary, modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Production", fontWeight = FontWeight.Bold, color = if (annulee) TexteGris else TexteFonce)
                Text(sousTitre, fontSize = 12.sp, color = TexteGris)
            }
            DonakaBadge(
                if (annulee) "Annulée" else "Validée",
                type = if (annulee) TypeBadge.NEUTRE else TypeBadge.SUCCES
            )
            if (!annulee) {
                DonakaOverflowMenu(
                    listOf(ActionMenu("Annuler cette fournée", Icons.Default.Cancel, danger = true) { onAnnuler() })
                )
            }
        }

        // Produits fabriqués
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                .background(SurfaceMoyenne.copy(alpha = 0.5f)).padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            f.lignes.forEach { l ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        l.nom, Modifier.weight(1f), fontSize = 13.sp, color = TexteFonce,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "${l.quantite}×" +
                                if (l.quantite != l.quantitePlanifiee) " (prévu ${l.quantitePlanifiee})" else "",
                        fontSize = 13.sp, fontWeight = FontWeight.Bold,
                        color = if (annulee) TexteGris else Primary
                    )
                }
            }
            HorizontalDivider(color = SurfaceMoyenne)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text("${f.totalPieces} pièces", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Ingrédients (repliés par défaut)
        if (f.consommations.isNotEmpty()) {
            TextButton(onClick = { ouvert = !ouvert }) {
                Text(
                    if (ouvert) "Masquer les ingrédients" else "Ingrédients consommés",
                    color = Primary, fontWeight = FontWeight.SemiBold
                )
                Icon(
                    if (ouvert) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    null, tint = Primary
                )
            }
            if (ouvert) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    f.consommations.forEach {
                        val manque = it.manque > 0.0005
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(it.nom, fontSize = 13.sp, color = TexteFonce)
                            Text(
                                it.requis.avecUnite(it.unite), fontSize = 13.sp, fontWeight = FontWeight.Medium,
                                color = if (manque) Rouge else TexteFonce
                            )
                        }
                        if (manque) {
                            Text(
                                "Stock insuffisant : ${it.manque.avecUnite(it.unite)} en moins",
                                fontSize = 11.sp, color = Rouge
                            )
                        }
                    }
                    if (annulee) {
                        Text("Ces ingrédients ont été remis en stock.", fontSize = 12.sp, color = TexteGris)
                    }
                }
            }
        }
    }
}

@Composable
private fun Vide(filtre: Boolean) {
    Column(
        Modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            if (filtre) Icons.Default.SearchOff else Icons.Default.History,
            null, tint = TexteGris.copy(alpha = 0.5f), modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            if (filtre) "Aucune fournée trouvée" else "Aucune fournée",
            fontWeight = FontWeight.SemiBold, color = TexteFonce
        )
        Text(
            if (filtre) "Essaie un autre mot-clé ou une autre période."
            else "Les productions validées apparaîtront ici.",
            color = TexteGris, textAlign = TextAlign.Center, fontSize = 13.sp
        )
    }
}