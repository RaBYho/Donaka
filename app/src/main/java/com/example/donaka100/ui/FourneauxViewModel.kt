package com.example.donaka100.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.donaka100.data.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.DayOfWeek
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

enum class OngletFourneaux(val libelle: String) {
    A_PREPARER("À Préparer"), RECETTES("Recettes"), HISTORIQUE("Historique")
}

/** Une ligne du plan : total d'un produit sur toutes les commandes de demain */
data class LignePlan(
    val produitId: String,
    val nom: String,
    val quantite: Int,
    val produit: Produit?
)
enum class TriProduit(val libelle: String) {
    NOM("Nom (A → Z)"),
    PRIX_CROISSANT("Prix de gros croissant"),
    PRIX_DECROISSANT("Prix de gros décroissant"),
    CATEGORIE("Catégorie")
}
enum class PeriodeFournee(val libelle: String) {
    TOUT("Tout"),
    CE_MOIS("Ce Mois"),
    SEMAINE_DERNIERE("Semaine Dernière"),
    MOIS_DERNIER("Mois Dernier");

    fun accepte(d: LocalDate, auj: LocalDate): Boolean = when (this) {
        TOUT -> true
        CE_MOIS -> YearMonth.from(d) == YearMonth.from(auj)
        SEMAINE_DERNIERE -> {
            val debut = auj.with(DayOfWeek.MONDAY).minusWeeks(1)
            !d.isBefore(debut) && !d.isAfter(debut.plusDays(6))
        }
        MOIS_DERNIER -> YearMonth.from(d) == YearMonth.from(auj).minusMonths(1)
    }
}

data class SyntheseMois(
    val mois: String = "",
    val nbFournees: Int = 0,
    val unites: Int = 0,
    val moyenneParJour: Int = 0,
    val farineKg: Double = 0.0
)

private val fmtLong = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH)
private val fmtCourt = DateTimeFormatter.ofPattern("dd/MM/yyyy")

/** Tout ce qu'on peut chercher dans une fournée : produits, ingrédients, date */
private fun Fournee.texteRecherche(): String =
    (lignes.map { it.nom } + consommations.map { it.nom } + date.format(fmtLong) + date.format(fmtCourt))
        .joinToString(" ").lowercase()
data class FourneauxUiState(
    val isLoading: Boolean = true,
    val erreur: String? = null,
    val produits: List<Produit> = emptyList(),      // archivés inclus (pour le calcul du plan)
    val ingredients: List<Ingredient> = emptyList(),
    val commandes: List<Commande> = emptyList(),
    val fournees: List<Fournee> = emptyList(),
    val onglet: OngletFourneaux = OngletFourneaux.A_PREPARER,
    val rechercheProduit: String = "",
    val categorieFiltre: String? = null,
    val tri: TriProduit = TriProduit.NOM,
    val rechercheFournee: String = "",
    val periode: PeriodeFournee = PeriodeFournee.TOUT

) {
    val demain: LocalDate get() = LocalDate.now().plusDays(1)

    val produitsActifs: List<Produit> get() = produits.filterNot { it.archive }.sortedBy { it.nom }

    val categories: List<String>
        get() = produitsActifs.map { it.categorie.trim() }
            .filter { it.isNotEmpty() }.distinct().sortedBy { it.lowercase() }

    val produitsAffiches: List<Produit>
        get() {
            val q = rechercheProduit.trim().lowercase()
            val cat = categorieFiltre?.takeIf { it in categories }
            val filtres = produitsActifs.filter { p ->
                (cat == null || p.categorie.trim() == cat) &&
                        (q.isEmpty() ||
                                p.nom.lowercase().contains(q) ||
                                p.categorie.lowercase().contains(q) ||
                                p.recette.any { r ->
                                    ingredients.firstOrNull { it.id == r.ingredientId }
                                        ?.nom?.lowercase()?.contains(q) == true
                                })
            }
            return when (tri) {
                TriProduit.NOM -> filtres.sortedBy { it.nom.lowercase() }
                TriProduit.PRIX_CROISSANT -> filtres.sortedBy { it.prixGros }
                TriProduit.PRIX_DECROISSANT -> filtres.sortedByDescending { it.prixGros }
                TriProduit.CATEGORIE -> filtres.sortedWith(
                    compareBy({ it.categorie.isBlank() }, { it.categorie.lowercase() }, { it.nom.lowercase() })
                )
            }
        }
    val plan: List<LignePlan>
        get() = commandes
            .filter { it.date == demain }
            .flatMap { it.lignes }
            .groupBy { it.produitId }
            .map { (id, lignes) ->
                LignePlan(id, lignes.first().nom, lignes.sumOf { it.quantite }, produits.firstOrNull { it.id == id })
            }
            .sortedBy { it.nom }

    val produitsSansRecette: List<LignePlan>
        get() = plan.filter { it.produit == null || !it.produit.aRecette }

    /** Ingrédients nécessaires pour fabriquer ces quantités (proportionnel, sans arrondi au lot) */
    fun besoinsPour(quantites: Map<String, Int>): List<Besoin> {
        val requis = mutableMapOf<String, Double>()
        produits.forEach { p ->
            val q = quantites[p.id] ?: 0
            if (q > 0 && p.aRecette) {
                val lots = q.toDouble() / p.piecesParLot
                p.recette.forEach { r ->
                    requis[r.ingredientId] = (requis[r.ingredientId] ?: 0.0) + r.quantiteParLot * lots
                }
            }
        }
        return requis
            .mapNotNull { (id, r) -> ingredients.firstOrNull { it.id == id }?.let { Besoin(it, r) } }
            .sortedBy { it.ingredient.nom }
    }

    val besoinsPlan: List<Besoin> get() = besoinsPour(plan.associate { it.produitId to it.quantite })

    /** Une fournée annulée ne compte plus : on peut relancer la production */
    val fourneeDemain: Fournee? get() = fournees.firstOrNull { it.date == demain && !it.annulee }

    val historiqueAffiche: List<Fournee>
        get() {
            val auj = LocalDate.now()
            val q = rechercheFournee.trim().lowercase()
            return fournees
                .filter { periode.accepte(it.date, auj) }
                .filter { q.isEmpty() || it.texteRecherche().contains(q) }
                .sortedByDescending { it.heure }
        }

    /** Toujours calculée sur le mois en cours, quel que soit le filtre */
    val syntheseMois: SyntheseMois
        get() {
            val auj = LocalDate.now()
            val duMois = fournees.filter { !it.annulee && YearMonth.from(it.date) == YearMonth.from(auj) }
            val unites = duMois.sumOf { it.totalPieces }
            val jours = duMois.map { it.date }.distinct().size
            val farine = duMois.flatMap { it.consommations }
                .filter { it.nom.contains("farine", ignoreCase = true) }
                .sumOf {
                    when (it.unite) {
                        UniteStock.KG -> it.deduit
                        UniteStock.G -> it.deduit / 1000
                        else -> 0.0
                    }
                }
            return SyntheseMois(
                mois = auj.month.getDisplayName(TextStyle.FULL, Locale.FRENCH)
                    .replaceFirstChar { it.uppercase() },
                nbFournees = duMois.size,
                unites = unites,
                moyenneParJour = if (jours == 0) 0 else unites / jours,
                farineKg = farine
            )
        }
}

