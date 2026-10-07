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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.donaka100.data.Ingredient
import com.example.donaka100.data.MouvementStock
import com.example.donaka100.data.StatutStock
import com.example.donaka100.data.cleNom
import com.example.donaka100.ui.Fournisseur
import com.example.donaka100.ui.OngletStock
import com.example.donaka100.ui.StockViewModel
import com.example.donaka100.ui.components.*
import com.example.donaka100.ui.forms.AchatFormSheet
import com.example.donaka100.ui.forms.AjustementSheet
import com.example.donaka100.ui.forms.FournisseurFormSheet
import com.example.donaka100.ui.forms.IngredientFormSheet
import com.example.donaka100.ui.forms.ModifierAchatSheet
import com.example.donaka100.ui.screens.stock.AchatsTab
import com.example.donaka100.ui.screens.stock.FournisseursTab
import com.example.donaka100.ui.screens.stock.InventaireTab
import com.example.donaka100.ui.theme.*
import com.example.donaka100.ui.util.ouvrirAppel

private sealed interface DialogueStock {
    data object Nouveau : DialogueStock
    data class Edition(val ingredient: Ingredient) : DialogueStock
    data class Ajuster(val ingredient: Ingredient) : DialogueStock
    data class Acheter(val nom: String = "", val fournisseur: String = "") : DialogueStock
    data class Supprimer(val ingredient: Ingredient) : DialogueStock
    data class Bloque(val ingredient: Ingredient, val produits: List<String>) : DialogueStock
    data class ModifierAchat(val achat: MouvementStock) : DialogueStock
    data class AnnulerAchat(val achat: MouvementStock) : DialogueStock
    data class AnnulationBloquee(val raison: String) : DialogueStock
    data object CreerFournisseur : DialogueStock
    data class EditerFournisseur(val fournisseur: Fournisseur) : DialogueStock
    data class SupprimerFiche(val fournisseur: Fournisseur) : DialogueStock
}

/**
 * Écran Stock avec navigation réactive, transitions douces et calculs isolés.
 */
