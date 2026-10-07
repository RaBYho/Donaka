package com.example.donaka100

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.donaka100.data.LigneDemande
import com.example.donaka100.data.LigneRecetteSaisie
import com.example.donaka100.data.NouveauProduit
import com.example.donaka100.data.NouvelIngredient
import com.example.donaka100.data.RoomFourneauxRepository
import com.example.donaka100.data.RoomStockRepository
import com.example.donaka100.data.UniteStock
import com.example.donaka100.data.local.DonakaDatabase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class RoomFourneauxRepositoryTest {

    private lateinit var database: DonakaDatabase
    private lateinit var fourneauxRepository: RoomFourneauxRepository
    private lateinit var stockRepository: RoomStockRepository

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, DonakaDatabase::class.java).build()
        fourneauxRepository = RoomFourneauxRepository(database)
        stockRepository = RoomStockRepository(database)
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun testProductionEtAnnulationStockRestitution() = runBlocking {
        stockRepository.creerIngredient(
            NouvelIngredient(
                nom = "Farine T55",
                unite = UniteStock.KG,
                quantite = 1.0,
                seuil = 0.2,
                rayon = "Poudres",
                fournisseur = "Grossiste"
            )
        )

        val ingredientsInitial = stockRepository.getIngredients()
        val farine = ingredientsInitial.first { it.nom == "Farine T55" }
        assertEquals(1.0, farine.quantite, 0.001)

        fourneauxRepository.creerProduit(
            NouveauProduit(
                nom = "Baguette",
                prixGros = 1000,
                prixPublic = 1200,
                piecesParLot = 1,
                recette = listOf(
                    LigneRecetteSaisie(
                        nom = "Farine T55",
                        unite = UniteStock.KG,
                        quantiteParLot = 0.25
                    )
                ),
                categorie = "Boulangerie"
            )
        )

        val produits = fourneauxRepository.getProduits()
        val baguette = produits.first { it.nom == "Baguette" }

        val aujourdhui = LocalDate.now()
        fourneauxRepository.lancerProduction(
            date = aujourdhui,
            lignes = listOf(LigneDemande(produitId = baguette.id, quantite = 1))
        )

        val ingredientsApresProd = stockRepository.getIngredients()
        val farineApresProd = ingredientsApresProd.first { it.id == farine.id }
        assertEquals(0.75, farineApresProd.quantite, 0.001)

        fourneauxRepository.annulerFournee(fourneauxRepository.getFournees().first { it.date == aujourdhui }.id)

        val ingredientsApresAnnulation = stockRepository.getIngredients()
        val farineApresAnnulation = ingredientsApresAnnulation.first { it.id == farine.id }
        assertEquals(1.0, farineApresAnnulation.quantite, 0.001)
    }
}
