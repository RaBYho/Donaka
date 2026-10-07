package com.example.donaka100.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.donaka100.data.FicheFournisseur
import com.example.donaka100.data.Fournee
import com.example.donaka100.data.Ingredient
import com.example.donaka100.data.ModificationAchat
import com.example.donaka100.data.MouvementStock
import com.example.donaka100.data.NouveauFournisseur
import com.example.donaka100.data.NouvelAchat
import com.example.donaka100.data.NouvelIngredient
import com.example.donaka100.data.Produit
import com.example.donaka100.data.RoomStockRepository
import com.example.donaka100.data.StatutStock
import com.example.donaka100.data.StockRepository
import com.example.donaka100.data.cleNom
import com.example.donaka100.data.nettoyerNom
import com.example.donaka100.ui.components.avecUnite
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth

enum class OngletStock(val libelle: String) {
    INVENTAIRE("Inventaire"), ACHAT("Achats"), FOURNISSEURS("Fournisseurs")
}

sealed interface FiltreStock {
    data object Tout : FiltreStock
    data object Alertes : FiltreStock
    data object ARenseigner : FiltreStock
    data class Rayon(val nom: String) : FiltreStock
}

sealed interface FiltreAchat {
    data object Tous : FiltreAchat
    data object Aujourdhui : FiltreAchat
    data object CeMois : FiltreAchat
    data class Rayon(val nom: String) : FiltreAchat
}

data class Fournisseur(
    val nom: String,
    val fiche: FicheFournisseur?,
    val ingredients: List<Ingredient>,
    val achats: List<MouvementStock>
) {
    val telephone: String get() = fiche?.telephone.orEmpty()
    val adresse: String get() = fiche?.adresse.orEmpty()
    val delai: String get() = fiche?.delai.orEmpty()
    val dernierAchat: MouvementStock? get() = achats.maxByOrNull { it.dateHeure }
    val rayons: List<String>
        get() = ingredients.map { it.rayon.trim() }.filter { it.isNotEmpty() }.distinct()
    val rayonPrincipal: String?
        get() = ingredients.map { it.rayon.trim() }.filter { it.isNotEmpty() }
            .groupingBy { it }.eachCount().maxByOrNull { it.value }?.key
}

data class FiltresStockUi(
    val onglet: OngletStock = OngletStock.INVENTAIRE,
    val recherche: String = "",
    val filtre: FiltreStock = FiltreStock.Tout,
    val rechercheAchat: String = "",
    val filtreAchat: FiltreAchat = FiltreAchat.Tous,
    val rechercheFournisseur: String = "",
    val rayonFournisseur: String? = null
)

private data class DonneesStock(
    val ingredients: List<Ingredient>,
    val produits: List<Produit>,
    val fournees: List<Fournee>,
    val mouvements: List<MouvementStock>,
    val fiches: List<FicheFournisseur>
)

