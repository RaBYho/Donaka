package com.example.donaka100.data

import com.example.donaka100.DonakaApplication
import com.example.donaka100.data.local.entity.DepenseEntity
import com.example.donaka100.data.local.toDomain
import kotlinx.coroutines.flow.first
import java.time.LocalTime
import java.time.ZoneOffset
import java.util.UUID

interface DepenseRepository {
    suspend fun getDepenses(): List<Depense>
    /** Les achats de stock : ils sont gérés dans Stock, ici on ne fait que les lire */
    suspend fun getAchats(): List<MouvementStock>

    suspend fun creer(d: NouvelleDepense)
    suspend fun modifier(id: String, d: NouvelleDepense)
    suspend fun supprimer(id: String)
}

class RoomDepenseRepository : DepenseRepository {

    private val db get() = DonakaApplication.instance.database
    private val depenseDao get() = db.depenseDao()
    private val stockDao get() = db.stockDao()

    override suspend fun getDepenses(): List<Depense> {
        return depenseDao.getAllDepenses().first().map { it.toDomain() }
    }

    override suspend fun getAchats(): List<MouvementStock> {
        return stockDao.getAllMouvementsStock().first().map { it.toDomain() }.filter { it.estAchat }
    }

    override suspend fun creer(d: NouvelleDepense) {
        require(d.montant > 0)
        val entity = DepenseEntity(
            id = UUID.randomUUID().toString(),
            categorie = d.categorie.nettoyerNom(),
            montant = d.montant,
            note = d.note.nettoyerNom(),
            dateHeure = d.date.atTime(LocalTime.now()).toInstant(ZoneOffset.UTC).toEpochMilli(),
            mode = d.mode,
            annule = false
        )
        depenseDao.insertDepense(entity)
    }

    override suspend fun modifier(id: String, d: NouvelleDepense) {
        val existante = depenseDao.getDepenseByIdSync(id) ?: return
        val updated = existante.copy(
            categorie = d.categorie.nettoyerNom(),
            montant = d.montant,
            note = d.note.nettoyerNom(),
            dateHeure = d.date.atTime(LocalTime.now()).toInstant(ZoneOffset.UTC).toEpochMilli(),
            mode = d.mode
        )
        depenseDao.updateDepense(updated)
    }

    override suspend fun supprimer(id: String) {
        depenseDao.softDeleteDepense(id)
    }
}
