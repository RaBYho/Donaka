package com.example.donaka100.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.donaka100.data.local.entity.IngredientEntity
import com.example.donaka100.data.local.entity.LigneRecetteEntity
import com.example.donaka100.data.local.entity.MouvementStockEntity
import com.example.donaka100.data.local.entity.PrixIngredientEntity
import com.example.donaka100.data.local.entity.ProduitEntity
import com.example.donaka100.data.local.model.ProduitWithRecette
import kotlinx.coroutines.flow.Flow

@Dao
interface StockDao {

    // --- Ingrédients ---
    @Query("SELECT * FROM ingredients WHERE archive = 0 ORDER BY nom ASC")
    fun getAllIngredients(): Flow<List<IngredientEntity>>

    @Query("SELECT * FROM ingredients WHERE archive = 0 ORDER BY nom ASC")
    suspend fun getAllIngredientsSync(): List<IngredientEntity>

    @Query("SELECT * FROM ingredients WHERE id = :id AND archive = 0")
    fun getIngredientById(id: String): Flow<IngredientEntity?>

    @Query("SELECT * FROM ingredients WHERE id = :id AND archive = 0")
    suspend fun getIngredientByIdSync(id: String): IngredientEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(ingredient: IngredientEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredients(ingredients: List<IngredientEntity>)

    @Update
    suspend fun updateIngredient(ingredient: IngredientEntity)

    // Soft-delete STRICT (pas de DELETE physique)
    @Query("UPDATE ingredients SET archive = 1 WHERE id = :id")
    suspend fun softDeleteIngredient(id: String)

    @Query("UPDATE ingredients SET quantite = quantite + :delta WHERE id = :id")
    suspend fun ajusterQuantiteIngredient(id: String, delta: Double)


    // --- Prix Ingrédients (Achats / Coût de revient) ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrixIngredient(prix: PrixIngredientEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrixIngredients(prixList: List<PrixIngredientEntity>)

    @Query("SELECT * FROM prix_ingredients WHERE ingredientId = :ingredientId ORDER BY dateEnregistrement DESC LIMIT 1")
    fun getDernierPrixIngredient(ingredientId: String): Flow<PrixIngredientEntity?>

    @Query("SELECT * FROM prix_ingredients WHERE ingredientId = :ingredientId ORDER BY dateEnregistrement DESC LIMIT 1")
    suspend fun getDernierPrixIngredientSync(ingredientId: String): PrixIngredientEntity?


    // --- Produits & Recettes ---
    @Transaction
    @Query("SELECT * FROM produits WHERE archive = 0 ORDER BY nom ASC")
    fun getAllProduitsWithRecette(): Flow<List<ProduitWithRecette>>

    @Transaction
    @Query("SELECT * FROM produits WHERE archive = 0 ORDER BY nom ASC")
    suspend fun getAllProduitsWithRecetteSync(): List<ProduitWithRecette>

    @Transaction
    @Query("SELECT * FROM produits WHERE id = :id AND archive = 0")
    fun getProduitWithRecetteById(id: String): Flow<ProduitWithRecette?>

    @Transaction
    @Query("SELECT * FROM produits WHERE id = :id AND archive = 0")
    suspend fun getProduitWithRecetteByIdSync(id: String): ProduitWithRecette?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduit(produit: ProduitEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduits(produits: List<ProduitEntity>)

    @Update
    suspend fun updateProduit(produit: ProduitEntity)

    // Soft-delete STRICT (pas de DELETE physique)
    @Query("UPDATE produits SET archive = 1 WHERE id = :id")
    suspend fun softDeleteProduit(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLignesRecette(lignes: List<LigneRecetteEntity>)

    @Query("DELETE FROM lignes_recette WHERE produitId = :produitId")
    suspend fun deleteRecetteForProduit(produitId: String)


    // --- Mouvements de Stock ---
    @Query("SELECT * FROM mouvements_stock ORDER BY dateHeure DESC")
    fun getAllMouvementsStock(): Flow<List<MouvementStockEntity>>

    @Query("SELECT * FROM mouvements_stock ORDER BY dateHeure DESC")
    suspend fun getAllMouvementsStockSync(): List<MouvementStockEntity>

    @Query("SELECT * FROM mouvements_stock WHERE id = :id")
    suspend fun getMouvementStockByIdSync(id: String): MouvementStockEntity?

    @Query("SELECT * FROM mouvements_stock WHERE ingredientId = :ingredientId ORDER BY dateHeure DESC")
    fun getMouvementsForIngredient(ingredientId: String): Flow<List<MouvementStockEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMouvementStock(mouvement: MouvementStockEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMouvementsStock(mouvements: List<MouvementStockEntity>)

    @Query("UPDATE mouvements_stock SET annule = 1 WHERE id = :id")
    suspend fun annulerAchatMouvement(id: String)
}
