package com.example.donaka100.data

import com.example.donaka100.DonakaApplication
import com.example.donaka100.data.local.FifoImputationHelper
import com.example.donaka100.data.local.entity.ClientEntity
import com.example.donaka100.data.local.entity.CommandeEntity
import com.example.donaka100.data.local.entity.LigneCommandeEntity
import com.example.donaka100.data.local.entity.PaiementEntity
import com.example.donaka100.data.local.toDomain
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.util.UUID

interface CommandeRepository {
    suspend fun getClients(): List<Client>
    suspend fun getEncaissements(): List<Encaissement>
    suspend fun getCommandes(): List<Commande>
    suspend fun getProduits(): List<Produit>

    suspend fun creerClient(client: NouveauClient)
    suspend fun modifierClient(id: String, client: NouveauClient)
    suspend fun supprimerClient(id: String)
    suspend fun encaisser(clientId: String, montant: Long, mode: ModeReglement)

    suspend fun creerCommande(c: NouvelleCommande)
    suspend fun modifierCommande(id: String, c: NouvelleCommande)
    suspend fun supprimerCommande(id: String)
    suspend fun livrer(commandeId: String, montantRecu: Long, mode: ModeReglement?)
    suspend fun annulerLivraison(commandeId: String)
    suspend fun venteNonPrevue(lignes: List<LigneDemande>, mode: ModeReglement)
    suspend fun rattacherClient(commandeId: String, client: NouveauClient)
}

class RoomCommandeRepository : CommandeRepository {

    private val db get() = DonakaApplication.instance.database
    private val clientDao get() = db.clientDao()
    private val commandeDao get() = db.commandeDao()
    private val paiementDao get() = db.paiementDao()
    private val stockDao get() = db.stockDao()

    private var compteur = 0

    private fun numero(prefixe: String): String {
        compteur++
        return "$prefixe-%04d".format(compteur)
    }

    override suspend fun getClients(): List<Client> {
        return clientDao.getAllClientsWithDetails().first().map { it.toDomain() }
    }

    override suspend fun getEncaissements(): List<Encaissement> {
        return paiementDao.getAllPaiements().first().map { it.toDomain() }
    }

    override suspend fun getCommandes(): List<Commande> {
        return commandeDao.getAllCommandesWithDetails().first().map { it.toDomain() }
    }

    override suspend fun getProduits(): List<Produit> {
        return stockDao.getAllProduitsWithRecette().first().map { it.toDomain() }.filterNot { it.archive }
    }

    override suspend fun creerClient(client: NouveauClient) {
        val entity = ClientEntity(
            id = UUID.randomUUID().toString(),
            nom = client.nom.nettoyerNom(),
            telephone = client.telephone,
            quartier = client.quartier.nettoyerNom(),
            plafondCreance = client.plafondCreance,
            type = TypeClient.AUTRE,
            archive = false
        )
        clientDao.insertClient(entity)
    }

    override suspend fun modifierClient(id: String, client: NouveauClient) {
        val existant = clientDao.getClientByIdSync(id) ?: return
        val updated = existant.copy(
            nom = client.nom.nettoyerNom(),
            telephone = client.telephone,
            quartier = client.quartier.nettoyerNom(),
            plafondCreance = client.plafondCreance
        )
        clientDao.updateClient(updated)
    }

    override suspend fun supprimerClient(id: String) {
        clientDao.softDeleteClient(id)
    }

    override suspend fun encaisser(clientId: String, montant: Long, mode: ModeReglement) {
        val clientWithDetails = clientDao.getClientWithDetailsByIdSync(clientId) ?: return
        val commandesLivrees = commandeDao.getCommandesLivreesNonPayeesSync(clientId)

        val paiementsAInserer = FifoImputationHelper.imputerPaiementFIFO(
            clientId = clientId,
            clientNom = clientWithDetails.client.nom,
            montantTotal = montant,
            mode = mode,
            note = "Règlement de créance",
            dateHeure = System.currentTimeMillis(),
            commandesLivrees = commandesLivrees
        )

        paiementDao.insertPaiements(paiementsAInserer)
    }

    override suspend fun creerCommande(c: NouvelleCommande) {
        val client = clientDao.getClientByIdSync(c.clientId) ?: return
        val produits = getProduits()

        val commandeId = UUID.randomUUID().toString()
        val epochDate = c.date.toEpochDay()
        val epochEcheance = c.date.plusDays(7).toEpochDay()

        val entity = CommandeEntity(
            id = commandeId,
            clientId = client.id,
            clientNom = client.nom,
            nonPrevu = false,
            date = epochDate,
            heureSouhaitee = c.heure,
            livreeA = null,
            echeance = epochEcheance,
            archive = false
        )
        commandeDao.insertCommande(entity)

        val lignesEntities = c.lignes.filter { it.quantite > 0 }.mapNotNull { d ->
            val p = produits.firstOrNull { it.id == d.produitId } ?: return@mapNotNull null
            LigneCommandeEntity(
                id = UUID.randomUUID().toString(),
                commandeId = commandeId,
                produitId = p.id,
                nomProduit = p.nom,
                quantite = d.quantite,
                prixUnitaireFige = p.prixGros
            )
        }
        commandeDao.insertLignesCommande(lignesEntities)
    }

