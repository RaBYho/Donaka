package com.example.donaka100.data

import kotlinx.coroutines.delay
import java.time.LocalTime
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds

interface DepenseRepository {
    suspend fun getDepenses(): List<Depense>
    /** Les achats de stock : ils sont gérés dans Stock, ici on ne fait que les lire */
    suspend fun getAchats(): List<MouvementStock>

    suspend fun creer(d: NouvelleDepense)
    suspend fun modifier(id: String, d: NouvelleDepense)
    suspend fun supprimer(id: String)
}

class FakeDepenseRepository(avecDemo: Boolean = true) : DepenseRepository {

    private val db = FakeBackend

    init { if (avecDemo) db.chargerDemo() }

    override suspend fun getDepenses(): List<Depense> { delay(500.milliseconds); return db.depenses }
    override suspend fun getAchats() = db.mouvements.filter { it.estAchat }

    override suspend fun creer(d: NouvelleDepense) {
        delay(300.milliseconds)
        require(d.montant > 0)
        db.depenses += Depense(
                    UUID.randomUUID().toString(), d.categorie.nettoyerNom(), d.montant,
                    d.note.nettoyerNom(), d.date.atTime(LocalTime.now()), d.mode
                )
    }

    override suspend fun modifier(id: String, d: NouvelleDepense) {
        delay(300.milliseconds)
        db.depenses = db.depenses.map {
            if (it.id != id) it
            else it.copy(
                categorie = d.categorie.nettoyerNom(), montant = d.montant,
                note = d.note.nettoyerNom(), mode = d.mode,
                // même jour : on garde l'heure d'origine
                dateHeure = if (it.dateHeure.toLocalDate() == d.date) it.dateHeure
                else d.date.atTime(LocalTime.now())
            )
        }
    }

    override suspend fun supprimer(id: String) {
        delay(300.milliseconds)
        db.depenses = db.depenses.filterNot { it.id == id }
    }
}