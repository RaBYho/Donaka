package com.example.donaka100.ui.screens.stock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.donaka100.data.MouvementStock
import com.example.donaka100.ui.FiltreAchat
import com.example.donaka100.ui.StockUiState
import com.example.donaka100.ui.components.*
import com.example.donaka100.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val formatHeure = DateTimeFormatter.ofPattern("HH:mm")
private val formatJour = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH)

private fun libelleJour(d: LocalDate): String {
    val auj = LocalDate.now()
    return when (d) {
        auj -> "Aujourd'hui"
        auj.minusDays(1) -> "Hier"
        else -> d.format(formatJour).replaceFirstChar { it.uppercase() }
    }
}

@Composable
fun AchatsTab(
    etat: StockUiState,
    modifier: Modifier,
    onAchat: () -> Unit,
    onRecherche: (String) -> Unit,
    onFiltre: (FiltreAchat) -> Unit,
    onModifier: (MouvementStock) -> Unit,
    onAnnuler: (MouvementStock) -> Unit
) {
    val liste = etat.achatsAffiches
    val groupes = remember(liste) { liste.groupBy { it.dateHeure.toLocalDate() } }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        userScrollEnabled = !etat.isLoading
    ) {
        item { Resume(etat) }
        item {
            DonakaButton(
                "Effectuer un Achat", onAchat,
                icone = Icons.Default.AddShoppingCart, pleineLargeur = true
            )
        }

        when {
            etat.isLoading -> items(3) { DonakaCard(isLoading = true) {} }

            etat.achats.isEmpty() -> item {
                Vide(Icons.AutoMirrored.Filled.ReceiptLong, "Aucun achat", "Les achats enregistrés apparaîtront ici.")
            }

            else -> {
                item {
                    DonakaSearchBar(
                        value = etat.rechercheAchat, onValueChange = onRecherche,
                        placeholder = "Rechercher un ingrédient, un fournisseur…"
                    )
                }
                item {
                    val options = buildList<FiltreAchat> {
                        add(FiltreAchat.Tous)
                        add(FiltreAchat.Aujourdhui)
                        add(FiltreAchat.CeMois)
                        etat.rayonsAchats.forEach { add(FiltreAchat.Rayon(it)) }
                    }
                    DonakaFilterChips(
                        options = options,
                        selected = etat.filtreAchatEffectif,
                        onSelect = onFiltre,
                        label = {
                            when (it) {
                                FiltreAchat.Tous -> "Tous les flux"
                                FiltreAchat.Aujourdhui -> "Aujourd'hui (${etat.nbAchatsAujourdhui})"
                                FiltreAchat.CeMois -> "Ce mois"
                                is FiltreAchat.Rayon -> it.nom
                            }
                        }
                    )
                }
                item {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Historique des achats", fontWeight = FontWeight.Bold, color = TexteFonce)
                        DonakaBadge("Ce mois : ${etat.depenseMois.enMGA()}", type = TypeBadge.NEUTRE)
                    }
                }

                if (liste.isEmpty()) {
                    item {
                        Vide(Icons.Default.SearchOff, "Aucun achat trouvé", "Essaie une autre recherche ou un autre filtre.")
                    }
                } else {
                    groupes.forEach { (date, achats) ->
                        item(key = "titre-$date") { TitreJour(date, achats) }
                        items(achats, key = { it.id }) { m ->
                            CarteAchat(m, onModifier = { onModifier(m) }, onAnnuler = { onAnnuler(m) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Resume(etat: StockUiState) {
    DonakaCard(isLoading = etat.isLoading) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(Modifier.weight(1f)) {
                Text("Dépensé aujourd'hui", fontSize = 12.sp, color = TexteGris)
                Text(etat.depenseAujourdhui.enMGA(), fontWeight = FontWeight.Bold, color = Rouge)
            }
            Column(Modifier.weight(1f)) {
                Text("Achats du jour", fontSize = 12.sp, color = TexteGris)
                val n = etat.nbAchatsAujourdhui
                Text("$n achat${if (n > 1) "s" else ""}", fontWeight = FontWeight.Bold, color = TexteFonce)
            }
        }
    }
}

@Composable
private fun TitreJour(date: LocalDate, achats: List<MouvementStock>) {
    val total = achats.filter { !it.annule }.sumOf { it.montant ?: 0L }
    Row(
        Modifier.fillMaxWidth().padding(start = 4.dp, top = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(libelleJour(date), fontWeight = FontWeight.SemiBold, color = TexteFonce)
        if (total > 0) Text("-${total.enMGA()}", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Rouge)
    }
}
@Composable
private fun CarteAchat(m: MouvementStock, onModifier: () -> Unit, onAnnuler: () -> Unit) {
    val annule = m.annule
    val montant = m.montant

    DonakaCard(containerColor = if (annule) SurfaceMoyenne.copy(alpha = 0.6f) else SurfaceBlanche) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Box(
                Modifier.size(40.dp).clip(RoundedCornerShape(10.dp))
                    .background(if (annule) SurfaceMoyenne else PrimaireClair),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Inventory2, null,
                    tint = if (annule) TexteGris else Primary, modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "${m.ingredientNom} · ${m.quantite.avecUnite(m.unite)}",
                    fontWeight = FontWeight.Bold, color = if (annule) TexteGris else TexteFonce,
                    maxLines = 2, overflow = TextOverflow.Ellipsis
                )
                if (m.fournisseur.isNotBlank()) {
                    Text(m.fournisseur, fontSize = 12.sp, color = TexteGris, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                if (montant != null) {
                    Text(
                        "-${montant.enMGA()}", fontWeight = FontWeight.Bold,
                        color = if (annule) TexteGris else Rouge, textAlign = TextAlign.End
                    )
                } else {
                    Text("Montant non renseigné", fontSize = 11.sp, color = TexteGris, textAlign = TextAlign.End)
                }
                Text(
                    m.dateHeure.format(formatHeure) + if (montant != null && m.mode != null) " • ${m.mode.libelle}" else "",
                    fontSize = 11.sp, color = TexteGris
                )
            }
        }

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Row(
                Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                if (annule) {
                    DonakaBadge("Annulé", type = TypeBadge.NEUTRE)
                    DonakaBadge("Stock retiré", type = TypeBadge.NEUTRE)
                } else {
                    DonakaBadge("Stock (+${m.quantite.avecUnite(m.unite)})", type = TypeBadge.SUCCES)
                    if (montant != null) DonakaBadge("Trésorerie déduite", type = TypeBadge.NEUTRE)
                    else DonakaBadge("Montant à compléter", type = TypeBadge.ALERTE)
                }
            }
            if (!annule) {
                DonakaOverflowMenu(
                    listOf(
                        ActionMenu("Modifier", Icons.Default.Edit) { onModifier() },
                        ActionMenu("Annuler l'achat", Icons.Default.Cancel, danger = true) { onAnnuler() }
                    )
                )
            }
        }
    }
}

@Composable
private fun Vide(icone: androidx.compose.ui.graphics.vector.ImageVector, titre: String, message: String) {
    Column(
        Modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icone, null, tint = TexteGris.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(8.dp))
        Text(titre, fontWeight = FontWeight.SemiBold, color = TexteFonce)
        Text(message, color = TexteGris, textAlign = TextAlign.Center, fontSize = 13.sp)
    }
}