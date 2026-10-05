package com.example.donaka100.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.donaka100.data.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface BoardUiState {
    data object Loading : BoardUiState
    data class Success(val data: BoardData) : BoardUiState
    data class Error(val message: String) : BoardUiState
}

/**
 * ViewModel réactif pour le Tableau de bord Donaka.
 * Exécute tous les calculs sur Dispatchers.Default / IO pour éviter les saccades sur le Thread UI.
 */
class BoardViewModel(
    private val repository: BoardRepository = RoomBoardRepository()
) : ViewModel() {

    private val _etat = MutableStateFlow<BoardUiState>(BoardUiState.Loading)
    val etat: StateFlow<BoardUiState> = _etat.asStateFlow()

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages: Flow<String> = _messages.receiveAsFlow()

    init { charger() }

    fun charger() {
        viewModelScope.launch {
            _etat.value = BoardUiState.Loading
            try {
                // Transfert du calcul sur le thread d'arrière-plan pour une réactivité maximale
                val result = withContext(Dispatchers.Default) { repository.getBoard() }
                _etat.value = BoardUiState.Success(result)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _etat.value = BoardUiState.Error("Impossible de charger les données.")
            }
        }
    }

    /** À l'ouverture de l'écran : met à jour en arrière-plan sans skeleton intrusive */
    fun actualiser() {
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.Default) { repository.getBoard() }
                _etat.value = BoardUiState.Success(result)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) { }
        }
    }

    fun creerClient(c: NouveauClient) =
        action("Client « ${c.nom} » ajouté") { repository.creerClient(c) }

    fun acheter(a: NouvelAchat) =
        action("Achat enregistré") { repository.acheter(a) }

    private fun action(succes: String, bloc: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { bloc() }
                val result = withContext(Dispatchers.Default) { repository.getBoard() }
                _etat.value = BoardUiState.Success(result)
                _messages.send(succes)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _messages.send("Échec de l'enregistrement. Réessaie.")
            }
        }
    }
}