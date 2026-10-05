package com.example.donaka100.data

import com.example.donaka100.DonakaApplication
import com.example.donaka100.data.local.entity.ConsommationEntity
import com.example.donaka100.data.local.entity.FourneeEntity
import com.example.donaka100.data.local.entity.IngredientEntity
import com.example.donaka100.data.local.entity.LigneFourneeEntity
import com.example.donaka100.data.local.entity.LigneRecetteEntity
import com.example.donaka100.data.local.entity.MouvementStockEntity
import com.example.donaka100.data.local.entity.ProduitEntity
import com.example.donaka100.data.local.toDomain
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.util.UUID

interface FourneauxRepository {
    suspend fun getProduits(): List<Produit>
    suspend fun getIngredients(): List<Ingredient>
    suspend fun getCommandes(): List<Commande>
    suspend fun getFournees(): List<Fournee>

    suspend fun creerProduit(p: NouveauProduit): Int
    suspend fun modifierProduit(id: String, p: NouveauProduit): Int
    /** Retourne true si le produit a été archivé (déjà commandé), false s'il a été supprimé */
    suspend fun supprimerProduit(id: String): Boolean

    suspend fun lancerProduction(date: LocalDate, lignes: List<LigneDemande>)
    suspend fun annulerFournee(id: String)
}

class RoomFourneauxRepository : FourneauxRepository {

    private val db get() = DonakaApplication.instance.database
    private val stockDao get() = db.stockDao()
    private val fourneeDao get() = db.fourneeDao()
    private val commandeDao get() = db.commandeDao()

    private class Resolution(val recette: List<LigneRecette>, val pivotId: String?, val nbCrees: Int)

    private suspend fun trouverOuCreer(nom: String, unite: UniteStock, crees: MutableList<Ingredient>): Ingredient {
        val cle = nom.cleNom()
        val ingredients = stockDao.getAllIngredients().first()
        ingredients.firstOrNull { it.nom.cleNom() == cle }?.let { return it.toDomain() }

        val id = UUID.randomUUID().toString()
        val newEntity = IngredientEntity(
            id = id,
            nom = nom.nettoyerNom(),
            unite = unite,
            quantite = 0.0,
            seuil = 0.0,
            rayon = "",
            fournisseur = "",
            archive = false
        )
        stockDao.insertIngredient(newEntity)
        val domain = newEntity.toDomain()
        crees += domain
        return domain
    }

    private suspend fun resoudre(p: NouveauProduit): Resolution {
        val crees = mutableListOf<Ingredient>()
        val recette = p.recette
            .map { s -> LigneRecette(trouverOuCreer(s.nom, s.unite, crees).id, s.quantiteParLot) }
            .groupBy { it.ingredientId }
            .map { (id, l) -> LigneRecette(id, l.sumOf { it.quantiteParLot }) }

        val ingredients = stockDao.getAllIngredients().first()
        val pivotId = p.pivotNom?.let { n ->
            ingredients.firstOrNull { it.nom.cleNom() == n.cleNom() }?.id
        }
        return Resolution(recette, pivotId, crees.size)
    }

    override suspend fun getProduits(): List<Produit> {
        return stockDao.getAllProduitsWithRecette().first().map { it.toDomain() }
    }

    override suspend fun getIngredients(): List<Ingredient> {
        return stockDao.getAllIngredients().first().map { it.toDomain() }
    }

    override suspend fun getCommandes(): List<Commande> {
        return commandeDao.getAllCommandesWithDetails().first().map { it.toDomain() }
    }

    override suspend fun getFournees(): List<Fournee> {
        return fourneeDao.getAllFourneesWithDetails().first().map { it.toDomain() }
    }

    override suspend fun creerProduit(p: NouveauProduit): Int {
        val r = resoudre(p)
        val produitId = UUID.randomUUID().toString()

        val entity = ProduitEntity(
            id = produitId,
            nom = p.nom.nettoyerNom(),
            prixGros = p.prixGros,
            prixPublic = p.prixPublic,
            piecesParLot = p.piecesParLot,
            categorie = p.categorie.nettoyerNom(),
            pivotIngredientId = r.pivotId,
            archive = false
        )
        stockDao.insertProduit(entity)

        val lignesRecetteEntities = r.recette.map {
            LigneRecetteEntity(
                produitId = produitId,
                ingredientId = it.ingredientId,
                quantiteParLot = it.quantiteParLot,
                estPivot = it.ingredientId == r.pivotId
            )
        }
        stockDao.insertLignesRecette(lignesRecetteEntities)

        return r.nbCrees
    }

