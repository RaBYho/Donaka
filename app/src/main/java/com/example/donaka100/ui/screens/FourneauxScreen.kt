package com.example.donaka100.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.donaka100.data.Fournee
import com.example.donaka100.data.Produit
import com.example.donaka100.ui.FourneauxViewModel
import com.example.donaka100.ui.OngletFourneaux
import com.example.donaka100.ui.components.*
import com.example.donaka100.ui.forms.ProduitFormSheet
import com.example.donaka100.ui.forms.ProductionSheet
import com.example.donaka100.ui.screens.fourneaux.*
import com.example.donaka100.ui.theme.*

private sealed interface DialogueFourneaux {
    data object CreerProduit : DialogueFourneaux
    data class EditerProduit(val produit: Produit) : DialogueFourneaux
    data class SupprimerProduit(val produit: Produit) : DialogueFourneaux
    data object Lancer : DialogueFourneaux
    data class AnnulerFournee(val fournee: Fournee) : DialogueFourneaux
}

/**
 * Écran Fourneaux avec transitions douces entre onglets et calculs légers.
 */
@Composable
fun FourneauxScreen(vm: FourneauxViewModel = viewModel()) {
    val etat by vm.etat.collectAsStateWithLifecycle()
    val isRefreshing by vm.isRefreshing.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var dialogue by remember { mutableStateOf<DialogueFourneaux?>(null) }

    LaunchedEffect(Unit) { vm.messages.collect { snackbar.showSnackbar(it) } }
    LaunchedEffect(Unit) { vm.onOnglet(OngletFourneaux.A_PREPARER) }

    DonakaPullRefresh(
        isRefreshing = isRefreshing,
        onRefresh = vm::actualiser
    ) {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
            Column(
                Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Gestion des Fourneaux", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TexteFonce)
                        Text(
                            when (etat.onglet) {
                                OngletFourneaux.A_PREPARER -> "Plan de fabrication pour demain"
                                OngletFourneaux.RECETTES -> "Produits, prix et recettes par lot"
                                OngletFourneaux.HISTORIQUE -> "Fournées validées"
                            },
                            fontSize = 12.sp, color = TexteGris
                        )
                    }
                    DonakaBadge("Demain", type = TypeBadge.NEUTRE)
                }
                DonakaSegmentedToggle(
                    options = OngletFourneaux.entries,
                    selected = etat.onglet,
                    onSelect = vm::onOnglet,
                    label = { it.libelle }
                )
            }

            AnimatedContent(
                targetState = etat.onglet,
                transitionSpec = {
                    fadeIn(animationSpec = tween(280, easing = FastOutSlowInEasing)) togetherWith
                            fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing))
                },
                modifier = Modifier.weight(1f),
                label = "fourneauxTabTransition"
            ) { onglet ->
                when {
                    etat.erreur != null -> DonakaEmptyState(
                        titre = "Connexion impossible", message = etat.erreur!!,
                        icone = Icons.Default.CloudOff,
                        labelAction = "Réessayer", onAction = vm::charger
                    )

                    onglet == OngletFourneaux.A_PREPARER -> PreparerTab(
                        etat = etat, modifier = Modifier.fillMaxSize(),
                        onLancer = { dialogue = DialogueFourneaux.Lancer },
                        onAnnuler = { dialogue = DialogueFourneaux.AnnulerFournee(it) },
                        onVoirRecettes = { vm.onOnglet(OngletFourneaux.RECETTES) }
                    )

                    onglet == OngletFourneaux.RECETTES -> RecettesTab(
                        etat = etat, modifier = Modifier.fillMaxSize(),
                        onNouveau = { dialogue = DialogueFourneaux.CreerProduit },
                        onModifier = { dialogue = DialogueFourneaux.EditerProduit(it) },
                        onSupprimer = { dialogue = DialogueFourneaux.SupprimerProduit(it) },
                        onRecherche = vm::onRechercheProduit,
                        onCategorie = vm::onCategorie,
                        onTri = vm::onTri
                    )

                    else -> FourneesTab(
                        etat = etat, modifier = Modifier.fillMaxSize(),
                        onAnnuler = { dialogue = DialogueFourneaux.AnnulerFournee(it) },
                        onRecherche = vm::onRechercheFournee,
                        onPeriode = vm::onPeriode
                    )
                }
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }
}

    when (val d = dialogue) {
        null -> Unit

        DialogueFourneaux.CreerProduit -> ProduitFormSheet(
            ingredients = etat.ingredients,
            categories = etat.categories,
            onDismiss = { dialogue = null },
            onSave = { vm.creerProduit(it); dialogue = null }
        )

        is DialogueFourneaux.EditerProduit -> ProduitFormSheet(
            ingredients = etat.ingredients,
            categories = etat.categories,
            initial = d.produit,
            onDismiss = { dialogue = null },
            onSave = { vm.modifierProduit(d.produit.id, it); dialogue = null }
        )

        is DialogueFourneaux.SupprimerProduit -> DonakaConfirmDialog(
            titre = "Supprimer ce produit ?",
            message = "« ${d.produit.nom} » sera retiré. S'il a déjà été commandé, il est archivé " +
                    "et les commandes existantes restent intactes.",
            onConfirm = { vm.supprimerProduit(d.produit); dialogue = null },
            onDismiss = { dialogue = null }
        )

        DialogueFourneaux.Lancer -> ProductionSheet(
            plan = etat.plan,
            besoinsPour = { q -> etat.besoinsPour(q) },
            onDismiss = { dialogue = null },
            onConfirm = { vm.lancer(it); dialogue = null }
        )

        is DialogueFourneaux.AnnulerFournee -> DonakaConfirmDialog(
            titre = "Annuler cette fournée ?",
            message = "Les ingrédients déduits seront remis en stock.",
            labelConfirmer = "Oui, annuler",
            onConfirm = { vm.annulerFournee(d.fournee); dialogue = null },
            onDismiss = { dialogue = null }
        )
    }
}