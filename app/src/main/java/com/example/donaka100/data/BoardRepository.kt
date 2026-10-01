package com.example.donaka100.data

import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter

interface BoardRepository {
    suspend fun getBoard(): BoardData
    suspend fun ajouterVente(vente: NouvelleVente)
}

/** Faux backend : tout à 0, ou les valeurs du maquette avec avecDemo = true */
class FakeBoardRepository(avecDemo: Boolean = false) : BoardRepository {

    private var data = if (avecDemo) donneesDemo() else BoardData()

    override suspend fun getBoard(): BoardData {
        delay(800)                       // simule le réseau (on voit le skeleton)
        return data
    }

    override suspend fun ajouterVente(vente: NouvelleVente) {
        delay(300)
        val heure = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        val operation = Operation(
            id = System.currentTimeMillis().toString(),
            titre = vente.libelle,
            heure = heure,
            detail = vente.detail,
            montant = if (vente.aCredit) 0 else vente.montant
        )
        data = if (vente.aCredit) {
            data.copy(
                nbCreances = data.nbCreances + 1,
                totalCreances = data.totalCreances + vente.montant,
                dernieresOperations = (listOf(operation) + data.dernieresOperations).take(5)
            )
        } else {
            data.copy(
                chiffreAffaires = data.chiffreAffaires + vente.montant,
                dernieresOperations = (listOf(operation) + data.dernieresOperations).take(5)
            )
        }
    }

    private fun donneesDemo() = BoardData(
        nomUtilisateur = "Chef Baker",
        fournilOuvert = true,
        chiffreAffaires = 450_000,
        achatsEtFrais = 130_000,
        variationVeille = 14,
        stocks = listOf(
            StockSurveille("1", "Farine T55", 12, 50),
            StockSurveille("2", "Beurre de Tourage", 18, 15),
            StockSurveille("3", "Sucre", 80, 20)          // OK : n'apparaît pas
        ),
        commandesDemain = listOf(
            LigneCommande("Gâteaux Chocolat 8p", 15),
            LigneCommande("Baguettes Tradition", 30),
            LigneCommande("Brioches Pur Beurre", 10)
        ),
        heureLivraison = "08h00",
        nbCreances = 3,
        totalCreances = 420_000,
        dernieresOperations = listOf(
            Operation("a", "Épicerie Toky", "10:45", "Espèces", 50_000),
            Operation("b", "Mme Rasoa (Farine)", "09:30", "Sac 50kg", -80_000)
        )
    )
}