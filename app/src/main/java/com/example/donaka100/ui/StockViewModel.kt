package com.example.donaka100.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.donaka100.data.*
import com.example.donaka100.ui.components.avecUnite
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
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
 * Optimisé avec des opérations asynchrones sur Dispatchers.IO.
 */
class StockViewModel(
    private val repository: StockRepository = RoomStockRepository()
) : ViewModel() {

    private val _etat = MutableStateFlow(StockUiState())
    val etat: StateFlow<StockUiState> = _etat.asStateFlow()

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages: Flow<String> = _messages.receiveAsFlow()

    init { charger() }

    private suspend fun rafraichir() = withContext(Dispatchers.IO) {
        coroutineScope {
            val ingDef = async { repository.getIngredients() }
            val prodDef = async { repository.getProduits() }
            val fourDef = async { repository.getFournees() }
            val mouvDef = async { repository.getMouvements() }
            val fichDef = async { repository.getFiches() }

            val ingredients = ingDef.await()
            val produits = prodDef.await()
            val fournees = fourDef.await()
            val mouvements = mouvDef.await()
            val fiches = fichDef.await()

            _etat.update {
                it.copy(
                    ingredients = ingredients, produits = produits, fournees = fournees,
                    mouvements = mouvements, fiches = fiches
                )
            }
        }
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
                _etat.update { it.copy(isLoading = false, erreur = "Impossible de charger le stock.") }
            }
        }
    }

    fun onOnglet(o: OngletStock) = _etat.update { it.copy(onglet = o) }
    fun onRecherche(t: String) = _etat.update { it.copy(recherche = t) }
    fun onFiltre(f: FiltreStock) = _etat.update { it.copy(filtre = f) }
    fun onRechercheAchat(t: String) = _etat.update { it.copy(rechercheAchat = t) }
    fun onFiltreAchat(f: FiltreAchat) = _etat.update { it.copy(filtreAchat = f) }

    fun modifierAchat(m: MouvementStock, mod: ModificationAchat) =
        action("Achat mis à jour") { repository.modifierAchat(m.id, mod) }

    fun annulerAchat(m: MouvementStock) =
        action("Achat annulé · ${m.quantite.avecUnite(m.unite)} retirés du stock") {
            repository.annulerAchat(m.id)
        }

    fun onRechercheFournisseur(t: String) = _etat.update { it.copy(rechercheFournisseur = t) }
    fun onRayonFournisseur(r: String?) = _etat.update { it.copy(rayonFournisseur = r) }

    fun creerFournisseur(f: NouveauFournisseur) =
        action("« ${f.nom.nettoyerNom()} » ajouté aux fournisseurs") { repository.creerFournisseur(f) }

    fun modifierFournisseur(ancienNom: String, f: NouveauFournisseur) =
        action("Fiche mise à jour") { repository.modifierFournisseur(ancienNom, f) }

    fun supprimerFournisseur(nom: String) =
        action("« $nom » supprimé") { repository.supprimerFournisseur(nom) }

    fun voirAchats(nom: String) = _etat.update {
        it.copy(onglet = OngletStock.ACHAT, rechercheAchat = nom, filtreAchat = FiltreAchat.Tous)
    }

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
        val existant = _etat.value.ingredients.firstOrNull { it.nom.cleNom() == a.nom.cleNom() }
        val unite = existant?.unite ?: a.unite
        val cree = repository.acheter(a)
        "Achat enregistré : +${a.quantite.avecUnite(unite)}" + if (cree) " · ingrédient ajouté au stock" else ""
    }

    private fun action(succes: String, bloc: suspend () -> Unit) = actionMsg { bloc(); succes }

    private fun actionMsg(bloc: suspend () -> String) {
        viewModelScope.launch {
            try {
                val message = withContext(Dispatchers.IO) { bloc() }
                rafraichir()
                _messages.send(message)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _messages.send("Échec de l'opération. Réessaie.")
            }
        }
    }

    /** À l'ouverture de l'écran : met à jour en arrière-plan sans skeleton */
    fun actualiser() {
        viewModelScope.launch {
            try {
                rafraichir()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) { }
        }
    }
}