data class StockUiState(
    val isLoading: Boolean = true,
    val erreur: String? = null,
    val ingredients: List<Ingredient> = emptyList(),
    val produits: List<Produit> = emptyList(),
    val fournees: List<Fournee> = emptyList(),
    val onglet: OngletStock = OngletStock.INVENTAIRE,
    val recherche: String = "",
    val filtre: FiltreStock = FiltreStock.Tout,
    val mouvements: List<MouvementStock> = emptyList(),
    val rechercheAchat: String = "",
    val filtreAchat: FiltreAchat = FiltreAchat.Tous,
    val fiches: List<FicheFournisseur> = emptyList(),
    val rechercheFournisseur: String = "",
    val rayonFournisseur: String? = null
) {
    val rayons: List<String>
        get() = ingredients.map { it.rayon.trim() }.filter { it.isNotEmpty() }
            .distinct().sortedBy { it.lowercase() }

    // ----- Fournisseurs -----
    val listeFournisseurs: List<Fournisseur>
        get() {
            val noms = LinkedHashMap<String, String>()
            fiches.forEach { noms.putIfAbsent(it.nom.cleNom(), it.nom.nettoyerNom()) }
            ingredients.filter { it.fournisseur.isNotBlank() }
                .forEach { noms.putIfAbsent(it.fournisseur.cleNom(), it.fournisseur.nettoyerNom()) }

            return noms.map { (cle, nom) ->
                Fournisseur(
                    nom = nom,
                    fiche = fiches.firstOrNull { it.nom.cleNom() == cle },
                    ingredients = ingredients.filter { it.fournisseur.cleNom() == cle },
                    achats = mouvements.filter { it.estAchat && !it.annule && it.fournisseur.cleNom() == cle }
                )
            }.sortedBy { it.nom.lowercase() }
        }

    val fournisseurs: List<String> get() = listeFournisseurs.map { it.nom }

    val rayonsFournisseurs: List<String>
        get() = listeFournisseurs.flatMap { it.rayons }.distinct().sortedBy { it.lowercase() }

    val fournisseursAffiches: List<Fournisseur>
        get() {
            val q = rechercheFournisseur.trim().lowercase()
            val chiffres = q.filter { it.isDigit() }
            val rayon = rayonFournisseur?.takeIf { it in rayonsFournisseurs }
            return listeFournisseurs.filter { f ->
                (rayon == null || rayon in f.rayons) &&
                        (q.isEmpty() ||
                                f.nom.lowercase().contains(q) ||
                                f.ingredients.any { it.nom.lowercase().contains(q) } ||
                                (chiffres.isNotEmpty() && f.telephone.contains(chiffres)))
            }
        }

    val critiques: List<Ingredient> get() = ingredients.filter { it.statut == StatutStock.CRITIQUE }
    val aRenseigner: List<Ingredient> get() = ingredients.filter { it.statut == StatutStock.A_RENSEIGNER }

    val filtreEffectif: FiltreStock
        get() = when (val f = filtre) {
            is FiltreStock.Rayon -> if (f.nom in rayons) f else FiltreStock.Tout
            FiltreStock.ARenseigner -> if (aRenseigner.isNotEmpty()) f else FiltreStock.Tout
            else -> f
        }

    val ingredientsAffiches: List<Ingredient>
        get() {
            val q = recherche.trim().lowercase()
            val f = filtreEffectif
            return ingredients
                .filter { i ->
                    val okFiltre = when (f) {
                        FiltreStock.Tout -> true
                        FiltreStock.Alertes -> i.statut == StatutStock.CRITIQUE
                        FiltreStock.ARenseigner -> i.statut == StatutStock.A_RENSEIGNER
                        is FiltreStock.Rayon -> i.rayon.trim() == f.nom
                    }
                    val okRecherche = q.isEmpty() ||
                            i.nom.lowercase().contains(q) ||
                            i.rayon.lowercase().contains(q) ||
                            i.fournisseur.lowercase().contains(q)
                    okFiltre && okRecherche
                }
                .sortedWith(compareBy({ it.statut.priorite }, { it.nom.lowercase() }))
        }

    // ----- Achats -----
    val achats: List<MouvementStock> get() = mouvements.filter { it.estAchat }
    private val achatsValides: List<MouvementStock> get() = achats.filter { !it.annule }

    private fun rayonDe(m: MouvementStock): String =
        ingredients.firstOrNull { it.id == m.ingredientId }?.rayon?.trim().orEmpty()

    val rayonsAchats: List<String>
        get() = achats.map { rayonDe(it) }.filter { it.isNotEmpty() }.distinct().sortedBy { it.lowercase() }

    val nbAchatsAujourdhui: Int
        get() = achatsValides.count { it.dateHeure.toLocalDate() == LocalDate.now() }

    val depenseAujourdhui: Long
        get() = achatsValides.filter { it.dateHeure.toLocalDate() == LocalDate.now() }.sumOf { it.montant ?: 0L }

    val depenseMois: Long
        get() {
            val mois = YearMonth.now()
            return achatsValides.filter { YearMonth.from(it.dateHeure) == mois }.sumOf { it.montant ?: 0L }
        }

    val filtreAchatEffectif: FiltreAchat
        get() = when (val f = filtreAchat) {
            is FiltreAchat.Rayon -> if (f.nom in rayonsAchats) f else FiltreAchat.Tous
            else -> f
        }

    val achatsAffiches: List<MouvementStock>
        get() {
            val auj = LocalDate.now()
            val q = rechercheAchat.trim().lowercase()
            val f = filtreAchatEffectif
            return achats
                .filter { m ->
                    val date = m.dateHeure.toLocalDate()
                    when (f) {
                        FiltreAchat.Tous -> true
                        FiltreAchat.Aujourdhui -> date == auj
                        FiltreAchat.CeMois -> YearMonth.from(date) == YearMonth.from(auj)
                        is FiltreAchat.Rayon -> rayonDe(m) == f.nom
                    }
                }
                .filter {
                    q.isEmpty() || it.ingredientNom.lowercase().contains(q) ||
                            it.fournisseur.lowercase().contains(q)
                }
                .sortedByDescending { it.dateHeure }
        }

    fun raisonBlocageAnnulation(m: MouvementStock): String? {
        val ing = ingredients.firstOrNull { it.id == m.ingredientId }
            ?: return "L'ingrédient « ${m.ingredientNom} » n'existe plus dans le stock."
        return if (ing.quantite + 0.0005 < m.quantite)
            "Il reste ${ing.quantite.avecUnite(ing.unite)} de ${ing.nom}, moins que les " +
                    "${m.quantite.avecUnite(ing.unite)} achetés : une partie a déjà été utilisée. " +
                    "Utilise « Ajuster » dans l'inventaire pour corriger."
        else null
    }

    fun produitsUtilisant(ingredientId: String): List<Produit> =
        produits.filter { p -> !p.archive && p.recette.any { it.ingredientId == ingredientId } }

    fun reserveJours(ing: Ingredient): Int? {
        if (ing.quantite <= 0) return null
        val depuis = LocalDate.now().minusDays(30)
        val consos = fournees
            .filter { !it.annulee && !it.date.isBefore(depuis) }
            .mapNotNull { f -> f.consommations.firstOrNull { it.ingredientId == ing.id }?.deduit?.takeIf { it > 0 } }
        if (consos.isEmpty()) return null
        return (ing.quantite / consos.average()).toInt()
    }
}

