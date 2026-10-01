package com.example.donaka100.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.donaka100.data.Client
import com.example.donaka100.data.Commande
import com.example.donaka100.ui.CommandeUiState
import com.example.donaka100.ui.CommandeViewModel
import com.example.donaka100.ui.FiltreHistorique
import com.example.donaka100.ui.OngletCommande
import com.example.donaka100.ui.components.*
import com.example.donaka100.ui.forms.*
import com.example.donaka100.ui.screens.commande.*
import com.example.donaka100.ui.theme.*
import com.example.donaka100.ui.util.ouvrirAppel
import com.example.donaka100.ui.util.ouvrirSms
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Quelle feuille / boîte de dialogue est ouverte (une seule à la fois) */
private sealed interface Dialogue {
    // Clients
    data object Creation : Dialogue
    data class Edition(val client: Client) : Dialogue
    data class Actions(val client: Client) : Dialogue
    data class Encaisser(val client: Client) : Dialogue
    data class Suppression(val client: Client) : Dialogue
    // Commandes
    data class CreerCommande(val clientId: String?) : Dialogue
    data class ModifierCommande(val commande: Commande) : Dialogue
    data class Livrer(val commande: Commande) : Dialogue
    data object NonPrevu : Dialogue
    data class Rattacher(val commande: Commande) : Dialogue
    data class AnnulerLivraison(val commande: Commande) : Dialogue
    data class SupprimerCommande(val commande: Commande) : Dialogue
}