    override suspend fun modifierProduit(id: String, p: NouveauProduit): Int {
        val r = resoudre(p)

        val entity = ProduitEntity(
            id = id,
            nom = p.nom.nettoyerNom(),
            prixGros = p.prixGros,
            prixPublic = p.prixPublic,
            piecesParLot = p.piecesParLot,
            categorie = p.categorie.nettoyerNom(),
            pivotIngredientId = r.pivotId,
            archive = false
        )
        stockDao.updateProduit(entity)

        stockDao.deleteRecetteForProduit(id)
        val lignesRecetteEntities = r.recette.map {
            LigneRecetteEntity(
                produitId = id,
                ingredientId = it.ingredientId,
                quantiteParLot = it.quantiteParLot,
                estPivot = it.ingredientId == r.pivotId
            )
        }
        stockDao.insertLignesRecette(lignesRecetteEntities)

        return r.nbCrees
    }

    override suspend fun supprimerProduit(id: String): Boolean {
        val commandes = getCommandes()
        val utilise = commandes.any { c -> c.lignes.any { it.produitId == id } }

        stockDao.softDeleteProduit(id)
        return utilise
    }

    override suspend fun lancerProduction(date: LocalDate, lignes: List<LigneDemande>) {
        val fourneesAujourdhui = fourneeDao.getFourneesForDate(date.toEpochDay()).first()
        check(fourneesAujourdhui.none { it.fournee.annuleeA == null }) { "Production déjà validée pour ce jour" }

        val demande = lignes.filter { it.quantite > 0 }
        require(demande.isNotEmpty())

        val produits = getProduits()
        val ingredients = getIngredients()
        val commandes = getCommandes()

        val requis = mutableMapOf<String, Double>()
        val lignesFournee = demande.mapNotNull { d ->
            val p = produits.firstOrNull { it.id == d.produitId } ?: return@mapNotNull null
            if (p.aRecette) {
                val lots = d.quantite.toDouble() / p.piecesParLot
                p.recette.forEach { r ->
                    requis[r.ingredientId] = (requis[r.ingredientId] ?: 0.0) + r.quantiteParLot * lots
                }
            }
            val prevu = commandes.filter { it.date == date }
                .flatMap { it.lignes }.filter { it.produitId == p.id }.sumOf { it.quantite }
            LigneFournee(p.id, p.nom, d.quantite, prevu)
        }

        val consommations = requis.mapNotNull { (id, besoin) ->
            val ing = ingredients.firstOrNull { it.id == id } ?: return@mapNotNull null
            Consommation(ing.id, ing.nom, ing.unite, besoin, minOf(besoin, ing.quantite))
        }

        val fourneeId = UUID.randomUUID().toString()
        val maintenant = System.currentTimeMillis()

        val fourneeEntity = FourneeEntity(
            id = fourneeId,
            date = date.toEpochDay(),
            heure = maintenant,
            annuleeA = null
        )
        fourneeDao.insertFournee(fourneeEntity)

        val lignesEntities = lignesFournee.map {
            LigneFourneeEntity(
                id = UUID.randomUUID().toString(),
                fourneeId = fourneeId,
                produitId = it.produitId,
                nomProduit = it.nom,
                quantite = it.quantite,
                quantitePlanifiee = it.quantitePlanifiee
            )
        }
        fourneeDao.insertLignesFournee(lignesEntities)

        val consommationsEntities = consommations.map {
            ConsommationEntity(
                id = UUID.randomUUID().toString(),
                fourneeId = fourneeId,
                ingredientId = it.ingredientId,
                nomIngredient = it.nom,
                unite = it.unite,
                requis = it.requis,
                deduit = it.deduit
            )
        }
        fourneeDao.insertConsommations(consommationsEntities)

        for (c in consommations) {
            if (c.deduit > 0) {
                stockDao.ajusterQuantiteIngredient(c.ingredientId, -c.deduit)
                val mouvement = MouvementStockEntity(
                    id = UUID.randomUUID().toString(),
                    ingredientId = c.ingredientId,
                    ingredientNom = c.nom,
                    dateHeure = maintenant,
                    type = TypeMouvementStock.SORTIE,
                    quantite = -c.deduit,
                    motif = "Production",
                    montant = null,
                    fournisseur = "",
                    estAchat = false,
                    mode = null,
                    annule = false,
                    unite = c.unite
                )
                stockDao.insertMouvementStock(mouvement)
            }
        }
    }

    override suspend fun annulerFournee(id: String) {
        val details = fourneeDao.getFourneeWithDetailsById(id).first() ?: return
        if (details.fournee.annuleeA != null) return

        val maintenant = System.currentTimeMillis()
        fourneeDao.annulerFournee(id, maintenant)

        for (c in details.consommations) {
            if (c.deduit > 0) {
                stockDao.ajusterQuantiteIngredient(c.ingredientId, c.deduit)
                val mouvement = MouvementStockEntity(
                    id = UUID.randomUUID().toString(),
                    ingredientId = c.ingredientId,
                    ingredientNom = c.nomIngredient,
                    dateHeure = maintenant,
                    type = TypeMouvementStock.ENTREE,
                    quantite = c.deduit,
                    motif = "Annulation de fournée",
                    montant = null,
                    fournisseur = "",
                    estAchat = false,
                    mode = null,
                    annule = false,
                    unite = c.unite
                )
                stockDao.insertMouvementStock(mouvement)
            }
        }
    }
}