/**
 * ViewModel réactif pour le Stock.
 * Observe directement la base de données Room via des Flows.
 */
class StockViewModel(
    private val repository: StockRepository = RoomStockRepository()
) : ViewModel() {

    private val _filtres = MutableStateFlow(FiltresStockUi())
    private val _relance = MutableStateFlow(0)

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages: Flow<String> = _messages.receiveAsFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val donneesStockFlow = _relance.flatMapLatest {
        combine(
            repository.observeIngredients(),
            repository.observeProduits(),
            repository.observeFournees(),
            repository.observeMouvements(),
            repository.observeFiches()
        ) { ingredients, produits, fournees, mouvements, fiches ->
            DonneesStock(ingredients, produits, fournees, mouvements, fiches)
        }
    }

    val etat: StateFlow<StockUiState> = combine(
        donneesStockFlow,
        _filtres
    ) { d, f ->
        StockUiState(
            isLoading = false,
            erreur = null,
            ingredients = d.ingredients,
            produits = d.produits,
            fournees = d.fournees,
            mouvements = d.mouvements,
            fiches = d.fiches,
            onglet = f.onglet,
            recherche = f.recherche,
            filtre = f.filtre,
            rechercheAchat = f.rechercheAchat,
            filtreAchat = f.filtreAchat,
            rechercheFournisseur = f.rechercheFournisseur,
            rayonFournisseur = f.rayonFournisseur
        )
    }.catch { e ->
        if (e is CancellationException) throw e
        emit(StockUiState(isLoading = false, erreur = "Impossible de charger le stock."))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StockUiState(isLoading = true)
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

    fun onOnglet(o: OngletStock) = _filtres.update { it.copy(onglet = o) }
    fun onRecherche(t: String) = _filtres.update { it.copy(recherche = t) }
    fun onFiltre(f: FiltreStock) = _filtres.update { it.copy(filtre = f) }
    fun onRechercheAchat(t: String) = _filtres.update { it.copy(rechercheAchat = t) }
    fun onFiltreAchat(f: FiltreAchat) = _filtres.update { it.copy(filtreAchat = f) }
    fun onRechercheFournisseur(t: String) = _filtres.update { it.copy(rechercheFournisseur = t) }
    fun onRayonFournisseur(r: String?) = _filtres.update { it.copy(rayonFournisseur = r) }

    fun voirAchats(nom: String) = _filtres.update {
        it.copy(onglet = OngletStock.ACHAT, rechercheAchat = nom, filtreAchat = FiltreAchat.Tous)
    }

    fun modifierAchat(m: MouvementStock, mod: ModificationAchat) =
        action("Achat mis à jour") { repository.modifierAchat(m.id, mod) }

    fun annulerAchat(m: MouvementStock) =
        action("Achat annulé · ${m.quantite.avecUnite(m.unite)} retirés du stock") {
            repository.annulerAchat(m.id)
        }

    fun creerFournisseur(f: NouveauFournisseur) =
        action("« ${f.nom.nettoyerNom()} » ajouté aux fournisseurs") { repository.creerFournisseur(f) }

    fun modifierFournisseur(ancienNom: String, f: NouveauFournisseur) =
        action("Fiche mise à jour") { repository.modifierFournisseur(ancienNom, f) }

    fun supprimerFournisseur(nom: String) =
        action("« $nom » supprimé") { repository.supprimerFournisseur(nom) }

    fun creerIngredient(i: NouvelIngredient) =
        action("« ${i.nom.nettoyerNom()} » ajouté au stock") { repository.creerIngredient(i) }

    fun modifierIngredient(id: String, i: NouvelIngredient) =
        action("Fiche mise à jour") { repository.modifierIngredient(id, i) }

    fun supprimerIngredient(i: Ingredient) =
        action("« ${i.nom} » supprimé") { repository.supprimerIngredient(i.id) }

    fun ajuster(i: Ingredient, quantite: Double, motif: String) =
        action("Stock de ${i.nom} ajusté à ${quantite.avecUnite(i.unite)}") {
            repository.ajuster(i.id, quantite, motif)
        }

    fun acheter(a: NouvelAchat) = actionMsg {
        val existant = etat.value.ingredients.firstOrNull { it.nom.cleNom() == a.nom.cleNom() }
        val unite = existant?.unite ?: a.unite
        val cree = repository.acheter(a)
        "Achat enregistré : +${a.quantite.avecUnite(unite)}" + if (cree) " · ingrédient ajouté au stock" else ""
    }

    private fun action(succes: String, bloc: suspend () -> Unit) = actionMsg { bloc(); succes }

    private fun actionMsg(bloc: suspend () -> String) {
        viewModelScope.launch {
            try {
                val message = withContext(Dispatchers.IO) { bloc() }
                _messages.send(message)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _messages.send(e.message ?: "Échec de l'opération. Réessaie.")
            }
        }
    }
}
