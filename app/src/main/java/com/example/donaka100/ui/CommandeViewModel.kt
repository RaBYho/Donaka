package com.example.donaka100.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.donaka100.data.*
import com.example.donaka100.ui.components.enMGA
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth

enum class OngletCommande(val libelle: String) {
    DU_JOUR("Commandes"), CREANCES("Clients"), HISTORIQUE("Historique")
}

enum class FiltreHistorique(val libelle: String) {
    TOUT("Tout"),
    AUJOURDHUI("Aujourd'hui"),
    CE_MOIS("Ce Mois"),
    ESPECES("Espèces"),
    MVOLA("MVola"),
    AIRTEL_ORANGE("Airtel / Orange"),
    A_CREDIT("À crédit");

    fun accepte(e: Encaissement, aujourdhui: LocalDate): Boolean = when (this) {
        TOUT -> true
        AUJOURDHUI -> e.dateHeure.toLocalDate() == aujourdhui
        CE_MOIS -> YearMonth.from(e.dateHeure) == YearMonth.from(aujourdhui)
        ESPECES -> e.mode == ModeReglement.ESPECES
        MVOLA -> e.mode == ModeReglement.MVOLA
        AIRTEL_ORANGE -> e.mode == ModeReglement.AIRTEL_ORANGE
        A_CREDIT -> e.type == TypeMouvement.A_CREDIT
    }
}

data class FiltresCommandeUi(
    val recherche: String = "",
    val rechercheHistorique: String = "",
    val filtre: FiltreHistorique = FiltreHistorique.TOUT,
    val onglet: OngletCommande = OngletCommande.DU_JOUR
)

private data class DonneesCommande(
    val clients: List<Client>,
    val encaissements: List<Encaissement>,
    val commandes: List<Commande>,
    val produits: List<Produit>
)

data class CommandeUiState(
    val isLoading: Boolean = true,
    val erreur: String? = null,
    val clients: List<Client> = emptyList(),
    val encaissements: List<Encaissement> = emptyList(),
    val commandes: List<Commande> = emptyList(),
    val produits: List<Produit> = emptyList(),
    val recherche: String = "",
    val rechercheHistorique: String = "",
    val filtre: FiltreHistorique = FiltreHistorique.TOUT,
    val onglet: OngletCommande = OngletCommande.DU_JOUR
) {
    // ----- Clients & créances -----
    val totalCreances: Long get() = clients.sumOf { it.resteDu }
    val nbDebiteurs: Int get() = clients.count { !it.aJour }

    val clientsAffiches: List<Client>
        get() {
            val q = recherche.trim().lowercase()
            val chiffres = q.filter { it.isDigit() }
            val filtres = if (q.isEmpty()) clients else clients.filter {
                it.nom.lowercase().contains(q) ||
                        (chiffres.isNotEmpty() && it.telephone.contains(chiffres))
            }
            return filtres.sortedByDescending { it.resteDu }
        }

    fun client(id: String?): Client? = clients.firstOrNull { it.id == id }

    // ----- Livraisons -----
    val aLivrer: List<Commande>
        get() {
            val auj = LocalDate.now()
            return commandes
                .filter { !it.livree && it.date <= auj }
                .sortedWith(compareBy<Commande>({ it.date }, { it.heure.isBlank() }, { it.heure }, { it.clientNom }))
        }

    val livrees: List<Commande>
        get() = commandes
            .filter { it.livree && it.date == LocalDate.now() }
            .sortedByDescending { it.livreeA }

    val commandesDemain: List<Commande>
        get() = commandes
            .filter { it.date == LocalDate.now().plusDays(1) }
            .sortedBy { it.clientNom }

    fun commandeDemainDe(clientId: String): Commande? =
        commandesDemain.firstOrNull { it.clientId == clientId }

    val valeurLivreeAujourdhui: Long get() = livrees.sumOf { it.total }

    // ----- Historique -----
    val totalEncaisseAujourdhui: Long
        get() {
            val auj = LocalDate.now()
            return encaissements
                .filter { it.argentRecu && it.dateHeure.toLocalDate() == auj }
                .sumOf { it.montant }
        }

    val historiqueAffiche: List<Encaissement>
        get() {
            val auj = LocalDate.now()
            val q = rechercheHistorique.trim().lowercase().removePrefix("#")
            return encaissements
                .filter { filtre.accepte(it, auj) }
                .filter {
                    q.isEmpty() ||
                            it.clientNom.lowercase().contains(q) ||
                            it.numero.lowercase().contains(q) ||
                            it.note.lowercase().contains(q)
                }
                .sortedByDescending { it.dateHeure }
        }
}

