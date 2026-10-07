package com.example.donaka100.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.donaka100.ui.ParametresViewModel
import com.example.donaka100.ui.components.*
import com.example.donaka100.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParametresScreen(
    onRetour: () -> Unit,
    vm: ParametresViewModel = viewModel()
) {
    BackHandler { onRetour() }

    val reglages by vm.reglages.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    var nomChefState by remember(reglages.nomChef) { mutableStateOf(reglages.nomChef) }
    var nomBoulangerieState by remember(reglages.nomBoulangerie) { mutableStateOf(reglages.nomBoulangerie) }

    var dialogueEtape1 by remember { mutableStateOf(false) }
    var dialogueEtape2 by remember { mutableStateOf(false) }
    var motConfirmation by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        vm.messages.collect { snackbar.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Paramètres", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onRetour) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Profil
            item {
                DonakaCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, null, tint = Primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("PROFIL", fontSize = 11.sp, color = TexteGris, fontWeight = FontWeight.Bold)
                    }

                    DonakaTextField(
                        value = nomChefState,
                        onValueChange = { nomChefState = it },
                        label = "Nom du chef"
                    )

                    DonakaTextField(
                        value = nomBoulangerieState,
                        onValueChange = { nomBoulangerieState = it },
                        label = "Nom de la boulangerie"
                    )

                    DonakaButton(
                        texte = "Enregistrer le profil",
                        onClick = { vm.enregistrerProfil(nomChefState, nomBoulangerieState) },
                        style = StyleBouton.PRIMAIRE,
                        pleineLargeur = true
                    )
                }
            }

            // 2. Sécurité
            item {
                DonakaCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, null, tint = TexteGris, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("SÉCURITÉ", fontSize = 11.sp, color = TexteGris, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Code PIN de verrouillage", fontWeight = FontWeight.SemiBold, color = TexteGris)
                            Text("Empêcher l'accès non autorisé", fontSize = 12.sp, color = TexteGris)
                        }
                        DonakaBadge("Bientôt disponible", type = TypeBadge.NEUTRE)
                    }
                }
            }

            // 3. Notifications
            item {
                DonakaCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, null, tint = TexteGris, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("NOTIFICATIONS", fontSize = 11.sp, color = TexteGris, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Rappels & alertes stock", fontWeight = FontWeight.SemiBold, color = TexteGris)
                            Text("Rappels du soir et alertes farine", fontSize = 12.sp, color = TexteGris)
                        }
                        DonakaBadge("Bientôt disponible", type = TypeBadge.NEUTRE)
                    }
                }
            }

            // 4. Sauvegarde
            item {
                DonakaCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudSync, null, tint = TexteGris, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("SAUVEGARDE & SYNCHRO", fontSize = 11.sp, color = TexteGris, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Sauvegarde cloud automatique", fontWeight = FontWeight.SemiBold, color = TexteGris)
                            Text("Exporter ou restaurer les données", fontSize = 12.sp, color = TexteGris)
                        }
                        DonakaBadge("Bientôt disponible", type = TypeBadge.NEUTRE)
                    }
                }
            }

            // 5. Données
            item {
                DonakaCard(containerColor = RougeClair.copy(alpha = 0.3f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DeleteForever, null, tint = Rouge, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("DONNÉES", fontSize = 11.sp, color = Rouge, fontWeight = FontWeight.Bold)
                    }
                    Text("Effacer toutes les données", fontWeight = FontWeight.Bold, color = TexteFonce)
                    Text(
                        "Réinitialise entièrement l'application (base de données et réglages).",
                        fontSize = 12.sp, color = TexteGris
                    )
                    DonakaButton(
                        texte = "Effacer toutes les données",
                        onClick = { dialogueEtape1 = true },
                        style = StyleBouton.DANGER,
                        pleineLargeur = true
                    )
                }
            }

            // 6. À propos
            item {
                DonakaCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null, tint = Primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("À PROPOS", fontSize = 11.sp, color = TexteGris, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Version de l'application", fontWeight = FontWeight.Medium, color = TexteFonce)
                        Text("1.0.0", fontWeight = FontWeight.Bold, color = Primary)
                    }
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Revoir l'assistant de démarrage", fontWeight = FontWeight.Medium, color = TexteGris)
                        DonakaBadge("Bientôt disponible", type = TypeBadge.NEUTRE)
                    }
                }
            }
        }
    }

    // Etape 1 : Confirmation initiale
    if (dialogueEtape1) {
        DonakaConfirmDialog(
            titre = "Effacer toutes les données ?",
            message = "Cette action est irréversible. Toutes les commandes, stocks, clients, dépenses et réglages seront définitivement effacés.",
            labelConfirmer = "Continuer",
            onConfirm = {
                dialogueEtape1 = false
                motConfirmation = ""
                dialogueEtape2 = true
            },
            onDismiss = { dialogueEtape1 = false }
        )
    }

    // Etape 2 : Confirmation par saisie du mot EFFACER
    if (dialogueEtape2) {
        AlertDialog(
            onDismissRequest = { dialogueEtape2 = false },
            title = { Text("Confirmation définitive", fontWeight = FontWeight.Bold, color = Rouge) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Pour valider la suppression totale, saisis le mot EFFACER ci-dessous :",
                        fontSize = 13.sp, color = TexteFonce
                    )
                    DonakaTextField(
                        value = motConfirmation,
                        onValueChange = { motConfirmation = it },
                        label = "Saisir EFFACER"
                    )
                }
            },
            confirmButton = {
                DonakaButton(
                    texte = "Valider et effacer",
                    onClick = {
                        dialogueEtape2 = false
                        vm.effacerTout(onSuccess = onRetour)
                    },
                    style = StyleBouton.DANGER,
                    enabled = motConfirmation.trim() == "EFFACER"
                )
            },
            dismissButton = {
                DonakaButton(
                    texte = "Annuler",
                    onClick = { dialogueEtape2 = false },
                    style = StyleBouton.TEXTE
                )
            },
            containerColor = SurfaceBlanche,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
