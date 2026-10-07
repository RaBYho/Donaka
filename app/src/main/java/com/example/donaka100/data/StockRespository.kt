package com.example.donaka100.data

import androidx.room.withTransaction
import com.example.donaka100.DonakaApplication
import com.example.donaka100.data.local.DonakaDatabase
import com.example.donaka100.data.local.entity.AchatEntity
import com.example.donaka100.data.local.entity.FournisseurEntity
import com.example.donaka100.data.local.entity.IngredientEntity
import com.example.donaka100.data.local.entity.MouvementStockEntity
import com.example.donaka100.data.local.entity.PrixIngredientEntity
import com.example.donaka100.data.local.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import kotlin.math.abs
import kotlin.math.roundToLong

interface StockRepository {
    fun observeIngredients(): Flow<List<Ingredient>>
    fun observeProduits(): Flow<List<Produit>>
    fun observeFournees(): Flow<List<Fournee>>
    fun observeMouvements(): Flow<List<MouvementStock>>
    fun observeFiches(): Flow<List<FicheFournisseur>>

    suspend fun getIngredients(): List<Ingredient>
    suspend fun getProduits(): List<Produit>
    suspend fun getFournees(): List<Fournee>
    suspend fun getMouvements(): List<MouvementStock>

    suspend fun creerIngredient(i: NouvelIngredient)
    suspend fun modifierIngredient(id: String, i: NouvelIngredient)
    suspend fun supprimerIngredient(id: String)
    suspend fun ajuster(id: String, nouvelleQuantite: Double, motif: String)
    /** Retourne true si l'ingrédient a été créé par cet achat */
    suspend fun acheter(a: NouvelAchat): Boolean
    suspend fun modifierAchat(mouvementId: String, m: ModificationAchat)
    suspend fun annulerAchat(mouvementId: String)
    suspend fun getFiches(): List<FicheFournisseur>
    suspend fun creerFournisseur(f: NouveauFournisseur)
    /** Crée la fiche si le fournisseur n'en avait pas. Un changement de nom est propagé partout. */
    suspend fun modifierFournisseur(ancienNom: String, f: NouveauFournisseur)
    suspend fun supprimerFournisseur(nom: String)
}

