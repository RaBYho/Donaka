package com.example.donaka100.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.donaka100.data.BoardData
import com.example.donaka100.ui.BoardUiState
import com.example.donaka100.ui.BoardViewModel
import com.example.donaka100.ui.components.DonakaEmptyState
import com.example.donaka100.ui.screens.board.*
import com.example.donaka100.ui.forms.AchatFormSheet
import com.example.donaka100.ui.forms.ClientFormSheet

private enum class DialogueBoard { CLIENT, ACHAT }

/**
 * Écran d'accueil Tableau de bord avec animations fluides et chargement apaisant.
 */
@Composable
fun BoardScreen(
    onVoirStock: () -> Unit = {},
    onVoirCommande: () -> Unit = {},
    onVoirFourneaux: () -> Unit = {},
    onVoirDepenses: () -> Unit = {},
    vm: BoardViewModel = viewModel()
) {
    val etat by vm.etat.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var dialogue by remember { mutableStateOf<DialogueBoard?>(null) }

    LaunchedEffect(Unit) { vm.messages.collect { snackbar.showSnackbar(it) } }
    LaunchedEffect(Unit) { vm.actualiser() }

    Box(Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = etat,
            transitionSpec = {
                fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing)) togetherWith
                        fadeOut(animationSpec = tween(200, easing = FastOutSlowInEasing))
            },
            label = "boardStateTransition"
        ) { e ->
            when (e) {
                is BoardUiState.Loading -> BoardContent(
                    data = BoardData(), isLoading = true,
                    onVente = {}, onSortie = {}, onFournee = {}, onVoirStock = {}, onVoirCommande = {}, onVoirDepenses = {}
                )
                is BoardUiState.Success -> BoardContent(
                    data = e.data, isLoading = false,
                    onVente = { dialogue = DialogueBoard.CLIENT },
                    onSortie = { dialogue = DialogueBoard.ACHAT },
                    onFournee = onVoirFourneaux,
                    onVoirStock = onVoirStock,
                    onVoirCommande = onVoirCommande,
                    onVoirDepenses = onVoirDepenses
                )
                is BoardUiState.Error -> DonakaEmptyState(
                    titre = "Connexion impossible", message = e.message,
                    icone = Icons.Default.CloudOff,
                    labelAction = "Réessayer", onAction = vm::charger
                )
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }

    val data = (etat as? BoardUiState.Success)?.data
    when (dialogue) {
        DialogueBoard.CLIENT -> ClientFormSheet(
            onDismiss = { dialogue = null },
            onSave = { vm.creerClient(it); dialogue = null }
        )
        DialogueBoard.ACHAT -> AchatFormSheet(
            ingredients = data?.stocks.orEmpty(),
            fournisseurs = data?.fournisseurs.orEmpty(),
            onDismiss = { dialogue = null },
            onSave = { vm.acheter(it); dialogue = null }
        )
        null -> Unit
    }
}

@Composable
private fun BoardContent(
    data: BoardData,
    isLoading: Boolean,
    onVente: () -> Unit,
    onSortie: () -> Unit,
    onFournee: () -> Unit,
    onVoirStock: () -> Unit,
    onVoirCommande: () -> Unit,
    onVoirDepenses: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().graphicsLayer { alpha = 0.99f },
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        userScrollEnabled = !isLoading
    ) {
        item(key = "greeting") { BoardGreeting(data.nomUtilisateur, data.fournilOuvert) }
        item(key = "tresorerie") { BoardTresorerieCard(data, isLoading) }
        item(key = "quick_actions") { BoardQuickActions(onVente, onSortie, onFournee) }
        item(key = "stocks") { BoardStocksCard(data.stocksSousSurveillance, isLoading, onVoirStock) }
        item(key = "commandes") {
            BoardCommandesCard(
                data.commandesDemain, data.totalPiecesDemain, data.heureLivraison, isLoading
            )
        }
        item(key = "creances") { BoardCreancesCard(data.nbCreances, data.totalCreances, isLoading, onRelancer = onVoirCommande) }
        item(key = "operations") { BoardOperations(data.dernieresOperations) }
    }
}