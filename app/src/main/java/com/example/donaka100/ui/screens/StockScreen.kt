package com.example.donaka100.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.donaka100.data.Ingredient
import com.example.donaka100.ui.OngletStock
import com.example.donaka100.ui.StockViewModel
import com.example.donaka100.ui.components.*
import com.example.donaka100.ui.forms.AchatFormSheet
import com.example.donaka100.ui.forms.AjustementSheet
import com.example.donaka100.ui.forms.IngredientFormSheet
import com.example.donaka100.ui.screens.stock.InventaireTab
import com.example.donaka100.ui.theme.*
import com.example.donaka100.data.MouvementStock
import com.example.donaka100.ui.forms.ModifierAchatSheet
import com.example.donaka100.ui.screens.stock.AchatsTab
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
}

@Composable
fun StockScreen(vm: StockViewModel = viewModel()) {
    val etat by vm.etat.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var dialogue by remember { mutableStateOf<DialogueStock?>(null) }

    LaunchedEffect(Unit) { vm.messages.collect { snackbar.showSnackbar(it) } }

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
                            OngletStock.HISTORIQUE -> "Historique"
                            OngletStock.FOURNISSEURS -> "Fournisseurs"
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

            when {
                etat.erreur != null -> DonakaEmptyState(
                    titre = "Connexion impossible", message = etat.erreur!!,
                    icone = Icons.Default.CloudOff,
                    labelAction = "Réessayer", onAction = vm::charger
                )

                etat.onglet == OngletStock.INVENTAIRE -> InventaireTab(
                    etat = etat, modifier = Modifier.weight(1f),
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
                etat.onglet == OngletStock.HISTORIQUE -> AchatsTab(
                    etat = etat, modifier = Modifier.weight(1f),
                    onAchat = { dialogue = DialogueStock.Acheter() },
                    onRecherche = vm::onRechercheAchat,
                    onFiltre = vm::onFiltreAchat,
                    onModifier = { dialogue = DialogueStock.ModifierAchat(it) },
                    onAnnuler = { m ->
                        val raison = etat.raisonBlocageAnnulation(m)
                        dialogue = if (raison == null) DialogueStock.AnnulerAchat(m) else DialogueStock.AnnulationBloquee(raison)
                    }
                )
                else -> DonakaEmptyState(
                    titre = "Bientôt disponible",
                    message = "L'onglet « ${etat.onglet.libelle} » arrive dans une prochaine étape.",
                    icone = Icons.Default.Schedule
                )
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
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
            containerColor = SurfaceBlanche,
            title = { Text("Annulation impossible", fontWeight = FontWeight.Bold) },
            text = { Text(d.raison, color = TexteGris) },
            confirmButton = { DonakaButton("Compris", onClick = { dialogue = null }, style = StyleBouton.TEXTE) }
        )
    }
}