class RoomStockRepository(
    private val database: DonakaDatabase = DonakaApplication.instance.database
) : StockRepository {

    private val db get() = database
    private val stockDao get() = db.stockDao()
    private val fournisseurDao get() = db.fournisseurDao()
    private val achatDao get() = db.achatDao()
    private val fourneeDao get() = db.fourneeDao()

    override fun observeIngredients(): Flow<List<Ingredient>> {
        return stockDao.getAllIngredients().map { list -> list.map { it.toDomain() } }
    }

    override fun observeProduits(): Flow<List<Produit>> {
        return stockDao.getAllProduitsWithRecette().map { list -> list.map { it.toDomain() } }
    }

    override fun observeFournees(): Flow<List<Fournee>> {
        return fourneeDao.getAllFourneesWithDetails().map { list -> list.map { it.toDomain() } }
    }

    override fun observeMouvements(): Flow<List<MouvementStock>> {
        return stockDao.getAllMouvementsStock().map { list -> list.map { it.toDomain() } }
    }

    override fun observeFiches(): Flow<List<FicheFournisseur>> {
        return fournisseurDao.getAllFournisseurs().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getIngredients(): List<Ingredient> {
        return observeIngredients().first()
    }

    override suspend fun getProduits(): List<Produit> {
        return observeProduits().first()
    }

    override suspend fun getFournees(): List<Fournee> {
        return observeFournees().first()
    }

    override suspend fun getMouvements(): List<MouvementStock> {
        return observeMouvements().first()
    }

    override suspend fun creerIngredient(i: NouvelIngredient) = db.withTransaction {
        val tous = stockDao.getAllIngredientsSync()
        require(tous.none { it.nom.cleNom() == i.nom.cleNom() }) { "Ingrédient déjà existant" }

        val id = UUID.randomUUID().toString()
        val nomClean = i.nom.nettoyerNom()
        val rayonClean = i.rayon.nettoyerNom()
        val fournisseurClean = i.fournisseur.nettoyerNom()

        val entity = IngredientEntity(
            id = id,
            nom = nomClean,
            unite = i.unite,
            quantite = i.quantite,
            seuil = i.seuil,
            rayon = rayonClean,
            fournisseur = fournisseurClean,
            archive = false
        )
        stockDao.insertIngredient(entity)

        if (i.quantite > 0) {
            val mouvement = MouvementStockEntity(
                id = UUID.randomUUID().toString(),
                ingredientId = id,
                ingredientNom = nomClean,
                dateHeure = System.currentTimeMillis(),
                type = TypeMouvementStock.ENTREE,
                quantite = i.quantite,
                motif = "Stock initial",
                montant = null,
                fournisseur = fournisseurClean,
                estAchat = false,
                mode = null,
                annule = false,
                unite = i.unite
            )
            stockDao.insertMouvementStock(mouvement)
        }
    }

    override suspend fun modifierIngredient(id: String, i: NouvelIngredient) = db.withTransaction {
        val ingEntity = stockDao.getIngredientByIdSync(id) ?: return@withTransaction
        val nomClean = i.nom.nettoyerNom()
        val tous = stockDao.getAllIngredientsSync()
        require(tous.none { it.id != id && it.nom.cleNom() == nomClean.cleNom() }) { "Nom déjà pris" }

        val updated = ingEntity.copy(
            nom = nomClean,
            unite = i.unite,
            quantite = i.quantite,
            seuil = i.seuil,
            rayon = i.rayon.nettoyerNom(),
            fournisseur = i.fournisseur.nettoyerNom()
        )
        stockDao.updateIngredient(updated)

        val ecart = i.quantite - ingEntity.quantite
        if (abs(ecart) > 0.0005) {
            val mouvement = MouvementStockEntity(
                id = UUID.randomUUID().toString(),
                ingredientId = id,
                ingredientNom = nomClean,
                dateHeure = System.currentTimeMillis(),
                type = TypeMouvementStock.AJUSTEMENT,
                quantite = ecart,
                motif = "Correction de la fiche",
                montant = null,
                fournisseur = i.fournisseur.nettoyerNom(),
                estAchat = false,
                mode = null,
                annule = false,
                unite = i.unite
            )
            stockDao.insertMouvementStock(mouvement)
        }
    }

    override suspend fun supprimerIngredient(id: String) = db.withTransaction {
        val produits = stockDao.getAllProduitsWithRecetteSync().map { it.toDomain() }
        val utilises = produits.filter { !it.archive && it.recette.any { r -> r.ingredientId == id } }
        if (utilises.isNotEmpty()) {
            val noms = utilises.joinToString(", ") { "« ${it.nom} »" }
            throw IllegalStateException("Impossible de supprimer cet ingrédient : il est utilisé dans la recette de $noms.")
        }
        stockDao.softDeleteIngredient(id)
    }

    override suspend fun ajuster(id: String, nouvelleQuantite: Double, motif: String) = db.withTransaction {
        val ingEntity = stockDao.getIngredientByIdSync(id) ?: return@withTransaction
        val ecart = nouvelleQuantite - ingEntity.quantite
        if (abs(ecart) < 0.0005) return@withTransaction

        stockDao.ajusterQuantiteIngredient(id, ecart)

        val mouvement = MouvementStockEntity(
            id = UUID.randomUUID().toString(),
            ingredientId = id,
            ingredientNom = ingEntity.nom,
            dateHeure = System.currentTimeMillis(),
            type = TypeMouvementStock.AJUSTEMENT,
            quantite = ecart,
            motif = motif.ifBlank { "Ajustement après comptage" },
            montant = null,
            fournisseur = ingEntity.fournisseur,
            estAchat = false,
            mode = null,
            annule = false,
            unite = ingEntity.unite
        )
        stockDao.insertMouvementStock(mouvement)
    }

    override suspend fun acheter(a: NouvelAchat): Boolean = db.withTransaction {
        val cle = a.nom.cleNom()
        val fournisseur = a.fournisseur.nettoyerNom()
        var cree = false

        val tous = stockDao.getAllIngredientsSync()
        var ingEntity = tous.firstOrNull { it.nom.cleNom() == cle }

        if (ingEntity == null) {
            val newId = UUID.randomUUID().toString()
            ingEntity = IngredientEntity(
                id = newId,
                nom = a.nom.nettoyerNom(),
                unite = a.unite,
                quantite = 0.0,
                seuil = 0.0,
                rayon = "",
                fournisseur = fournisseur,
                archive = false
            )
            stockDao.insertIngredient(ingEntity)
            cree = true
        }

        val id = ingEntity.id
        stockDao.ajusterQuantiteIngredient(id, a.quantite)

        if (fournisseur.isNotBlank()) {
            val updatedIng = stockDao.getIngredientByIdSync(id)
            if (updatedIng != null && updatedIng.fournisseur.isBlank()) {
                stockDao.updateIngredient(updatedIng.copy(fournisseur = fournisseur))
            }
        }

        val maintenant = System.currentTimeMillis()
        val mouvementId = UUID.randomUUID().toString()

        val mouvement = MouvementStockEntity(
            id = mouvementId,
            ingredientId = id,
            ingredientNom = ingEntity.nom,
            dateHeure = maintenant,
            type = TypeMouvementStock.ENTREE,
            quantite = a.quantite,
            motif = "Achat",
            montant = a.montant,
            fournisseur = fournisseur,
            estAchat = true,
            mode = a.mode,
            annule = false,
            unite = a.unite
        )
        stockDao.insertMouvementStock(mouvement)

        val achatEntity = AchatEntity(
            id = mouvementId,
            fournisseurId = null,
            fournisseurNom = fournisseur,
            ingredientId = id,
            ingredientNom = ingEntity.nom,
            dateHeure = maintenant,
            quantite = a.quantite,
            montant = a.montant,
            mode = a.mode,
            echeance = null,
            annule = false
        )
        achatDao.insertAchat(achatEntity)

        if (a.montant != null && a.montant > 0 && a.quantite > 0) {
            val prixParUnite = (a.montant.toDouble() / a.quantite).roundToLong()
            val prixEntity = PrixIngredientEntity(
                id = UUID.randomUUID().toString(),
                ingredientId = id,
                prixParUnite = prixParUnite,
                dateEnregistrement = maintenant,
                fournisseur = fournisseur
            )
            stockDao.insertPrixIngredient(prixEntity)
        }

        cree
    }

    override suspend fun modifierAchat(mouvementId: String, m: ModificationAchat) = db.withTransaction {
        val mouvement = stockDao.getMouvementStockByIdSync(mouvementId) ?: return@withTransaction
        if (!mouvement.estAchat || mouvement.annule) return@withTransaction

        val updatedMouvement = mouvement.copy(
            montant = m.montant,
            fournisseur = m.fournisseur.nettoyerNom(),
            mode = m.mode
        )
        stockDao.insertMouvementStock(updatedMouvement)

        val achat = achatDao.getAchatByIdSync(mouvementId)
        if (achat != null) {
            achatDao.updateAchat(
                achat.copy(
                    montant = m.montant,
                    fournisseurNom = m.fournisseur.nettoyerNom(),
                    mode = m.mode
                )
            )
        }
    }

    override suspend fun annulerAchat(mouvementId: String) = db.withTransaction {
        val mouvement = stockDao.getMouvementStockByIdSync(mouvementId) ?: return@withTransaction
        if (!mouvement.estAchat || mouvement.annule) return@withTransaction

        val ingEntity = stockDao.getIngredientByIdSync(mouvement.ingredientId) ?: return@withTransaction

        stockDao.ajusterQuantiteIngredient(ingEntity.id, -mouvement.quantite)
        stockDao.annulerAchatMouvement(mouvementId)
        achatDao.annulerAchat(mouvementId)

        val annulationMouvement = MouvementStockEntity(
            id = UUID.randomUUID().toString(),
            ingredientId = ingEntity.id,
            ingredientNom = ingEntity.nom,
            dateHeure = System.currentTimeMillis(),
            type = TypeMouvementStock.SORTIE,
            quantite = -mouvement.quantite,
            motif = "Annulation d'achat",
            montant = null,
            fournisseur = mouvement.fournisseur,
            estAchat = false,
            mode = null,
            annule = false,
            unite = ingEntity.unite
        )
        stockDao.insertMouvementStock(annulationMouvement)
    }

    override suspend fun getFiches(): List<FicheFournisseur> {
        return observeFiches().first()
    }

    override suspend fun creerFournisseur(f: NouveauFournisseur) = db.withTransaction {
        val tous = fournisseurDao.getAllFournisseursSync()
        require(tous.none { it.nom.cleNom() == f.nom.cleNom() }) { "Fiche déjà existante" }

        val entity = FournisseurEntity(
            id = UUID.randomUUID().toString(),
            nom = f.nom.nettoyerNom(),
            telephone = f.telephone,
            adresse = f.adresse.nettoyerNom(),
            delai = f.delai.nettoyerNom(),
            archive = false
        )
        fournisseurDao.insertFournisseur(entity)
    }

    override suspend fun modifierFournisseur(ancienNom: String, f: NouveauFournisseur) = db.withTransaction {
        val nouveauNom = f.nom.nettoyerNom()

        val existante = fournisseurDao.getFournisseurByNomSync(ancienNom)
        if (existante != null) {
            fournisseurDao.updateFournisseur(
                existante.copy(
                    nom = nouveauNom,
                    telephone = f.telephone,
                    adresse = f.adresse.nettoyerNom(),
                    delai = f.delai.nettoyerNom()
                )
            )
        } else {
            fournisseurDao.insertFournisseur(
                FournisseurEntity(
                    id = UUID.randomUUID().toString(),
                    nom = nouveauNom,
                    telephone = f.telephone,
                    adresse = f.adresse.nettoyerNom(),
                    delai = f.delai.nettoyerNom(),
                    archive = false
                )
            )
        }
    }

    override suspend fun supprimerFournisseur(nom: String) = db.withTransaction {
        val f = fournisseurDao.getFournisseurByNomSync(nom)
        if (f != null) {
            fournisseurDao.softDeleteFournisseur(f.id)
        }
    }
}
