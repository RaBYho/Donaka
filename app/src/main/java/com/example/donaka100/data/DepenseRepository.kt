package com.example.donaka100.data

import androidx.room.withTransaction
import com.example.donaka100.DonakaApplication
import com.example.donaka100.data.local.DonakaDatabase
import com.example.donaka100.data.local.entity.DepenseEntity
import com.example.donaka100.data.local.toDomain
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalTime
import java.time.ZoneId
import java.util.UUID

interface DepenseRepository {
    fun observeDepenses(): Flow<List<Depense>>
    fun observeAchats(): Flow<List<MouvementStock>>

    suspend fun getDepenses(): List<Depense>
    /** Les achats de stock : ils sont gérés dans Stock, ici on ne fait que les lire */
    suspend fun getAchats(): List<MouvementStock>

    suspend fun creer(d: NouvelleDepense)
    suspend fun modifier(id: String, d: NouvelleDepense)
    suspend fun supprimer(id: String)
}

class RoomDepenseRepository(
    private val database: DonakaDatabase = DonakaApplication.instance.database
) : DepenseRepository {

    private val db get() = database
    private val depenseDao get() = db.depenseDao()
    private val stockDao get() = db.stockDao()

    override fun observeDepenses(): Flow<List<Depense>> {
        return depenseDao.getAllDepenses().map { list -> list.map { it.toDomain() } }
    }

    override fun observeAchats(): Flow<List<MouvementStock>> {
        return stockDao.getAllMouvementsStock().map { list -> list.map { it.toDomain() }.filter { it.estAchat } }
    }

    override suspend fun getDepenses(): List<Depense> {
        return depenseDao.getAllDepenses().first().map { it.toDomain() }
    }

    override suspend fun getAchats(): List<MouvementStock> {
        return stockDao.getAllMouvementsStock().first().map { it.toDomain() }.filter { it.estAchat }
    }

    override suspend fun creer(d: NouvelleDepense) = db.withTransaction {
        require(d.montant > 0)
        val entity = DepenseEntity(
            id = UUID.randomUUID().toString(),
            categorie = d.categorie.nettoyerNom(),
            montant = d.montant,
            note = d.note.nettoyerNom(),
            dateHeure = d.date.atTime(LocalTime.now()).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            mode = d.mode,
            annule = false
        )
        depenseDao.insertDepense(entity)
    }

    override suspend fun modifier(id: String, d: NouvelleDepense) = db.withTransaction {
        val existante = depenseDao.getDepenseByIdSync(id) ?: return@withTransaction
        val updated = existante.copy(
            categorie = d.categorie.nettoyerNom(),
            montant = d.montant,
            note = d.note.nettoyerNom(),
            dateHeure = d.date.atTime(LocalTime.now()).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            mode = d.mode
        )
        depenseDao.updateDepense(updated)
    }

    override suspend fun supprimer(id: String) = db.withTransaction {
        depenseDao.softDeleteDepense(id)
    }
}
