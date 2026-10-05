package com.example.donaka100.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.donaka100.data.local.entity.CommandeEntity
import com.example.donaka100.data.local.entity.LigneCommandeEntity
import com.example.donaka100.data.local.model.CommandeWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface CommandeDao {

    @Transaction
    @Query("SELECT * FROM commandes WHERE archive = 0 ORDER BY date DESC, heureSouhaitee ASC")
    fun getAllCommandesWithDetails(): Flow<List<CommandeWithDetails>>

    @Transaction
    @Query("SELECT * FROM commandes WHERE date = :date AND archive = 0 ORDER BY heureSouhaitee ASC")
    fun getCommandesForDate(date: Long): Flow<List<CommandeWithDetails>>

    @Transaction
    @Query("SELECT * FROM commandes WHERE clientId = :clientId AND archive = 0 ORDER BY date DESC")
    fun getCommandesForClient(clientId: String): Flow<List<CommandeWithDetails>>

    @Transaction
    @Query("SELECT * FROM commandes WHERE clientId = :clientId AND livreeA IS NOT NULL AND archive = 0 ORDER BY date ASC, id ASC")
    suspend fun getCommandesLivreesNonPayeesSync(clientId: String): List<CommandeWithDetails>

    @Transaction
    @Query("SELECT * FROM commandes WHERE id = :id AND archive = 0")
    fun getCommandeWithDetailsById(id: String): Flow<CommandeWithDetails?>

    @Transaction
    @Query("SELECT * FROM commandes WHERE id = :id AND archive = 0")
    suspend fun getCommandeWithDetailsByIdSync(id: String): CommandeWithDetails?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommande(commande: CommandeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommandes(commandes: List<CommandeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLignesCommande(lignes: List<LigneCommandeEntity>)

    @Update
    suspend fun updateCommande(commande: CommandeEntity)

    // Soft-delete STRICT (pas de DELETE physique)
    @Query("UPDATE commandes SET archive = 1 WHERE id = :id")
    suspend fun softDeleteCommande(id: String)

    @Query("UPDATE commandes SET livreeA = :livreeA WHERE id = :id")
    suspend fun marquerLivree(id: String, livreeA: Long)
}
