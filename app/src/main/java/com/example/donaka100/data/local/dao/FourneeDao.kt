package com.example.donaka100.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.donaka100.data.local.entity.ConsommationEntity
import com.example.donaka100.data.local.entity.FourneeEntity
import com.example.donaka100.data.local.entity.LigneFourneeEntity
import com.example.donaka100.data.local.model.FourneeWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface FourneeDao {

    @Transaction
    @Query("SELECT * FROM fournees ORDER BY heure DESC")
    fun getAllFourneesWithDetails(): Flow<List<FourneeWithDetails>>

    @Transaction
    @Query("SELECT * FROM fournees WHERE date = :date ORDER BY heure DESC")
    fun getFourneesForDate(date: Long): Flow<List<FourneeWithDetails>>

    @Transaction
    @Query("SELECT * FROM fournees WHERE date = :date ORDER BY heure DESC")
    suspend fun getFourneesForDateSync(date: Long): List<FourneeWithDetails>

    @Transaction
    @Query("SELECT * FROM fournees WHERE id = :id")
    fun getFourneeWithDetailsById(id: String): Flow<FourneeWithDetails?>

    @Transaction
    @Query("SELECT * FROM fournees WHERE id = :id")
    suspend fun getFourneeWithDetailsByIdSync(id: String): FourneeWithDetails?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFournee(fournee: FourneeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFournees(fournees: List<FourneeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLignesFournee(lignes: List<LigneFourneeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConsommations(consommations: List<ConsommationEntity>)

    // Soft-delete / Annulation (mise à jour horodatage annuleeA)
    @Query("UPDATE fournees SET annuleeA = :annuleeA WHERE id = :id")
    suspend fun annulerFournee(id: String, annuleeA: Long)
}
