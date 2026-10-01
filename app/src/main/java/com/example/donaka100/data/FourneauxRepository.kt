package com.example.donaka100.data

import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds

interface FourneauxRepository {
    suspend fun getProduits(): List<Produit>
    suspend fun getIngredients(): List<Ingredient>
    suspend fun getCommandes(): List<Commande>
    suspend fun getFournees(): List<Fournee>

    suspend fun creerProduit(p: NouveauProduit) : Int
    suspend fun modifierProduit(id: String, p: NouveauProduit) : Int
    /** Retourne true si le produit a été archivé (déjà commandé), false s'il a été supprimé */
    suspend fun supprimerProduit(id: String): Boolean

    suspend fun lancerProduction(date: LocalDate, lignes: List<LigneDemande>)
    suspend fun annulerFournee(id: String)
}

class FakeFourneauxRepository(avecDemo: Boolean = false) : FourneauxRepository {

    private val db = FakeBackend
    private class Resolution(val recette: List<LigneRecette>, val pivotId: String?, val nbCrees: Int)

    /** Retrouve l'ingrédient par son nom, ou le crée dans le stock à 0 */
    private fun trouverOuCreer(nom: String, unite: UniteStock, crees: MutableList<Ingredient>): Ingredient {
        val cle = nom.cleNom()
        db.ingredients.firstOrNull { it.nom.cleNom() == cle }?.let { return it }
        val nouveau = Ingredient(UUID.randomUUID().toString(), nom.nettoyerNom(), unite, 0.0, 0.0)
        db.ingredients += nouveau
        crees += nouveau
        return nouveau
    }

    private fun resoudre(p: NouveauProduit): Resolution {
        val crees = mutableListOf<Ingredient>()
        val recette = p.recette
            .map { s -> LigneRecette(trouverOuCreer(s.nom, s.unite, crees).id, s.quantiteParLot) }
            .groupBy { it.ingredientId }                       // un nom en double est additionné
            .map { (id, l) -> LigneRecette(id, l.sumOf { it.quantiteParLot }) }
        val pivotId = p.pivotNom?.let { n ->
            db.ingredients.firstOrNull { it.nom.cleNom() == n.cleNom() }?.id
        }
        return Resolution(recette, pivotId, crees.size)
    }
    init { if (avecDemo) db.chargerDemo() }

    override suspend fun getProduits(): List<Produit> { delay(500.milliseconds); return db.produits }
    override suspend fun getIngredients() = db.ingredients
    override suspend fun getCommandes() = db.commandes
    override suspend fun getFournees() = db.fournees

    override suspend fun creerProduit(p: NouveauProduit): Int {
        delay(300)
        val r = resoudre(p)
        db.produits += Produit(
                    id = UUID.randomUUID().toString(), nom = p.nom.nettoyerNom(),
                    prixGros = p.prixGros, prixPublic = p.prixPublic,
                    piecesParLot = p.piecesParLot, recette = r.recette,
                    categorie = p.categorie.nettoyerNom(), pivotId = r.pivotId
                )
        return r.nbCrees
    }

    override suspend fun modifierProduit(id: String, p: NouveauProduit): Int {
        delay(300.milliseconds)
        val r = resoudre(p)
        db.produits = db.produits.map {
            if (it.id == id) it.copy(
                nom = p.nom.nettoyerNom(), prixGros = p.prixGros, prixPublic = p.prixPublic,
                piecesParLot = p.piecesParLot, recette = r.recette,
                categorie = p.categorie.nettoyerNom(), pivotId = r.pivotId
            ) else it
        }
        return r.nbCrees
    }

    override suspend fun supprimerProduit(id: String): Boolean {
        delay(300.milliseconds)
        val utilise = db.commandes.any { c -> c.lignes.any { it.produitId == id } }
        db.produits =
            if (utilise) db.produits.map { if (it.id == id) it.copy(archive = true) else it }
            else db.produits.filterNot { it.id == id }
        return utilise
    }

    override suspend fun lancerProduction(date: LocalDate, lignes: List<LigneDemande>) {
        delay(400.milliseconds)
        check(db.fournees.none { it.date == date && !it.annulee }) { "Production déjà validée pour ce jour" }
        val demande = lignes.filter { it.quantite > 0 }
        require(demande.isNotEmpty())

        val requis = mutableMapOf<String, Double>()
        val lignesFournee = demande.mapNotNull { d ->
            val p = db.produits.firstOrNull { it.id == d.produitId } ?: return@mapNotNull null
            if (p.aRecette) {
                val lots = d.quantite.toDouble() / p.piecesParLot
                p.recette.forEach { r ->
                    requis[r.ingredientId] = (requis[r.ingredientId] ?: 0.0) + r.quantiteParLot * lots
                }
            }
            val prevu = db.commandes.filter { it.date == date }
                .flatMap { it.lignes }.filter { it.produitId == p.id }.sumOf { it.quantite }
            LigneFournee(p.id, p.nom, d.quantite, prevu)
        }

        val consommations = requis.mapNotNull { (id, besoin) ->
            val ing = db.ingredients.firstOrNull { it.id == id } ?: return@mapNotNull null
            Consommation(ing.id, ing.nom, ing.unite, besoin, minOf(besoin, ing.quantite))
        }

        // Le stock ne descend jamais sous 0
        db.ingredients = db.ingredients.map { ing ->
            val c = consommations.firstOrNull { it.ingredientId == ing.id }
            if (c == null) ing else ing.copy(quantite = (ing.quantite - c.deduit).coerceAtLeast(0.0))
        }
        db.fournees += Fournee(
                    UUID.randomUUID().toString(), date, LocalDateTime.now(), lignesFournee, consommations
                )
    }

    override suspend fun annulerFournee(id: String) {
        delay(300.milliseconds)
        val f = db.fournees.first { it.id == id }
        check(!f.annulee) { "Déjà annulée" }
        db.ingredients = db.ingredients.map { ing ->
            val c = f.consommations.firstOrNull { it.ingredientId == ing.id }
            if (c == null) ing else ing.copy(quantite = ing.quantite + c.deduit)
        }
        db.fournees = db.fournees.map {
            if (it.id == id) it.copy(annuleeA = LocalDateTime.now()) else it
        }
    }

}