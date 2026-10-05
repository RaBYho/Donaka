package com.example.donaka100.data

import com.example.donaka100.DonakaApplication
import com.example.donaka100.data.local.toDomain
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.roundToInt

interface BoardRepository {
    suspend fun getBoard(): BoardData
    suspend fun ajouterVente(v: NouvelleVenteComptoir)
    suspend fun ajouterDepense(d: NouvelleDepense)
    suspend fun creerClient(c: NouveauClient)
    suspend fun acheter(a: NouvelAchat)
}

class RoomBoardRepository : BoardRepository {

    private val db get() = DonakaApplication.instance.database
    private val commandeDao get() = db.commandeDao()
    private val clientDao get() = db.clientDao()
    private val stockDao get() = db.stockDao()
    private val depenseDao get() = db.depenseDao()
    private val paiementDao get() = db.paiementDao()
    private val fournisseurDao get() = db.fournisseurDao()

    private val clientsRepo = RoomCommandeRepository()
    private val stockRepo = RoomStockRepository()
    private val hhmm = DateTimeFormatter.ofPattern("HH:mm")

    override suspend fun getBoard(): BoardData {
        val auj = LocalDate.now()
        val debutAuj = auj.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
        val finAuj = auj.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli() - 1

        val hier = auj.minusDays(1)
        val debutHier = hier.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
        val finHier = auj.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli() - 1

        val paiementsAuj = paiementDao.getAllPaiements().first()
            .filter { !it.annule && it.type != TypeMouvement.A_CREDIT && it.dateHeure in debutAuj..finAuj }
        val encaisse = paiementsAuj.sumOf { it.montant }

        val depensesAuj = depenseDao.getAllDepenses().first()
            .filter { !it.annule && it.dateHeure in debutAuj..finAuj }
        val achatsMouvementsAuj = stockDao.getAllMouvementsStock().first()
            .filter { it.estAchat && !it.annule && (it.montant ?: 0L) > 0L && it.dateHeure in debutAuj..finAuj }

        val sorties = depensesAuj.sumOf { it.montant } + achatsMouvementsAuj.sumOf { it.montant ?: 0L }
        val tresorerie = encaisse - sorties

        val paiementsHier = paiementDao.getAllPaiements().first()
            .filter { !it.annule && it.type != TypeMouvement.A_CREDIT && it.dateHeure in debutHier..finHier }
        val depensesHier = depenseDao.getAllDepenses().first()
            .filter { !it.annule && it.dateHeure in debutHier..finHier }
        val achatsHier = stockDao.getAllMouvementsStock().first()
            .filter { it.estAchat && !it.annule && (it.montant ?: 0L) > 0L && it.dateHeure in debutHier..finHier }

        val veilleSorties = depensesHier.sumOf { it.montant } + achatsHier.sumOf { it.montant ?: 0L }
        val veilleTresorerie = paiementsHier.sumOf { it.montant } - veilleSorties

        val variation = if (veilleTresorerie == 0L) null
        else ((tresorerie - veilleTresorerie) * 100.0 / abs(veilleTresorerie)).roundToInt()

        val commandesDetails = commandeDao.getAllCommandesWithDetails().first()
        val livre = commandesDetails
            .filter { !it.commande.archive && it.commande.livreeA != null && it.commande.date == auj.toEpochDay() }
            .sumOf { it.total }

        val comptoir = commandesDetails
            .filter { !it.commande.archive && it.commande.nonPrevu && it.commande.date == auj.toEpochDay() }
            .sumOf { it.total }

        val credit = paiementsAuj
            .filter { it.type == TypeMouvement.A_CREDIT }
            .sumOf { it.montant }

        val demainDate = auj.plusDays(1).toEpochDay()
        val demainCommandes = commandesDetails.filter { !it.commande.archive && it.commande.date == demainDate }
        val lignesDemain = demainCommandes
            .flatMap { it.lignes }
            .groupBy { it.produitId }
            .map { (_, l) -> LigneCommande(l.first().nomProduit, l.sumOf { it.quantite }) }
            .sortedBy { it.nom }

        val heure = demainCommandes.map { it.commande.heureSouhaitee }.filter { it.isNotBlank() }.minOrNull()
            ?.replace(':', 'h').orEmpty()

        val clientsWithDetails = clientDao.getAllClientsWithDetails().first().map { it.toDomain() }
        val debiteurs = clientsWithDetails.filter { it.resteDu > 0 }

        val operations = buildList<Pair<Long, Operation>> {
            paiementsAuj.forEach {
                val dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(it.dateHeure), ZoneOffset.UTC)
                add(it.dateHeure to Operation(
                    "e-${it.id}", it.clientNom, dt.format(hhmm),
                    it.mode?.libelle.orEmpty(), it.montant
                ))
            }
            depensesAuj.forEach {
                val dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(it.dateHeure), ZoneOffset.UTC)
                add(it.dateHeure to Operation(
                    "d-${it.id}", it.categorie, dt.format(hhmm),
                    it.note.ifBlank { it.mode.libelle }, -it.montant
                ))
            }
            achatsMouvementsAuj.forEach {
                val dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(it.dateHeure), ZoneOffset.UTC)
                add(it.dateHeure to Operation(
                    "a-${it.id}", "Achat : ${it.ingredientNom}", dt.format(hhmm),
                    it.fournisseur.ifBlank { "Stock" }, -(it.montant ?: 0L)
                ))
            }
        }.sortedByDescending { it.first }.take(5).map { it.second }

        val fichesFournisseurs = fournisseurDao.getAllFournisseurs().first().map { it.nom }
        val ingredientsFournisseurs = stockDao.getAllIngredients().first().map { it.fournisseur }
        val fournisseurs = (fichesFournisseurs + ingredientsFournisseurs)
            .filter { it.isNotBlank() }
            .distinctBy { it.cleNom() }
            .sortedBy { it.lowercase() }

        val ingredientsDomain = stockDao.getAllIngredients().first().map { it.toDomain() }
        val produitsDomain = stockDao.getAllProduitsWithRecette().first().map { it.toDomain() }.filterNot { it.archive }
        val depensesEntities = depenseDao.getAllDepenses().first().filter { !it.annule }

        return BoardData(
            nomUtilisateur = "Chef Baker",
            fournilOuvert = true,
            chiffreAffaires = livre + comptoir,
            fournisseurs = fournisseurs,
            encaisse = encaisse,
            creditDuJour = credit,
            achatsEtFrais = sorties,
            variationVeille = variation,
            stocks = ingredientsDomain,
            commandesDemain = lignesDemain,
            heureLivraison = heure,
            nbCreances = debiteurs.size,
            totalCreances = debiteurs.sumOf { it.resteDu },
            dernieresOperations = operations,
            produits = produitsDomain.sortedBy { it.nom.lowercase() },
            categoriesDepense = depensesEntities.map { it.categorie.trim() }.filter { it.isNotEmpty() }
                .distinctBy { it.lowercase() }.sortedBy { it.lowercase() }
        )
    }

    override suspend fun ajouterVente(v: NouvelleVenteComptoir) {
        val commandeRepo = RoomCommandeRepository()
        commandeRepo.venteNonPrevue(v.lignes, v.mode)
    }

    override suspend fun ajouterDepense(d: NouvelleDepense) {
        val depenseRepo = RoomDepenseRepository()
        depenseRepo.creer(d)
    }

    override suspend fun creerClient(c: NouveauClient) {
        clientsRepo.creerClient(c)
    }

    override suspend fun acheter(a: NouvelAchat) {
        stockRepo.acheter(a)
    }
}