@Composable
fun CommandeScreen(vm: CommandeViewModel = viewModel()) {
    val etat by vm.etat.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var dialogue by remember { mutableStateOf<Dialogue?>(null) }

    LaunchedEffect(Unit) { vm.messages.collect { snackbar.showSnackbar(it) } }

    val dateDuJour = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH))
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            // Zone fixe : titre, onglets, recherche
            Column(
                Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column {
                    Text("Gestion des Commandes", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TexteFonce)
                    Text(
                        when (etat.onglet) {
                            OngletCommande.DU_JOUR -> "Livraisons du jour • $dateDuJour"
                            OngletCommande.CREANCES -> "Suivi des soldes clients"
                            OngletCommande.HISTORIQUE -> "Journal des encaissements & règlements clients"
                        },
                        fontSize = 12.sp, color = TexteGris
                    )
                }
                DonakaSegmentedToggle(
                    options = OngletCommande.entries,
                    selected = etat.onglet,
                    onSelect = vm::onOnglet,
                    label = { it.libelle }
                )
                if (etat.erreur == null) {
                    when (etat.onglet) {
                        OngletCommande.CREANCES -> DonakaSearchBar(
                            value = etat.recherche,
                            onValueChange = vm::onRecherche,
                            placeholder = "Rechercher par nom, téléphone…"
                        )
                        OngletCommande.HISTORIQUE -> {
                            DonakaSearchBar(
                                value = etat.rechercheHistorique,
                                onValueChange = vm::onRechercheHistorique,
                                placeholder = "Rechercher par client, bon #ENC…"
                            )
                            DonakaFilterChips(
                                options = FiltreHistorique.entries,
                                selected = etat.filtre,
                                onSelect = vm::onFiltre,
                                label = { it.libelle }
                            )
                        }
                        OngletCommande.DU_JOUR -> Unit
                    }
                }
            }

            when {
                etat.erreur != null -> DonakaEmptyState(
                    titre = "Connexion impossible", message = etat.erreur!!,
                    icone = Icons.Default.CloudOff,
                    labelAction = "Réessayer", onAction = vm::charger
                )

                etat.onglet == OngletCommande.DU_JOUR -> CommandesTab(
                    etat = etat,
                    modifier = Modifier.weight(1f),
                    onNonPrevu = { dialogue = Dialogue.NonPrevu },
                    onCommandeDemain = { dialogue = Dialogue.CreerCommande(null) },
                    onLivrer = { dialogue = Dialogue.Livrer(it) },
                    onModifier = { dialogue = Dialogue.ModifierCommande(it) },
                    onSupprimer = { dialogue = Dialogue.SupprimerCommande(it) },
                    onAnnuler = { dialogue = Dialogue.AnnulerLivraison(it) },
                    onRattacher = { dialogue = Dialogue.Rattacher(it) },
                    onAjouterDemain = { dialogue = Dialogue.CreerCommande(it) }
                )

                etat.onglet == OngletCommande.HISTORIQUE -> HistoriqueTab(
                    etat = etat,
                    modifier = Modifier.weight(1f)
                )

                else -> ListeCreances(
                    etat = etat,
                    modifier = Modifier.weight(1f),
                    onNouveau = { dialogue = Dialogue.Creation },
                    onEncaisser = { dialogue = Dialogue.Encaisser(it) },
                    onOptions = { dialogue = Dialogue.Actions(it) },
                    onAppeler = { context.ouvrirAppel(it.telephone) }
                )
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }

    when (val d = dialogue) {
        null -> Unit

        // ----- Clients -----
        Dialogue.Creation -> ClientFormSheet(
            onDismiss = { dialogue = null },
            onSave = { vm.creerClient(it); dialogue = null }
        )

        is Dialogue.Edition -> ClientFormSheet(
            initial = d.client,
            onDismiss = { dialogue = null },
            onSave = { vm.modifierClient(d.client.id, it); dialogue = null }
        )

        is Dialogue.Encaisser -> EncaissementSheet(
            client = d.client,
            onDismiss = { dialogue = null },
            onConfirm = { montant, mode -> vm.encaisser(d.client, montant, mode); dialogue = null }
        )

        is Dialogue.Actions -> ClientActionsSheet(
            client = d.client,
            onDismiss = { dialogue = null },
            onAppeler = { dialogue = null; context.ouvrirAppel(d.client.telephone) },
            onRappel = {
                dialogue = null
                context.ouvrirSms(
                    d.client.telephone,
                    "Bonjour ${d.client.nom}, petit rappel amical : il vous reste " +
                            "${d.client.resteDu.enMGA()} à régler à la boulangerie Donaka. Merci !"
                )
            },
            onReleve = {
                dialogue = null
                scope.launch { snackbar.showSnackbar("Relevé : bientôt disponible") }
            },
            onModifier = { dialogue = Dialogue.Edition(d.client) },
            onSupprimer = { dialogue = Dialogue.Suppression(d.client) }
        )

        is Dialogue.Suppression -> DonakaConfirmDialog(
            titre = "Supprimer ce client ?",
            message = "Voulez-vous vraiment supprimer « ${d.client.nom} » ?" +
                    if (!d.client.aJour) " Sa dette de ${d.client.resteDu.enMGA()} sera perdue." else "",
            onConfirm = { vm.supprimerClient(d.client); dialogue = null },
            onDismiss = { dialogue = null }
        )

        // ----- Commandes -----
        is Dialogue.CreerCommande -> CommandeFormSheet(
            produits = etat.produits,
            clients = etat.clients,
            date = LocalDate.now().plusDays(1),
            clientIdInitial = d.clientId,
            onDismiss = { dialogue = null },
            onSave = { vm.creerCommande(it); dialogue = null }
        )

        is Dialogue.ModifierCommande -> CommandeFormSheet(
            produits = etat.produits,
            clients = etat.clients,
            date = d.commande.date,
            initial = d.commande,
            onDismiss = { dialogue = null },
            onSave = { vm.modifierCommande(d.commande.id, it); dialogue = null }
        )

        is Dialogue.Livrer -> LivraisonSheet(
            commande = d.commande,
            client = etat.client(d.commande.clientId),
            onDismiss = { dialogue = null },
            onConfirm = { recu, mode -> vm.livrer(d.commande, recu, mode); dialogue = null }
        )

        Dialogue.NonPrevu -> VenteNonPrevueSheet(
            produits = etat.produits,
            onDismiss = { dialogue = null },
            onSave = { lignes, mode -> vm.venteNonPrevue(lignes, mode); dialogue = null }
        )

        is Dialogue.Rattacher -> ClientFormSheet(
            onDismiss = { dialogue = null },
            onSave = { vm.rattacherClient(d.commande, it); dialogue = null }
        )

        is Dialogue.AnnulerLivraison -> DonakaConfirmDialog(
            titre = "Annuler la livraison ?",
            message = "L'encaissement et la créance liés à cette livraison seront supprimés." +
                    if (d.commande.nonPrevu) " Cette vente « client non prévu » sera retirée." else
                        " La commande repasse dans « À livrer ».",
            labelConfirmer = "Oui, retirer",
            onConfirm = { vm.annulerLivraison(d.commande); dialogue = null },
            onDismiss = { dialogue = null }
        )

        is Dialogue.SupprimerCommande -> DonakaConfirmDialog(
            titre = "Supprimer cette commande ?",
            message = "La commande de ${d.commande.clientNom} (${d.commande.total.enMGA()}) sera supprimée.",
            onConfirm = { vm.supprimerCommande(d.commande); dialogue = null },
            onDismiss = { dialogue = null }
        )
    }
}
@Composable
private fun ListeCreances(
    etat: CommandeUiState,
    modifier: Modifier,
    onNouveau: () -> Unit,
    onEncaisser: (Client) -> Unit,
    onOptions: (Client) -> Unit,
    onAppeler: (Client) -> Unit
) {
    val clients = etat.clientsAffiches

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        userScrollEnabled = !etat.isLoading
    ) {
        item {
            DonakaButton(
                "Nouveau Client", onNouveau,
                icone = Icons.Default.PersonAdd, pleineLargeur = true
            )
        }
        item { CreancesBanner(etat.totalCreances, etat.nbDebiteurs, etat.isLoading) }
        item {
            Text(
                "Comptes Clients & Dépôts" + if (!etat.isLoading) " (${clients.size})" else "",
                fontWeight = FontWeight.SemiBold, color = TexteFonce
            )
        }

        when {
            etat.isLoading -> items(3) { DonakaCard(isLoading = true) {} }

            clients.isEmpty() -> item {
                AucunClient(recherche = etat.recherche.isNotBlank())
            }

            else -> items(clients, key = { it.id }) { client ->
                ClientCard(
                    client = client,
                    onEncaisser = { onEncaisser(client) },
                    onOptions = { onOptions(client) },
                    onAppeler = { onAppeler(client) }
                )
            }
        }
    }
}

@Composable
private fun AucunClient(recherche: Boolean) {
    Column(
        Modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.PersonOff, null, tint = TexteGris.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(8.dp))
        Text(
            if (recherche) "Aucun client trouvé" else "Aucun client enregistré",
            fontWeight = FontWeight.SemiBold, color = TexteFonce
        )
        Text(
            if (recherche) "Vérifiez le nom ou créez une nouvelle fiche client."
            else "Ajoute ton premier client avec le bouton ci-dessus.",
            color = TexteGris, textAlign = TextAlign.Center, fontSize = 13.sp
        )
    }
}