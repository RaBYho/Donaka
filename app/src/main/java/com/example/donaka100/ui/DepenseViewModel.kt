package com.example.donaka100.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.donaka100.data.*
import com.example.donaka100.ui.components.avecUnite
import com.example.donaka100.ui.components.enMGA
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

/** Une ligne de l'historique : dépense libre OU achat de stock */
data class LigneDepense(
    val id: String,
    val achat: Boolean,
    val titre: String,
    val detail: String,
    val categorie: String,
    val montant: Long,
    val dateHeure: LocalDateTime,
    val mode: ModeReglement?,
    val depense: Depense?          // null pour un achat de stock
)

sealed interface FiltreDepense {
    data object Tout : FiltreDepense
    data object Aujourdhui : FiltreDepense
    data object CeMois : FiltreDepense
    data class Categorie(val nom: String) : FiltreDepense
}

data class DepenseUiState(
    val isLoading: Boolean = true,
    val erreur: String? = null,
    val depenses: List<Depense> = emptyList(),
    val achats: List<MouvementStock> = emptyList(),
    val recherche: String = "",
    val filtre: FiltreDepense = FiltreDepense.Tout
) {
    val lignes: List<LigneDepense>
        get() {
            val libres = depenses.map { d ->
                LigneDepense(
                    id = "d-${d.id}", achat = false,
                    titre = if (d.note.isBlank()) d.categorie else "${d.categorie} • ${d.note}",
                    detail = "", categorie = d.categorie, montant = d.montant,
                    dateHeure = d.dateHeure, mode = d.mode, depense = d
                )
            }
            // Achats annulés ou sans montant : pas de dépense
            val deStock = achats.filter { !it.annule && (it.montant ?: 0L) > 0L }.map { m ->
                LigneDepense(
                    id = "a-${m.id}", achat = true,
                    titre = "Achat : ${m.ingredientNom} · ${m.quantite.avecUnite(m.unite)}",
                    detail = m.fournisseur, categorie = CATEGORIE_ACHATS, montant = m.montant ?: 0L,
                    dateHeure = m.dateHeure, mode = m.mode, depense = null
                )
            }
            return libres + deStock
        }

    /** Catégories saisies par le chef (sans « Achats de stock ») : suggestions du formulaire */
    val categories: List<String>
        get() = depenses.map { it.categorie.trim() }.filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }.sortedBy { it.lowercase() }

    val categoriesLignes: List<String>
        get() = lignes.map { it.categorie }.distinct().sortedBy { it.lowercase() }

    private val lignesMois: List<LigneDepense>
        get() = lignes.filter { YearMonth.from(it.dateHeure) == YearMonth.now() }

    val totalMois: Long get() = lignesMois.sumOf { it.montant }

    /** Catégorie -> total du mois, de la plus grosse à la plus petite */
    val repartition: List<Pair<String, Long>>
        get() = lignesMois.groupBy { it.categorie }
            .map { (c, l) -> c to l.sumOf { it.montant } }
            .sortedByDescending { it.second }

    val filtreEffectif: FiltreDepense
        get() = when (val f = filtre) {
            is FiltreDepense.Categorie -> if (f.nom in categoriesLignes) f else FiltreDepense.Tout
            else -> f
        }

    val nbAujourdhui: Int get() = lignes.count { it.dateHeure.toLocalDate() == LocalDate.now() }

    val lignesAffichees: List<LigneDepense>
        get() {
            val auj = LocalDate.now()
            val q = recherche.trim().lowercase()
            val f = filtreEffectif
            return lignes
                .filter { l ->
                    val date = l.dateHeure.toLocalDate()
                    when (f) {
                        FiltreDepense.Tout -> true
                        FiltreDepense.Aujourdhui -> date == auj
                        FiltreDepense.CeMois -> YearMonth.from(date) == YearMonth.from(auj)
                        is FiltreDepense.Categorie -> l.categorie == f.nom
                    }
                }
                .filter {
                    q.isEmpty() || it.titre.lowercase().contains(q) ||
                            it.categorie.lowercase().contains(q) || it.detail.lowercase().contains(q)
                }
                .sortedByDescending { it.dateHeure }
        }
}

class DepenseViewModel(
    // Demain : ApiDepenseRepository(...) ici. Mets avecDemo = true pour tester avec des données.
    private val repository: DepenseRepository = FakeDepenseRepository()
) : ViewModel() {

    private val _etat = MutableStateFlow(DepenseUiState())
    val etat: StateFlow<DepenseUiState> = _etat.asStateFlow()

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages: Flow<String> = _messages.receiveAsFlow()

    init { charger() }

    private suspend fun rafraichir() {
        val depenses = repository.getDepenses()
        val achats = repository.getAchats()
        _etat.update { it.copy(depenses = depenses, achats = achats) }
    }

    fun charger() {
        viewModelScope.launch {
            _etat.update { it.copy(isLoading = true, erreur = null) }
            try {
                rafraichir()
                _etat.update { it.copy(isLoading = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _etat.update { it.copy(isLoading = false, erreur = "Impossible de charger les dépenses.") }
            }
        }
    }

    fun onRecherche(t: String) = _etat.update { it.copy(recherche = t) }
    fun onFiltre(f: FiltreDepense) = _etat.update { it.copy(filtre = f) }

    fun creer(d: NouvelleDepense) =
        action("Dépense de ${d.montant.enMGA()} enregistrée") { repository.creer(d) }

    fun modifier(id: String, d: NouvelleDepense) =
        action("Dépense mise à jour") { repository.modifier(id, d) }

    fun supprimer(d: Depense) =
        action("Dépense supprimée") { repository.supprimer(d.id) }

    private fun action(succes: String, bloc: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                bloc()
                rafraichir()
                _messages.send(succes)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _messages.send("Échec de l'opération. Réessaie.")
            }
        }
    }
}