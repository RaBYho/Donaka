package com.example.donaka100.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.donaka100.data.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
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
 */
class BoardViewModel(
    private val repository: BoardRepository = RoomBoardRepository()
) : ViewModel() {

    private val _relance = MutableStateFlow(0)

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages: Flow<String> = _messages.receiveAsFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val boardDataFlow = _relance.flatMapLatest {
        repository.observeBoard()
    }

    val etat: StateFlow<BoardUiState> = boardDataFlow
        .map<BoardData, BoardUiState> { BoardUiState.Success(it) }
        .catch { e ->
            if (e is CancellationException) throw e
            emit(BoardUiState.Error("Impossible de charger les données."))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BoardUiState.Loading
        )

    fun charger() {
        _relance.update { it + 1 }
    }

    fun actualiser() {
        viewModelScope.launch {
            _isRefreshing.value = true
            val debut = System.currentTimeMillis()
            _relance.update { it + 1 }
            verifierSauvegardePointExtension()
            val duree = System.currentTimeMillis() - debut
            if (duree < 500) {
                delay(500 - duree)
            }
            _isRefreshing.value = false
        }
    }

    private suspend fun verifierSauvegardePointExtension() {
        // Point d'extension pour vérification future de la sauvegarde
    }

    fun creerClient(c: NouveauClient) =
        action("Client « ${c.nom} » ajouté") { repository.creerClient(c) }

    fun acheter(a: NouvelAchat) =
        action("Achat enregistré") { repository.acheter(a) }

    private fun action(succes: String, bloc: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { bloc() }
                _messages.send(succes)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _messages.send(e.message ?: "Échec de l'enregistrement. Réessaie.")
            }
        }
    }
}