package com.example.donaka100.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.donaka100.data.local.entity.FournisseurEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FournisseurDao {

    @Query("SELECT * FROM fournisseurs WHERE archive = 0 ORDER BY nom ASC")
    fun getAllFournisseurs(): Flow<List<FournisseurEntity>>

    @Query("SELECT * FROM fournisseurs WHERE id = :id AND archive = 0")
    fun getFournisseurById(id: String): Flow<FournisseurEntity?>

    @Query("SELECT * FROM fournisseurs WHERE id = :id AND archive = 0")
    suspend fun getFournisseurByIdSync(id: String): FournisseurEntity?

    @Query("SELECT * FROM fournisseurs WHERE LOWER(nom) = LOWER(:nom) AND archive = 0 LIMIT 1")
    suspend fun getFournisseurByNomSync(nom: String): FournisseurEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFournisseur(fournisseur: FournisseurEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFournisseurs(fournisseurs: List<FournisseurEntity>)

    @Update
    suspend fun updateFournisseur(fournisseur: FournisseurEntity)

    // Soft-delete STRICT (pas de DELETE physique)
    @Query("UPDATE fournisseurs SET archive = 1 WHERE id = :id")
    suspend fun softDeleteFournisseur(id: String)
}
