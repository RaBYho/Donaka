package com.example.donaka100

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.donaka100.data.LigneDemande
import com.example.donaka100.data.NouveauClient
import com.example.donaka100.data.NouvelleCommande
import com.example.donaka100.data.NouveauProduit
import com.example.donaka100.data.RoomCommandeRepository
import com.example.donaka100.data.RoomFourneauxRepository
import com.example.donaka100.data.local.DonakaDatabase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class RoomCommandeRepositoryTest {

    private lateinit var database: DonakaDatabase
    private lateinit var commandeRepository: RoomCommandeRepository
    private lateinit var fourneauxRepository: RoomFourneauxRepository

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, DonakaDatabase::class.java).build()
        commandeRepository = RoomCommandeRepository(database)
        fourneauxRepository = RoomFourneauxRepository(database)
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun testModificationCommandeRemplacementLignesEtPrixInchange() = runBlocking {
        // 1. Créer un client
        commandeRepository.creerClient(
            NouveauClient(nom = "Client Gâteau", telephone = "0340000000", quartier = "Centre", plafondCreance = 0)
        )
        val client = commandeRepository.getClients().first { it.nom == "Client Gâteau" }

        // 2. Créer un produit
        fourneauxRepository.creerProduit(
            NouveauProduit(
                nom = "Gâteau Chocolat",
                prixGros = 2000,
                prixPublic = 2500,
                piecesParLot = 1,
                recette = emptyList(),
                categorie = "Pâtisserie"
            )
        )
        val produit = fourneauxRepository.getProduits().first { it.nom == "Gâteau Chocolat" }

        // 3. Créer une commande de 10 gâteaux
        val demain = LocalDate.now().plusDays(1)
        commandeRepository.creerCommande(
            NouvelleCommande(
                clientId = client.id,
                date = demain,
                heure = "08:00",
                lignes = listOf(LigneDemande(produitId = produit.id, quantite = 10))
            )
        )

        val commandesInitiales = commandeRepository.getCommandes()
        val commande = commandesInitiales.first { it.clientId == client.id }
        assertEquals(1, commande.lignes.size)
        assertEquals(10, commande.lignes.first().quantite)
        assertEquals(2000L, commande.lignes.first().prixUnitaire)
        assertEquals(20000L, commande.total)

        // 4. Modifier la commande : 10 -> 15 gâteaux
        commandeRepository.modifierCommande(
            id = commande.id,
            c = NouvelleCommande(
                clientId = client.id,
                date = demain,
                heure = "09:00",
                lignes = listOf(LigneDemande(produitId = produit.id, quantite = 15))
            )
        )

        // 5. Relire la commande
        val commandesApresModif = commandeRepository.getCommandes()
        val commandeModifiee = commandesApresModif.first { it.id == commande.id }

        // Vérification : 1 seule ligne, 15 gâteaux (et PAS 25 !), prix unitaire à 2000, total = 30 000
        assertEquals(1, commandeModifiee.lignes.size)
        assertEquals(15, commandeModifiee.lignes.first().quantite)
        assertEquals(2000L, commandeModifiee.lignes.first().prixUnitaire)
        assertEquals(30000L, commandeModifiee.total)

        // 6. Test quantité 0 : la ligne doit disparaître
        commandeRepository.modifierCommande(
            id = commande.id,
            c = NouvelleCommande(
                clientId = client.id,
                date = demain,
                heure = "09:00",
                lignes = listOf(LigneDemande(produitId = produit.id, quantite = 0))
            )
        )
        val commandeApresZero = commandeRepository.getCommandes().first { it.id == commande.id }
        assertEquals(0, commandeApresZero.lignes.size)
        assertEquals(0L, commandeApresZero.total)
    }
}
