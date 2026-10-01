package com.example.donaka100.data

import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
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

class FakeCommandeRepository(avecDemo: Boolean = true) : CommandeRepository {

    private val db = FakeBackend

    init { if (avecDemo) db.chargerDemo() }

    private fun numero(prefixe: String): String {
        db.compteur++
        return "$prefixe-%04d".format(db.compteur)
    }

    /** Transforme les quantités saisies en lignes, avec le prix de gros (figé si la ligne existait déjà) */
    private fun lignes(demande: List<LigneDemande>, anciennes: List<LigneArticle> = emptyList()) =
        demande.filter { it.quantite > 0 }.mapNotNull { d ->
            val p = db.produits.firstOrNull { it.id == d.produitId } ?: return@mapNotNull null
            val prix = anciennes.firstOrNull { it.produitId == d.produitId }?.prixUnitaire ?: p.prixGros
            LigneArticle(p.id, p.nom, d.quantite, prix)
        }

    override suspend fun getClients(): List<Client> { delay(500); return db.clients }
    override suspend fun getEncaissements() = db.encaissements
    override suspend fun getCommandes() = db.commandes
    override suspend fun getProduits() = db.produits.filterNot { it.archive }

    // ---------- Clients ----------

    override suspend fun creerClient(client: NouveauClient) {
        delay(300)
        db.clients = db.clients + Client(
            id = UUID.randomUUID().toString(), nom = client.nom, telephone = client.telephone,
            quartier = client.quartier, plafondCreance = client.plafondCreance
        )
    }

    override suspend fun modifierClient(id: String, client: NouveauClient) {
        delay(300)
        db.clients = db.clients.map {
            if (it.id == id) it.copy(
                nom = client.nom, telephone = client.telephone,
                quartier = client.quartier, plafondCreance = client.plafondCreance
            ) else it
        }
    }

    override suspend fun supprimerClient(id: String) {
        delay(300)
        db.clients = db.clients.filterNot { it.id == id }
    }

    /** Paiement d'une créance existante, hors livraison */
    override suspend fun encaisser(clientId: String, montant: Long, mode: ModeReglement) {
        delay(300)
        val client = db.clients.first { it.id == clientId }
        val reste = (client.resteDu - montant).coerceAtLeast(0)
        db.clients = db.clients.map {
            if (it.id != clientId) it
            else it.copy(resteDu = reste, detailDette = if (reste == 0L) "" else it.detailDette)
        }
        val debut = if (reste == 0L) "Solde de la créance" else "Règlement partiel"
        db.encaissements = db.encaissements + Encaissement(
            id = UUID.randomUUID().toString(), numero = numero("ENC"), clientNom = client.nom,
            dateHeure = LocalDateTime.now(), mode = mode, montant = montant,
            note = if (client.detailDette.isBlank()) debut else "$debut : ${client.detailDette}",
            type = TypeMouvement.REGLEMENT
        )
    }

    // ---------- Commandes ----------

    override suspend fun creerCommande(c: NouvelleCommande) {
        delay(300)
        val client = db.clients.first { it.id == c.clientId }
        db.commandes = db.commandes + Commande(
            id = UUID.randomUUID().toString(), clientId = client.id, clientNom = client.nom,
            date = c.date, heure = c.heure, lignes = lignes(c.lignes)
        )
    }

    override suspend fun modifierCommande(id: String, c: NouvelleCommande) {
        delay(300)
        db.commandes = db.commandes.map {
            if (it.id == id && !it.livree)
                it.copy(date = c.date, heure = c.heure, lignes = lignes(c.lignes, it.lignes))
            else it
        }
    }

    override suspend fun supprimerCommande(id: String) {
        delay(300)
        db.commandes = db.commandes.filterNot { it.id == id && !it.livree }
    }

