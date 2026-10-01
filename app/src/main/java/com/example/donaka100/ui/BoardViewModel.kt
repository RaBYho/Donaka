package com.example.donaka100.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.donaka100.data.*
import com.example.donaka100.ui.forms.FormulaireVente
import com.example.donaka100.ui.forms.ModePaiement
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface BoardUiState {
    data object Loading : BoardUiState
    data class Success(val data: BoardData) : BoardUiState
    data class Error(val message: String) : BoardUiState
}

class BoardViewModel(
    // Demain : ApiBoardRepository(...) ici, c'est le seul endroit à changer
    private val repository: BoardRepository = FakeBoardRepository()
) : ViewModel() {

    private val _etat = MutableStateFlow<BoardUiState>(BoardUiState.Loading)
    val etat: StateFlow<BoardUiState> = _etat.asStateFlow()

    // Messages ponctuels (snackbar) : consommés une seule fois
    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages: Flow<String> = _messages.receiveAsFlow()

    init { charger() }

    fun charger() {
        viewModelScope.launch {
            _etat.value = BoardUiState.Loading
            try {
                _etat.value = BoardUiState.Success(repository.getBoard())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _etat.value = BoardUiState.Error("Impossible de charger les données.")
            }
        }
    }

    fun enregistrerVente(form: FormulaireVente) {
        viewModelScope.launch {
            try {
                repository.ajouterVente(
                    NouvelleVente(
                        libelle = form.client.ifBlank { form.produit },
                        detail = form.mode.libelle,
                        montant = form.total,
                        aCredit = form.mode == ModePaiement.CREDIT
                    )
                )
                // Recharge sans repasser par Loading, donc pas de clignotement
                _etat.value = BoardUiState.Success(repository.getBoard())
                _messages.send("Vente enregistrée")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _messages.send("Échec de l'enregistrement. Réessaie.")
            }
        }
    }
}