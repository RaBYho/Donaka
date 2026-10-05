package com.example.donaka100.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.donaka100.data.local.entity.DepenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DepenseDao {

    @Query("SELECT * FROM depenses WHERE annule = 0 ORDER BY dateHeure DESC")
    fun getAllDepenses(): Flow<List<DepenseEntity>>

    @Query("SELECT * FROM depenses WHERE id = :id AND annule = 0")
    suspend fun getDepenseByIdSync(id: String): DepenseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDepense(depense: DepenseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDepenses(depenses: List<DepenseEntity>)

    @Update
    suspend fun updateDepense(depense: DepenseEntity)

    // Soft-delete STRICT (pas de DELETE physique)
    @Query("UPDATE depenses SET annule = 1 WHERE id = :id")
    suspend fun softDeleteDepense(id: String)
}