    override suspend fun livrer(commandeId: String, montantRecu: Long, mode: ModeReglement?) {
        delay(300)
        val cmd = db.commandes.first { it.id == commandeId }
        check(!cmd.livree) { "Déjà livrée" }

        val recu = montantRecu.coerceIn(0, cmd.total)
        val reste = cmd.total - recu
        val maintenant = LocalDateTime.now()

        // Une commande en retard livrée aujourd'hui compte pour aujourd'hui
        db.commandes = db.commandes.map {
            if (it.id != commandeId) it
            else it.copy(
                date = maintenant.toLocalDate(), livreeA = maintenant.toLocalTime(),
                montantPaye = recu, mode = if (recu > 0) mode else null
            )
        }

        val nouveaux = mutableListOf<Encaissement>()
        if (recu > 0) {
            nouveaux += Encaissement(
                UUID.randomUUID().toString(), numero("ENC"), cmd.clientNom, maintenant, mode, recu,
                "Livraison : ${cmd.resume}", TypeMouvement.ENCAISSE, cmd.id
            )
        }
        if (reste > 0) {
            nouveaux += Encaissement(
                UUID.randomUUID().toString(), numero("CRD"), cmd.clientNom, maintenant, null, reste,
                "À crédit : ${cmd.resume}", TypeMouvement.A_CREDIT, cmd.id
            )
            val jour = maintenant.format(DateTimeFormatter.ofPattern("dd/MM"))
            db.clients = db.clients.map {
                if (it.id != cmd.clientId) it
                else it.copy(
                    resteDu = it.resteDu + reste,
                    detailDette = if (it.detailDette.isBlank()) "Livraison du $jour : ${cmd.resume}"
                    else "Plusieurs livraisons"
                )
            }
        }
        db.encaissements = db.encaissements + nouveaux
    }

    override suspend fun annulerLivraison(commandeId: String) {
        delay(300)
        val cmd = db.commandes.first { it.id == commandeId }
        check(cmd.livree) { "Pas encore livrée" }
        val reste = cmd.resteACredit

        db.encaissements = db.encaissements.filterNot { it.commandeId == cmd.id }
        if (reste > 0) {
            db.clients = db.clients.map {
                if (it.id != cmd.clientId) it else {
                    val nouveau = (it.resteDu - reste).coerceAtLeast(0)
                    it.copy(resteDu = nouveau, detailDette = if (nouveau == 0L) "" else it.detailDette)
                }
            }
        }
        db.commandes =
            if (cmd.nonPrevu) db.commandes.filterNot { it.id == cmd.id }   // n'était pas prévue : on la retire
            else db.commandes.map {
                if (it.id == cmd.id) it.copy(livreeA = null, montantPaye = 0, mode = null) else it
            }
    }

    override suspend fun venteNonPrevue(lignes: List<LigneDemande>, mode: ModeReglement) {
        delay(300)
        val l = lignes(lignes)
        require(l.isNotEmpty())
        val maintenant = LocalDateTime.now()
        val total = l.sumOf { it.montant }
        val cmd = Commande(
            id = UUID.randomUUID().toString(), clientId = null, clientNom = "Client non prévu",
            nonPrevu = true, date = maintenant.toLocalDate(), lignes = l,
            livreeA = maintenant.toLocalTime(), montantPaye = total, mode = mode
        )
        db.commandes = db.commandes + cmd
        db.encaissements = db.encaissements + Encaissement(
            UUID.randomUUID().toString(), numero("ENC"), cmd.clientNom, maintenant, mode, total,
            "Livraison : ${cmd.resume}", TypeMouvement.ENCAISSE, cmd.id
        )
    }

    override suspend fun rattacherClient(commandeId: String, client: NouveauClient) {
        delay(300)
        val nouveau = Client(
            id = UUID.randomUUID().toString(), nom = client.nom, telephone = client.telephone,
            quartier = client.quartier, plafondCreance = client.plafondCreance
        )
        db.clients = db.clients + nouveau
        db.commandes = db.commandes.map {
            if (it.id == commandeId) it.copy(clientId = nouveau.id, clientNom = nouveau.nom, nonPrevu = false) else it
        }
        db.encaissements = db.encaissements.map {
            if (it.commandeId == commandeId) it.copy(clientNom = nouveau.nom) else it
        }
    }
}