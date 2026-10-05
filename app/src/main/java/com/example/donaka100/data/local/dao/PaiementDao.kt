package com.example.donaka100.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.donaka100.data.local.entity.PaiementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaiementDao {

    @Query("SELECT * FROM paiements WHERE annule = 0 ORDER BY dateHeure DESC")
    fun getAllPaiements(): Flow<List<PaiementEntity>>

    @Query("SELECT * FROM paiements WHERE clientId = :clientId AND annule = 0 ORDER BY dateHeure DESC")
    fun getPaiementsForClient(clientId: String): Flow<List<PaiementEntity>>

    @Query("SELECT * FROM paiements WHERE commandeId = :commandeId AND annule = 0 ORDER BY dateHeure DESC")
    fun getPaiementsForCommande(commandeId: String): Flow<List<PaiementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaiement(paiement: PaiementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaiements(paiements: List<PaiementEntity>)

    // Soft-delete STRICT (pas de DELETE physique)
    @Query("UPDATE paiements SET annule = 1 WHERE id = :id")
    suspend fun annulerPaiement(id: String)
}