@Composable
fun StockScreen(vm: StockViewModel = viewModel()) {
    val etat by vm.etat.collectAsStateWithLifecycle()
    val isRefreshing by vm.isRefreshing.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var dialogue by remember { mutableStateOf<DialogueStock?>(null) }
    val context = LocalContext.current

    LaunchedEffect(Unit) { vm.messages.collect { snackbar.showSnackbar(it) } }

    com.example.donaka100.ui.components.DonakaPullRefresh(
        isRefreshing = isRefreshing,
        onRefresh = vm::actualiser
    ) {
        Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Column(
                Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column {
                    Text("Gestion du Stock", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TexteFonce)
                    Text(
                        when (etat.onglet) {
                            OngletStock.INVENTAIRE -> "Matières premières"
                            OngletStock.ACHAT -> "Historique"
                            OngletStock.FOURNISSEURS -> "Fournisseurs & approvisionnement"
                        },
                        fontSize = 12.sp, color = TexteGris
                    )
                }
                DonakaSegmentedToggle(
                    options = OngletStock.entries,
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
                label = "stockTabTransition"
            ) { onglet ->
                when {
                    etat.erreur != null -> DonakaEmptyState(
                        titre = "Connexion impossible", message = etat.erreur!!,
                        icone = Icons.Default.CloudOff,
                        labelAction = "Réessayer", onAction = vm::charger
                    )

                    onglet == OngletStock.INVENTAIRE -> InventaireTab(
                        etat = etat, modifier = Modifier.fillMaxSize(),
                        onAchat = { dialogue = DialogueStock.Acheter() },
                        onNouveau = { dialogue = DialogueStock.Nouveau },
                        onRecherche = vm::onRecherche,
                        onFiltre = vm::onFiltre,
                        onCommander = { dialogue = DialogueStock.Acheter(it.nom, it.fournisseur) },
                        onAjuster = { dialogue = DialogueStock.Ajuster(it) },
                        onModifier = { dialogue = DialogueStock.Edition(it) },
                        onSupprimer = { ing ->
                            val utilises = etat.produitsUtilisant(ing.id)
                            dialogue =
                                if (utilises.isEmpty()) DialogueStock.Supprimer(ing)
                                else DialogueStock.Bloque(ing, utilises.map { it.nom })
                        }
                    )

                    onglet == OngletStock.ACHAT -> AchatsTab(
                        etat = etat, modifier = Modifier.fillMaxSize(),
                        onAchat = { dialogue = DialogueStock.Acheter() },
                        onRecherche = vm::onRechercheAchat,
                        onFiltre = vm::onFiltreAchat,
                        onModifier = { dialogue = DialogueStock.ModifierAchat(it) },
                        onAnnuler = { m ->
                            val raison = etat.raisonBlocageAnnulation(m)
                            dialogue = if (raison == null) DialogueStock.AnnulerAchat(m) else DialogueStock.AnnulationBloquee(raison)
                        }
                    )

                    else -> FournisseursTab(
                        etat = etat, modifier = Modifier.fillMaxSize(),
                        onNouveau = { dialogue = DialogueStock.CreerFournisseur },
                        onRecherche = vm::onRechercheFournisseur,
                        onRayon = vm::onRayonFournisseur,
                        onAppeler = { context.ouvrirAppel(it.telephone) },
                        onCommander = { f ->
                            val prefill = f.ingredients.firstOrNull { it.statut == StatutStock.CRITIQUE }?.nom
                                ?: f.ingredients.singleOrNull()?.nom.orEmpty()
                            dialogue = DialogueStock.Acheter(prefill, f.nom)
                        },
                        onModifier = { dialogue = DialogueStock.EditerFournisseur(it) },
                        onVoirAchats = { vm.voirAchats(it.nom) },
                        onSupprimerFiche = { dialogue = DialogueStock.SupprimerFiche(it) }
                    )
                }
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }
}

    when (val d = dialogue) {
        null -> Unit

        DialogueStock.Nouveau -> IngredientFormSheet(
            autresNoms = etat.ingredients.map { it.nom },
            rayons = etat.rayons, fournisseurs = etat.fournisseurs,
            onDismiss = { dialogue = null },
            onSave = { vm.creerIngredient(it); dialogue = null }
        )

        is DialogueStock.Edition -> IngredientFormSheet(
            initial = d.ingredient,
            autresNoms = etat.ingredients.filter { it.id != d.ingredient.id }.map { it.nom },
            rayons = etat.rayons, fournisseurs = etat.fournisseurs,
            uniteVerrouillee = etat.produitsUtilisant(d.ingredient.id).isNotEmpty(),
            onDismiss = { dialogue = null },
            onSave = { vm.modifierIngredient(d.ingredient.id, it); dialogue = null }
        )

        is DialogueStock.Ajuster -> AjustementSheet(
            ingredient = d.ingredient,
            onDismiss = { dialogue = null },
            onConfirm = { q, motif -> vm.ajuster(d.ingredient, q, motif); dialogue = null }
        )

        is DialogueStock.Acheter -> AchatFormSheet(
            ingredients = etat.ingredients, fournisseurs = etat.fournisseurs,
            nomInitial = d.nom, fournisseurInitial = d.fournisseur,
            onDismiss = { dialogue = null },
            onSave = { vm.acheter(it); dialogue = null }
        )

        is DialogueStock.Supprimer -> DonakaConfirmDialog(
            titre = "Supprimer cet ingrédient ?",
            message = "« ${d.ingredient.nom} » sera retiré du stock. L'historique des mouvements est conservé.",
            onConfirm = { vm.supprimerIngredient(d.ingredient); dialogue = null },
            onDismiss = { dialogue = null }
        )

        is DialogueStock.Bloque -> AlertDialog(
            onDismissRequest = { dialogue = null },
            containerColor = SurfaceBlanche,
            title = { Text("Suppression impossible", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "« ${d.ingredient.nom} » est utilisé dans la recette de : ${d.produits.joinToString(", ")}. " +
                            "Retire-le d'abord de ces recettes.",
                    color = TexteGris
                )
            },
            confirmButton = { DonakaButton("Compris", onClick = { dialogue = null }, style = StyleBouton.TEXTE) }
        )

        is DialogueStock.ModifierAchat -> ModifierAchatSheet(
            achat = d.achat, fournisseurs = etat.fournisseurs,
            onDismiss = { dialogue = null },
            onSave = { vm.modifierAchat(d.achat, it); dialogue = null }
        )

        is DialogueStock.AnnulerAchat -> DonakaConfirmDialog(
            titre = "Annuler cet achat ?",
            message = "${d.achat.quantite.avecUnite(d.achat.unite)} de ${d.achat.ingredientNom} seront retirés du stock." +
                    (d.achat.montant?.let { " Le montant de ${it.enMGA()} ne sera plus compté dans les dépenses." } ?: ""),
            labelConfirmer = "Oui, annuler",
            onConfirm = { vm.annulerAchat(d.achat); dialogue = null },
            onDismiss = { dialogue = null }
        )

        is DialogueStock.AnnulationBloquee -> AlertDialog(
            onDismissRequest = { dialogue = null },
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            title = { Text("Annulation impossible", fontWeight = FontWeight.Bold) },
            text = { Text(d.raison, color = TexteGris) },
            confirmButton = { DonakaButton("Compris", onClick = { dialogue = null }, style = StyleBouton.TEXTE) }
        )

        DialogueStock.CreerFournisseur -> FournisseurFormSheet(
            autresNoms = etat.fiches.map { it.nom },
            onDismiss = { dialogue = null },
            onSave = { vm.creerFournisseur(it); dialogue = null }
        )

        is DialogueStock.EditerFournisseur -> FournisseurFormSheet(
            initial = d.fournisseur,
            autresNoms = etat.listeFournisseurs
                .filter { it.nom.cleNom() != d.fournisseur.nom.cleNom() }.map { it.nom },
            onDismiss = { dialogue = null },
            onSave = { vm.modifierFournisseur(d.fournisseur.nom, it); dialogue = null }
        )

        is DialogueStock.SupprimerFiche -> DonakaConfirmDialog(
            titre = "Supprimer ce fournisseur ?",
            message = "« ${d.fournisseur.nom} » sera retiré de la liste." +
                    (if (d.fournisseur.ingredients.isNotEmpty())
                        " Ses ${d.fournisseur.ingredients.size} ingrédient(s) n'auront plus de fournisseur."
                    else "") +
                    " Les achats passés gardent son nom dans l'historique.",
            onConfirm = { vm.supprimerFournisseur(d.fournisseur.nom); dialogue = null },
            onDismiss = { dialogue = null }
        )
    }
}