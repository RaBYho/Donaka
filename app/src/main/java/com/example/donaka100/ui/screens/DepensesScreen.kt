package com.example.donaka100.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.donaka100.data.CATEGORIE_ACHATS
import com.example.donaka100.data.Depense
import com.example.donaka100.data.cleNom
import com.example.donaka100.ui.DepenseUiState
import com.example.donaka100.ui.DepenseViewModel
import com.example.donaka100.ui.FiltreDepense
import com.example.donaka100.ui.LigneDepense
import com.example.donaka100.ui.components.*
import com.example.donaka100.ui.forms.DepenseFormSheet
import com.example.donaka100.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs

private sealed interface DialogueDepense {
    data object Creer : DialogueDepense
    data class Editer(val depense: Depense) : DialogueDepense
    data class Supprimer(val depense: Depense) : DialogueDepense
}

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

private fun couleurCategorie(nom: String): Color {
    if (nom == CATEGORIE_ACHATS) return Primary
    val palette = listOf(Ambre, Vert, Color(0xFF5B6B8C), Rouge, Color(0xFF8A7267))
    return palette[abs(nom.cleNom().hashCode()) % palette.size]
}

private fun iconeCategorie(l: LigneDepense): ImageVector {
    if (l.achat) return Icons.Default.ShoppingCart
    val c = l.categorie.lowercase()
    return when {
        c.contains("transport") || c.contains("carburant") -> Icons.Default.LocalShipping
        c.contains("jirama") || c.contains("énergie") || c.contains("energie") || c.contains("électr") -> Icons.Default.Bolt
        c.contains("emball") -> Icons.Default.Inventory2
        c.contains("salaire") || c.contains("main") -> Icons.Default.Groups
        else -> Icons.Default.Receipt
    }
}

@Composable
fun DepensesScreen(
    onVoirStock: () -> Unit = {},
    vm: DepenseViewModel = viewModel()
) {
    val etat by vm.etat.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var dialogue by remember { mutableStateOf<DialogueDepense?>(null) }

    LaunchedEffect(Unit) { vm.messages.collect { snackbar.showSnackbar(it) } }

    val mois = remember {
        val m = LocalDate.now()
        m.month.getDisplayName(TextStyle.FULL, Locale.FRENCH).replaceFirstChar { it.uppercase() } + " ${m.year}"
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp)) {
                Text("Gestion des Dépenses", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TexteFonce)
                Text("Charges opérationnelles · $mois", fontSize = 12.sp, color = TexteGris)
            }

            if (etat.erreur != null) {
                DonakaEmptyState(
                    titre = "Connexion impossible", message = etat.erreur!!,
                    icone = Icons.Default.CloudOff,
                    labelAction = "Réessayer", onAction = vm::charger
                )
            } else {
                Contenu(
                    etat = etat, modifier = Modifier.weight(1f),
                    onAjouter = { dialogue = DialogueDepense.Creer },
                    onRecherche = vm::onRecherche,
                    onFiltre = vm::onFiltre,
                    onModifier = { dialogue = DialogueDepense.Editer(it) },
                    onSupprimer = { dialogue = DialogueDepense.Supprimer(it) },
                    onVoirAchat = onVoirStock
                )
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }

    when (val d = dialogue) {
        null -> Unit

        DialogueDepense.Creer -> DepenseFormSheet(
            categories = etat.categories,
            onDismiss = { dialogue = null },
            onSave = { vm.creer(it); dialogue = null }
        )

        is DialogueDepense.Editer -> DepenseFormSheet(
            initial = d.depense, categories = etat.categories,
            onDismiss = { dialogue = null },
            onSave = { vm.modifier(d.depense.id, it); dialogue = null }
        )

        is DialogueDepense.Supprimer -> DonakaConfirmDialog(
            titre = "Supprimer cette dépense ?",
            message = "« ${d.depense.categorie} » de ${d.depense.montant.enMGA()} sera supprimée " +
                    "et ne comptera plus dans les totaux.",
            onConfirm = { vm.supprimer(d.depense); dialogue = null },
            onDismiss = { dialogue = null }
        )
    }
}

