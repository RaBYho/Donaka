package com.example.donaka100.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.donaka100.data.*
import com.example.donaka100.ui.components.enMGA
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
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
 * Exécute les requêtes sur Dispatchers.IO pour éliminer toute saccade lors de la mise à jour de l'UI.
 */
class CommandeViewModel(
    private val repository: CommandeRepository = RoomCommandeRepository()
) : ViewModel() {

    private val _etat = MutableStateFlow(CommandeUiState())
    val etat: StateFlow<CommandeUiState> = _etat.asStateFlow()

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages: Flow<String> = _messages.receiveAsFlow()

    init { charger() }

    private suspend fun rafraichir() = withContext(Dispatchers.IO) {
        coroutineScope {
            val clientsDef = async { repository.getClients() }
            val encaissementsDef = async { repository.getEncaissements() }
            val commandesDef = async { repository.getCommandes() }
            val produitsDef = async { repository.getProduits() }

            val clients = clientsDef.await()
            val encaissements = encaissementsDef.await()
            val commandes = commandesDef.await()
            val produits = produitsDef.await()

            _etat.update {
                it.copy(clients = clients, encaissements = encaissements, commandes = commandes, produits = produits)
            }
        }
    }

    fun charger() {
        viewModelScope.launch {
            _etat.update { it.copy(isLoading = true, erreur = null) }
            try {
                rafraichir()
                _etat.update { it.copy(isLoading = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _etat.update { it.copy(isLoading = false, erreur = "Impossible de charger les données.") }
            }
        }
    }

    fun onRecherche(texte: String) = _etat.update { it.copy(recherche = texte) }
    fun onRechercheHistorique(texte: String) = _etat.update { it.copy(rechercheHistorique = texte) }
    fun onFiltre(filtre: FiltreHistorique) = _etat.update { it.copy(filtre = filtre) }
    fun onOnglet(onglet: OngletCommande) = _etat.update { it.copy(onglet = onglet) }

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
        val existe = _etat.value.commandes.any { it.clientId == n.clientId && it.date == n.date }
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

    /** Modèle d'action optimisé : exécute en IO, rafraîchit réactivement, notifie */
    private fun action(succes: String, bloc: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { bloc() }
                rafraichir()
                _messages.send(succes)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _messages.send("Échec de l'opération. Réessaie.")
            }
        }
    }

    /** À l'ouverture de l'écran : met à jour en arrière-plan sans skeleton */
    fun actualiser() {
        viewModelScope.launch {
            try {
                rafraichir()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) { }
        }
    }
}