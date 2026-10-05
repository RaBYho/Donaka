package com.example.donaka100.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.donaka100.data.local.entity.AchatEntity
import com.example.donaka100.data.local.entity.PaiementFournisseurEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AchatDao {

    @Query("SELECT * FROM achats WHERE annule = 0 ORDER BY dateHeure DESC")
    fun getAllAchats(): Flow<List<AchatEntity>>

    @Query("SELECT * FROM achats WHERE ingredientId = :ingredientId AND annule = 0 ORDER BY dateHeure DESC")
    fun getAchatsForIngredient(ingredientId: String): Flow<List<AchatEntity>>

    @Query("SELECT * FROM achats WHERE id = :id AND annule = 0")
    suspend fun getAchatByIdSync(id: String): AchatEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchat(achat: AchatEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchats(achats: List<AchatEntity>)

    @Update
    suspend fun updateAchat(achat: AchatEntity)

    @Query("UPDATE achats SET annule = 1 WHERE id = :id")
    suspend fun annulerAchat(id: String)


    // --- Paiements Fournisseurs ---
    @Query("SELECT * FROM paiements_fournisseur WHERE annule = 0 ORDER BY dateHeure DESC")
    fun getAllPaiementsFournisseur(): Flow<List<PaiementFournisseurEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaiementFournisseur(paiement: PaiementFournisseurEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaiementsFournisseur(paiements: List<PaiementFournisseurEntity>)

    @Query("UPDATE paiements_fournisseur SET annule = 1 WHERE id = :id")
    suspend fun annulerPaiementFournisseur(id: String)
}
