package com.example.donaka100.data

import com.example.donaka100.DonakaApplication
import com.example.donaka100.data.local.DonakaDatabase
import com.example.donaka100.data.local.entity.DepenseEntity
import com.example.donaka100.data.local.entity.FournisseurEntity
import com.example.donaka100.data.local.entity.IngredientEntity
import com.example.donaka100.data.local.entity.MouvementStockEntity
import com.example.donaka100.data.local.entity.PaiementEntity
import com.example.donaka100.data.local.model.ClientWithCommandesAndPaiements
import com.example.donaka100.data.local.model.CommandeWithDetails
import com.example.donaka100.data.local.model.ProduitWithRecette
import com.example.donaka100.data.local.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.roundToInt

interface BoardRepository {
    fun observeBoard(): Flow<BoardData>
    suspend fun getBoard(): BoardData
    suspend fun ajouterVente(v: NouvelleVenteComptoir)
    suspend fun ajouterDepense(d: NouvelleDepense)
    suspend fun creerClient(c: NouveauClient)
    suspend fun acheter(a: NouvelAchat)
}

private data class FluxFinanciers(
    val paiements: List<PaiementEntity>,
    val depenses: List<DepenseEntity>,
    val mouvements: List<MouvementStockEntity>
)

private data class FluxEntites(
    val commandes: List<CommandeWithDetails>,
    val clients: List<ClientWithCommandesAndPaiements>,
    val ingredients: List<IngredientEntity>,
    val produits: List<ProduitWithRecette>,
    val fournisseurs: List<FournisseurEntity>
)