@Composable
private fun Contenu(
    etat: DepenseUiState,
    modifier: Modifier,
    onAjouter: () -> Unit,
    onRecherche: (String) -> Unit,
    onFiltre: (FiltreDepense) -> Unit,
    onModifier: (Depense) -> Unit,
    onSupprimer: (Depense) -> Unit,
    onVoirAchat: () -> Unit
) {
    val liste = etat.lignesAffichees
    val groupes = remember(liste) { liste.groupBy { it.dateHeure.toLocalDate() } }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        userScrollEnabled = !etat.isLoading
    ) {
        item {
            DonakaButton("Ajouter une dépense", onAjouter, icone = Icons.Default.Add, pleineLargeur = true)
        }
        item { Resume(etat) }

        when {
            etat.isLoading -> items(3) { DonakaCard(isLoading = true) {} }

            etat.lignes.isEmpty() -> item {
                Vide(
                    Icons.Default.Receipt, "Aucune dépense",
                    "Ajoute ta première dépense avec le bouton ci-dessus. Les achats de stock apparaîtront ici aussi."
                )
            }

            else -> {
                item {
                    DonakaSearchBar(
                        value = etat.recherche, onValueChange = onRecherche,
                        placeholder = "Rechercher une dépense, une catégorie…"
                    )
                }
                item {
                    val options = buildList<FiltreDepense> {
                        add(FiltreDepense.Tout)
                        add(FiltreDepense.Aujourdhui)
                        add(FiltreDepense.CeMois)
                        etat.categoriesLignes.forEach { add(FiltreDepense.Categorie(it)) }
                    }
                    DonakaFilterChips(
                        options = options, selected = etat.filtreEffectif, onSelect = onFiltre,
                        label = {
                            when (it) {
                                FiltreDepense.Tout -> "Tout"
                                FiltreDepense.Aujourdhui -> "Aujourd'hui (${etat.nbAujourdhui})"
                                FiltreDepense.CeMois -> "Ce mois"
                                is FiltreDepense.Categorie -> it.nom
                            }
                        }
                    )
                }
                item { Text("Historique des dépenses", fontWeight = FontWeight.Bold, color = TexteFonce) }

                if (liste.isEmpty()) {
                    item { Vide(Icons.Default.SearchOff, "Aucune dépense trouvée", "Essaie une autre recherche ou un autre filtre.") }
                } else {
                    groupes.forEach { (date, lignes) ->
                        item(key = "titre-$date") { TitreJour(date, lignes) }
                        items(lignes, key = { it.id }) { l ->
                            CarteDepense(
                                l,
                                onModifier = { l.depense?.let(onModifier) },
                                onSupprimer = { l.depense?.let(onSupprimer) },
                                onVoirAchat = onVoirAchat
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Resume(etat: DepenseUiState) {
    DonakaCard(isLoading = etat.isLoading) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AccountBalanceWallet, null, tint = Primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("TOTAL DÉPENSES DU MOIS", fontSize = 11.sp, color = TexteGris, fontWeight = FontWeight.Bold)
        }
        Text(etat.totalMois.enMGA(), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TexteFonce)

        val rep = etat.repartition
        if (rep.isNotEmpty()) {
            val n = rep.size
            Text(
                "$n catégorie${if (n > 1) "s" else ""} • trésorerie décaissée",
                fontSize = 12.sp, color = TexteGris
            )
            Row(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50))) {
                rep.forEach { (cat, m) ->
                    Box(Modifier.weight(m.toFloat()).fillMaxHeight().background(couleurCategorie(cat)))
                }
            }
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rep.forEach { (cat, m) ->
                    Row(
                        Modifier.clip(RoundedCornerShape(50)).background(SurfaceMoyenne)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(couleurCategorie(cat)))
                        Spacer(Modifier.width(6.dp))
                        Text("$cat : ${m.enMGA()}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                    }
                }
            }
        } else if (!etat.isLoading) {
            Text("Aucune dépense ce mois-ci", fontSize = 12.sp, color = TexteGris)
        }
    }
}

@Composable
private fun TitreJour(date: LocalDate, lignes: List<LigneDepense>) {
    Row(
        Modifier.fillMaxWidth().padding(start = 4.dp, top = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(libelleJour(date), fontWeight = FontWeight.SemiBold, color = TexteFonce)
        Text("-${lignes.sumOf { it.montant }.enMGA()}", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Rouge)
    }
}

@Composable
private fun CarteDepense(
    l: LigneDepense,
    onModifier: () -> Unit,
    onSupprimer: () -> Unit,
    onVoirAchat: () -> Unit
) {
    val couleur = couleurCategorie(l.categorie)

    DonakaCard {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Box(
                Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(couleur.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) { Icon(iconeCategorie(l), null, tint = couleur, modifier = Modifier.size(22.dp)) }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    l.titre, fontWeight = FontWeight.Bold, color = TexteFonce,
                    maxLines = 2, overflow = TextOverflow.Ellipsis
                )
                val infos = listOfNotNull(
                    l.dateHeure.format(formatHeure),
                    l.mode?.libelle,
                    l.detail.takeIf { it.isNotBlank() }
                )
                Text(infos.joinToString(" • "), fontSize = 12.sp, color = TexteGris, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (l.achat) DonakaBadge("Achat de stock", type = TypeBadge.NEUTRE)
            }

            Spacer(Modifier.width(8.dp))

            Text(
                "-${l.montant.enMGA()}", fontWeight = FontWeight.Bold, color = Rouge,
                textAlign = TextAlign.End
            )

            DonakaOverflowMenu(
                if (l.achat) listOf(ActionMenu("Voir dans Achats",
                    Icons.AutoMirrored.Filled.ReceiptLong
                ) { onVoirAchat() })
                else listOf(
                    ActionMenu("Modifier", Icons.Default.Edit) { onModifier() },
                    ActionMenu("Supprimer", Icons.Default.Delete, danger = true) { onSupprimer() }
                )
            )
        }
    }
}

@Composable
private fun Vide(icone: ImageVector, titre: String, message: String) {
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