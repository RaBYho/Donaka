package com.example.donaka100.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.donaka100.DonakaApplication
import com.example.donaka100.data.Reglages
import com.example.donaka100.data.ReglagesRepository
import com.example.donaka100.data.local.DonakaDatabase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ParametresViewModel(
    private val repository: ReglagesRepository = DonakaApplication.instance.reglagesRepository,
    private val database: DonakaDatabase = DonakaApplication.instance.database
) : ViewModel() {

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages: Flow<String> = _messages.receiveAsFlow()

    val reglages: StateFlow<Reglages> = repository.observeReglages()
        .catch { e ->
            if (e is CancellationException) throw e
            emit(Reglages())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Reglages()
        )

    fun enregistrerProfil(nomChef: String, nomBoulangerie: String) {
        viewModelScope.launch {
            try {
                repository.setNomChef(nomChef)
                repository.setNomBoulangerie(nomBoulangerie)
                _messages.send("Profil mis à jour")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _messages.send("Échec de la mise à jour du profil.")
            }
        }
    }

    fun effacerTout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.effacerTout(database)
                _messages.send("Données réinitialisées")
                onSuccess()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _messages.send("Échec de la réinitialisation.")
            }
        }
    }
}