class RoomBoardRepository(
    private val database: DonakaDatabase = DonakaApplication.instance.database,
    private val reglagesRepository: ReglagesRepository = DonakaApplication.instance.reglagesRepository
) : BoardRepository {

    private val db get() = database
    private val commandeDao get() = db.commandeDao()
    private val clientDao get() = db.clientDao()
    private val stockDao get() = db.stockDao()
    private val depenseDao get() = db.depenseDao()
    private val paiementDao get() = db.paiementDao()
    private val fournisseurDao get() = db.fournisseurDao()

    private val clientsRepo get() = RoomCommandeRepository(database)
    private val stockRepo get() = RoomStockRepository(database)
    private val hhmm = DateTimeFormatter.ofPattern("HH:mm")

    override fun observeBoard(): Flow<BoardData> {
        val fluxFinanciers = combine(
            paiementDao.getAllPaiements(),
            depenseDao.getAllDepenses(),
            stockDao.getAllMouvementsStock()
        ) { paiements, depenses, mouvements ->
            FluxFinanciers(paiements, depenses, mouvements)
        }

        val fluxEntites = combine(
            commandeDao.getAllCommandesWithDetails(),
            clientDao.getAllClientsWithDetails(),
            stockDao.getAllIngredients(),
            stockDao.getAllProduitsWithRecette(),
            fournisseurDao.getAllFournisseurs()
        ) { commandes, clients, ingredients, produits, fournisseurs ->
            FluxEntites(commandes, clients, ingredients, produits, fournisseurs)
        }

        return combine(fluxFinanciers, fluxEntites, reglagesRepository.observeReglages()) { fin, ent, reglages ->
            calculerBoard(fin, ent, reglages)
        }
    }

    override suspend fun getBoard(): BoardData {
        return observeBoard().first()
    }

    private fun calculerBoard(fin: FluxFinanciers, ent: FluxEntites, reglages: Reglages): BoardData {
        val auj = LocalDate.now()
        val zone = ZoneId.systemDefault()
        val debutAuj = auj.atStartOfDay(zone).toInstant().toEpochMilli()
        val finAuj = auj.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1

        val hier = auj.minusDays(1)
        val debutHier = hier.atStartOfDay(zone).toInstant().toEpochMilli()
        val finHier = auj.atStartOfDay(zone).toInstant().toEpochMilli() - 1

        val paiementsAuj = fin.paiements
            .filter { !it.annule && it.type != TypeMouvement.A_CREDIT && it.dateHeure in debutAuj..finAuj }
        val encaisse = paiementsAuj.sumOf { it.montant }

        val depensesAuj = fin.depenses
            .filter { !it.annule && it.dateHeure in debutAuj..finAuj }
        val achatsMouvementsAuj = fin.mouvements
            .filter { it.estAchat && !it.annule && (it.montant ?: 0L) > 0L && it.dateHeure in debutAuj..finAuj }

        val sorties = depensesAuj.sumOf { it.montant } + achatsMouvementsAuj.sumOf { it.montant ?: 0L }
        val tresorerie = encaisse - sorties

        val paiementsHier = fin.paiements
            .filter { !it.annule && it.type != TypeMouvement.A_CREDIT && it.dateHeure in debutHier..finHier }
        val depensesHier = fin.depenses
            .filter { !it.annule && it.dateHeure in debutHier..finHier }
        val achatsHier = fin.mouvements
            .filter { it.estAchat && !it.annule && (it.montant ?: 0L) > 0L && it.dateHeure in debutHier..finHier }

        val veilleSorties = depensesHier.sumOf { it.montant } + achatsHier.sumOf { it.montant ?: 0L }
        val veilleTresorerie = paiementsHier.sumOf { it.montant } - veilleSorties

        val variation = if (veilleTresorerie == 0L) null
        else ((tresorerie - veilleTresorerie) * 100.0 / abs(veilleTresorerie)).roundToInt()

        val commandesDetails = ent.commandes
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

        val clientsWithDetails = ent.clients.map { it.toDomain() }
        val debiteurs = clientsWithDetails.filter { it.resteDu > 0 }

        val operations = buildList<Pair<Long, Operation>> {
            paiementsAuj.forEach {
                val dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(it.dateHeure), zone)
                add(it.dateHeure to Operation(
                    "e-${it.id}", it.clientNom, dt.format(hhmm),
                    it.mode?.libelle.orEmpty(), it.montant
                ))
            }
            depensesAuj.forEach {
                val dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(it.dateHeure), zone)
                add(it.dateHeure to Operation(
                    "d-${it.id}", it.categorie, dt.format(hhmm),
                    it.note.ifBlank { it.mode.libelle }, -it.montant
                ))
            }
            achatsMouvementsAuj.forEach {
                val dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(it.dateHeure), zone)
                add(it.dateHeure to Operation(
                    "a-${it.id}", "Achat : ${it.ingredientNom}", dt.format(hhmm),
                    it.fournisseur.ifBlank { "Stock" }, -(it.montant ?: 0L)
                ))
            }
        }.sortedByDescending { it.first }.take(5).map { it.second }

        val fichesFournisseurs = ent.fournisseurs.map { it.nom }
        val ingredientsFournisseurs = ent.ingredients.map { it.fournisseur }
        val fournisseurs = (fichesFournisseurs + ingredientsFournisseurs)
            .filter { it.isNotBlank() }
            .distinctBy { it.cleNom() }
            .sortedBy { it.lowercase() }

        val ingredientsDomain = ent.ingredients.map { it.toDomain() }
        val produitsDomain = ent.produits.map { it.toDomain() }.filterNot { it.archive }
        val depensesEntities = fin.depenses.filter { !it.annule }

        return BoardData(
            nomUtilisateur = reglages.nomChef,
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
        val commandeRepo = RoomCommandeRepository(database)
        commandeRepo.venteNonPrevue(v.lignes, v.mode)
    }

    override suspend fun ajouterDepense(d: NouvelleDepense) {
        val depenseRepo = RoomDepenseRepository(database)
        depenseRepo.creer(d)
    }

    override suspend fun creerClient(c: NouveauClient) {
        clientsRepo.creerClient(c)
    }

    override suspend fun acheter(a: NouvelAchat) {
        stockRepo.acheter(a)
    }
}
