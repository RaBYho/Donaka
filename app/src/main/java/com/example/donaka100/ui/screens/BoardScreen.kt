package com.example.donaka100.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.donaka100.data.BoardData
import com.example.donaka100.ui.BoardUiState
import com.example.donaka100.ui.BoardViewModel
import com.example.donaka100.ui.components.DonakaEmptyState
import com.example.donaka100.ui.forms.VenteFormSheet
import com.example.donaka100.ui.screens.board.*

@Composable
fun BoardScreen(
    onVoirStock: () -> Unit = {},
    onVoirDepenses: () -> Unit = {},
    vm: BoardViewModel = viewModel()
) {
    val etat by vm.etat.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var afficherVente by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        vm.messages.collect { snackbar.showSnackbar(it) }
    }

    Box(Modifier.fillMaxSize()) {
        when (val e = etat) {
            is BoardUiState.Loading -> BoardContent(
                data = BoardData(), isLoading = true,
                onVente = {}, onSortie = {}, onFournee = {},
                onVoirStock = {}, onVoirDepenses = {}
            )
            is BoardUiState.Success -> BoardContent(
                data = e.data, isLoading = false,
                onVente = { afficherVente = true },
                onSortie = { /* TODO : formulaire de dépense */ },
                onFournee = { /* TODO : page Fourneaux */ },
                onVoirStock = onVoirStock,
                onVoirDepenses = onVoirDepenses
            )
            is BoardUiState.Error -> DonakaEmptyState(
                titre = "Connexion impossible",
                message = e.message,
                icone = Icons.Default.CloudOff,
                labelAction = "Réessayer",
                onAction = vm::charger
            )
        }

        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }

    if (afficherVente) {
        VenteFormSheet(
            onDismiss = { afficherVente = false },
            onSave = { vm.enregistrerVente(it); afficherVente = false }
        )
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
    onVoirDepenses: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        userScrollEnabled = !isLoading
    ) {
        item { BoardGreeting(data.nomUtilisateur, data.fournilOuvert) }
        item { BoardTresorerieCard(data, isLoading) }
        item { BoardQuickActions(onVente, onSortie, onFournee) }
        item { BoardStocksCard(data.stocksSousSurveillance, isLoading, onVoirStock) }
        item {
            BoardCommandesCard(
                data.commandesDemain, data.totalPiecesDemain, data.heureLivraison, isLoading
            )
        }
        item { BoardCreancesCard(data.nbCreances, data.totalCreances, isLoading, onRelancer = {}) }
        item { BoardOperations(data.dernieresOperations, onToutVoir = onVoirDepenses) }
    }
}