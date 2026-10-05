package com.example.donaka100.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.donaka100.data.local.entity.ClientEntity
import com.example.donaka100.data.local.model.ClientWithCommandesAndPaiements
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao {

    @Query("SELECT * FROM clients WHERE archive = 0 ORDER BY nom ASC")
    fun getAllClients(): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients WHERE id = :id AND archive = 0")
    fun getClientById(id: String): Flow<ClientEntity?>

    @Query("SELECT * FROM clients WHERE id = :id AND archive = 0")
    suspend fun getClientByIdSync(id: String): ClientEntity?

    @Transaction
    @Query("SELECT * FROM clients WHERE archive = 0 ORDER BY nom ASC")
    fun getAllClientsWithDetails(): Flow<List<ClientWithCommandesAndPaiements>>

    @Transaction
    @Query("SELECT * FROM clients WHERE id = :id AND archive = 0")
    fun getClientWithDetailsById(id: String): Flow<ClientWithCommandesAndPaiements?>

    @Transaction
    @Query("SELECT * FROM clients WHERE id = :id AND archive = 0")
    suspend fun getClientWithDetailsByIdSync(id: String): ClientWithCommandesAndPaiements?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClient(client: ClientEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClients(clients: List<ClientEntity>)

    @Update
    suspend fun updateClient(client: ClientEntity)

    // Soft-delete STRICT (pas de DELETE physique)
    @Query("UPDATE clients SET archive = 1 WHERE id = :id")
    suspend fun softDeleteClient(id: String)
}