class FourneauxViewModel(
    // Demain : ApiFourneauxRepository(...) ici. Mets avecDemo = true pour tester avec des données.
    private val repository: FourneauxRepository = FakeFourneauxRepository()
) : ViewModel() {

    private val _etat = MutableStateFlow(FourneauxUiState())
    val etat: StateFlow<FourneauxUiState> = _etat.asStateFlow()

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages: Flow<String> = _messages.receiveAsFlow()

    init { charger() }

    private suspend fun rafraichir() {
        val produits = repository.getProduits()
        val ingredients = repository.getIngredients()
        val commandes = repository.getCommandes()
        val fournees = repository.getFournees()
        _etat.update {
            it.copy(produits = produits, ingredients = ingredients, commandes = commandes, fournees = fournees)
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
            } catch (e: Exception) {
                _etat.update { it.copy(isLoading = false, erreur = "Impossible de charger les données.") }
            }
        }
    }

    fun onOnglet(o: OngletFourneaux) = _etat.update { it.copy(onglet = o) }
    fun onRechercheProduit(t: String) = _etat.update { it.copy(rechercheProduit = t) }
    fun onCategorie(c: String?) = _etat.update { it.copy(categorieFiltre = c) }
    fun onTri(t: TriProduit) = _etat.update { it.copy(tri = t) }
    fun onRechercheFournee(t: String) = _etat.update { it.copy(rechercheFournee = t) }
    fun onPeriode(p: PeriodeFournee) = _etat.update { it.copy(periode = p) }
    private fun messageIngredients(nb: Int) =
        if (nb == 0) "" else " · $nb ingrédient${if (nb > 1) "s" else ""} ajouté${if (nb > 1) "s" else ""} au stock"
    fun creerProduit(p: NouveauProduit) = actionMsg {
        val nb = repository.creerProduit(p)
        "Produit « ${p.nom} » créé" + messageIngredients(nb)
    }
    fun modifierProduit(id: String, p: NouveauProduit) = actionMsg {
        val nb = repository.modifierProduit(id, p)
        "Produit mis à jour" + messageIngredients(nb)
    }
    fun supprimerProduit(p: Produit) = actionMsg {
        val archive = repository.supprimerProduit(p.id)
        if (archive) "« ${p.nom} » archivé (déjà commandé)" else "« ${p.nom} » supprimé"
    }

    fun lancer(lignes: List<LigneDemande>) {
        val manque = _etat.value.besoinsPour(lignes.associate { it.produitId to it.quantite })
            .any { !it.suffisant }
        val message =
            if (manque) "Production validée · stock insuffisant, ingrédients ramenés à 0"
            else "Production validée · ingrédients déduits du stock"
        action(message) { repository.lancerProduction(_etat.value.demain, lignes) }
    }

    fun annulerFournee(f: Fournee) =
        action("Fournée annulée · ingrédients remis en stock") { repository.annulerFournee(f.id) }

    private fun action(succes: String, bloc: suspend () -> Unit) = actionMsg { bloc(); succes }

    private fun actionMsg(bloc: suspend () -> String) {
        viewModelScope.launch {
            try {
                val message = bloc()
                rafraichir()
                _messages.send(message)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _messages.send("Échec de l'opération. Réessaie.")
            }
        }
    }
}