    override suspend fun modifierCommande(id: String, c: NouvelleCommande) {
        val existing = commandeDao.getCommandeWithDetailsByIdSync(id) ?: return
        if (existing.commande.livreeA != null) return

        val produits = getProduits()
        val updatedCommande = existing.commande.copy(
            date = c.date.toEpochDay(),
            heureSouhaitee = c.heure,
            echeance = c.date.plusDays(7).toEpochDay()
        )
        commandeDao.updateCommande(updatedCommande)

        val newLignesEntities = c.lignes.filter { it.quantite > 0 }.mapNotNull { d ->
            val p = produits.firstOrNull { it.id == d.produitId } ?: return@mapNotNull null
            val prixAncien = existing.lignes.firstOrNull { it.produitId == d.produitId }?.prixUnitaireFige ?: p.prixGros
            LigneCommandeEntity(
                id = UUID.randomUUID().toString(),
                commandeId = id,
                produitId = p.id,
                nomProduit = p.nom,
                quantite = d.quantite,
                prixUnitaireFige = prixAncien
            )
        }
        commandeDao.insertLignesCommande(newLignesEntities)
    }

    override suspend fun supprimerCommande(id: String) {
        commandeDao.softDeleteCommande(id)
    }

    override suspend fun livrer(commandeId: String, montantRecu: Long, mode: ModeReglement?) {
        val details = commandeDao.getCommandeWithDetailsByIdSync(commandeId) ?: return
        if (details.commande.livreeA != null) return

        val maintenant = System.currentTimeMillis()
        commandeDao.marquerLivree(commandeId, maintenant)

        val recu = montantRecu.coerceIn(0, details.total)
        val reste = details.total - recu

        if (recu > 0 && mode != null) {
            val paiementEntity = PaiementEntity(
                id = UUID.randomUUID().toString(),
                numero = numero("ENC"),
                clientId = details.commande.clientId ?: "",
                clientNom = details.commande.clientNom,
                commandeId = commandeId,
                montant = recu,
                dateHeure = maintenant,
                mode = mode,
                type = TypeMouvement.ENCAISSE,
                note = "Livraison",
                annule = false
            )
            paiementDao.insertPaiement(paiementEntity)
        }

        if (reste > 0) {
            val aCreditEntity = PaiementEntity(
                id = UUID.randomUUID().toString(),
                numero = numero("CRD"),
                clientId = details.commande.clientId ?: "",
                clientNom = details.commande.clientNom,
                commandeId = commandeId,
                montant = reste,
                dateHeure = maintenant,
                mode = null,
                type = TypeMouvement.A_CREDIT,
                note = "À crédit",
                annule = false
            )
            paiementDao.insertPaiement(aCreditEntity)
        }
    }

    override suspend fun annulerLivraison(commandeId: String) {
        val details = commandeDao.getCommandeWithDetailsByIdSync(commandeId) ?: return
        if (details.commande.livreeA == null) return

        val paiementsCommande = paiementDao.getPaiementsForCommande(commandeId).first()
        for (p in paiementsCommande) {
            paiementDao.annulerPaiement(p.id)
        }

        if (details.commande.nonPrevu) {
            commandeDao.softDeleteCommande(commandeId)
        } else {
            val updated = details.commande.copy(livreeA = null)
            commandeDao.updateCommande(updated)
        }
    }

    override suspend fun venteNonPrevue(lignes: List<LigneDemande>, mode: ModeReglement) {
        val produits = getProduits()
        val demande = lignes.filter { it.quantite > 0 }
        if (demande.isEmpty()) return

        val maintenant = System.currentTimeMillis()
        val commandeId = UUID.randomUUID().toString()
        val aujourdhui = LocalDate.now().toEpochDay()

        val entity = CommandeEntity(
            id = commandeId,
            clientId = null,
            clientNom = "Client direct",
            nonPrevu = true,
            date = aujourdhui,
            heureSouhaitee = "",
            livreeA = maintenant,
            echeance = aujourdhui,
            archive = false
        )
        commandeDao.insertCommande(entity)

        val lignesEntities = demande.mapNotNull { d ->
            val p = produits.firstOrNull { it.id == d.produitId } ?: return@mapNotNull null
            LigneCommandeEntity(
                id = UUID.randomUUID().toString(),
                commandeId = commandeId,
                produitId = p.id,
                nomProduit = p.nom,
                quantite = d.quantite,
                prixUnitaireFige = p.prixPublic
            )
        }
        commandeDao.insertLignesCommande(lignesEntities)

        val total = lignesEntities.sumOf { it.quantite.toLong() * it.prixUnitaireFige }

        val paiement = PaiementEntity(
            id = UUID.randomUUID().toString(),
            numero = numero("ENC"),
            clientId = "",
            clientNom = "Client direct",
            commandeId = commandeId,
            montant = total,
            dateHeure = maintenant,
            mode = mode,
            type = TypeMouvement.ENCAISSE,
            note = "Vente au comptoir",
            annule = false
        )
        paiementDao.insertPaiement(paiement)
    }

    override suspend fun rattacherClient(commandeId: String, client: NouveauClient) {
        val clientId = UUID.randomUUID().toString()
        val clientEntity = ClientEntity(
            id = clientId,
            nom = client.nom.nettoyerNom(),
            telephone = client.telephone,
            quartier = client.quartier.nettoyerNom(),
            plafondCreance = client.plafondCreance,
            type = TypeClient.AUTRE,
            archive = false
        )
        clientDao.insertClient(clientEntity)

        val cmd = commandeDao.getCommandeWithDetailsByIdSync(commandeId) ?: return
        val updatedCmd = cmd.commande.copy(
            clientId = clientId,
            clientNom = clientEntity.nom,
            nonPrevu = false
        )
        commandeDao.updateCommande(updatedCmd)
    }
}