/**
 * ViewModel réactif pour la gestion des commandes et encaissements.
 */
class CommandeViewModel(
    private val repository: CommandeRepository = RoomCommandeRepository()
) : ViewModel() {

    private val _filtres = MutableStateFlow(FiltresCommandeUi())
    private val _relance = MutableStateFlow(0)

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages: Flow<String> = _messages.receiveAsFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val donneesCommandeFlow = _relance.flatMapLatest {
        combine(
            repository.observeClients(),
            repository.observeEncaissements(),
            repository.observeCommandes(),
            repository.observeProduits()
        ) { clients, encaissements, commandes, produits ->
            DonneesCommande(clients, encaissements, commandes, produits)
        }
    }

    val etat: StateFlow<CommandeUiState> = combine(
        donneesCommandeFlow,
        _filtres
    ) { d, f ->
        CommandeUiState(
            isLoading = false,
            erreur = null,
            clients = d.clients,
            encaissements = d.encaissements,
            commandes = d.commandes,
            produits = d.produits,
            recherche = f.recherche,
            rechercheHistorique = f.rechercheHistorique,
            filtre = f.filtre,
            onglet = f.onglet
        )
    }.catch { e ->
        if (e is CancellationException) throw e
        emit(CommandeUiState(isLoading = false, erreur = "Impossible de charger les données."))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CommandeUiState(isLoading = true)
    )

    fun charger() {
        _relance.update { it + 1 }
    }

    fun actualiser() {
        viewModelScope.launch {
            _isRefreshing.value = true
            val debut = System.currentTimeMillis()
            _relance.update { it + 1 }
            verifierSauvegardePointExtension()
            val duree = System.currentTimeMillis() - debut
            if (duree < 500) {
                delay(500 - duree)
            }
            _isRefreshing.value = false
        }
    }

    private suspend fun verifierSauvegardePointExtension() {
        // Point d'extension pour vérification future de la sauvegarde
    }

    fun onRecherche(texte: String) = _filtres.update { it.copy(recherche = texte) }
    fun onRechercheHistorique(texte: String) = _filtres.update { it.copy(rechercheHistorique = texte) }
    fun onFiltre(filtre: FiltreHistorique) = _filtres.update { it.copy(filtre = filtre) }
    fun onOnglet(onglet: OngletCommande) = _filtres.update { it.copy(onglet = onglet) }

    // ----- Clients -----
    fun creerClient(c: NouveauClient) =
        action("Fiche client créée : ${c.nom}") { repository.creerClient(c) }

    fun modifierClient(id: String, c: NouveauClient) =
        action("Fiche client mise à jour") { repository.modifierClient(id, c) }

    fun supprimerClient(client: Client) =
        action("${client.nom} supprimé") { repository.supprimerClient(client.id) }

    fun encaisser(client: Client, montant: Long, mode: ModeReglement) =
        action("Règlement de ${montant.enMGA()} encaissé") { repository.encaisser(client.id, montant, mode) }

    // ----- Commandes -----
    fun creerCommande(n: NouvelleCommande) {
        val existe = etat.value.commandes.any { it.clientId == n.clientId && it.date == n.date }
        if (existe) {
            viewModelScope.launch { _messages.send("Ce client a déjà une commande pour ce jour : modifie-la.") }
            return
        }
        action("Commande enregistrée") { repository.creerCommande(n) }
    }

    fun modifierCommande(id: String, n: NouvelleCommande) =
        action("Commande mise à jour") { repository.modifierCommande(id, n) }

    fun supprimerCommande(c: Commande) =
        action("Commande de ${c.clientNom} supprimée") { repository.supprimerCommande(c.id) }

    fun livrer(c: Commande, montantRecu: Long, mode: ModeReglement?) {
        val reste = c.total - montantRecu
        val message =
            if (reste > 0) "Livraison enregistrée · ${reste.enMGA()} à crédit" else "Livraison enregistrée"
        action(message) { repository.livrer(c.id, montantRecu, mode) }
    }

    fun annulerLivraison(c: Commande) =
        action("Livraison annulée") { repository.annulerLivraison(c.id) }

    fun venteNonPrevue(lignes: List<LigneDemande>, mode: ModeReglement) =
        action("Vente enregistrée (client non prévu)") { repository.venteNonPrevue(lignes, mode) }

    fun rattacherClient(c: Commande, client: NouveauClient) =
        action("${client.nom} ajouté comme client") { repository.rattacherClient(c.id, client) }

    private fun action(succes: String, bloc: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { bloc() }
                _messages.send(succes)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _messages.send(e.message ?: "Échec de l'opération. Réessaie.")
            }
        }
    }
}