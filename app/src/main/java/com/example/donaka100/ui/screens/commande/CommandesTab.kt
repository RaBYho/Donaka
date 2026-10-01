package com.example.donaka100.ui.screens.commande

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.donaka100.data.Commande
import com.example.donaka100.ui.CommandeUiState
import com.example.donaka100.ui.components.*
import com.example.donaka100.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val formatHeure = DateTimeFormatter.ofPattern("HH:mm")
private val formatJour = DateTimeFormatter.ofPattern("dd/MM")

private fun pluriel(n: Int, mot: String) = if (n > 1) "${mot}s" else mot

@Composable
fun CommandesTab(
    etat: CommandeUiState,
    modifier: Modifier,
    onNonPrevu: () -> Unit,
    onCommandeDemain: () -> Unit,
    onLivrer: (Commande) -> Unit,
    onModifier: (Commande) -> Unit,
    onSupprimer: (Commande) -> Unit,
    onAnnuler: (Commande) -> Unit,
    onRattacher: (Commande) -> Unit,
    onAjouterDemain: (String) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        userScrollEnabled = !etat.isLoading
    ) {
        item {
            DonakaButton("Client non prévu", onNonPrevu, icone = Icons.Default.PersonAdd, pleineLargeur = true)
        }
        item { LivraisonsBanner(etat) }

        if (etat.isLoading) {
            items(2) { DonakaCard(isLoading = true) {} }
        } else {
            item { CarteALivrer(etat, onLivrer, onModifier, onSupprimer) }
            item {
                DonakaButton(
                    "Prendre une commande pour demain", onCommandeDemain,
                    style = StyleBouton.SECONDAIRE, icone = Icons.Default.EditNote, pleineLargeur = true
                )
            }

            item {
                Text(
                    "Livraisons terminées (${etat.livrees.size})",
                    fontWeight = FontWeight.SemiBold, color = TexteFonce
                )
            }
            if (etat.livrees.isEmpty()) {
                item { Text("Aucune livraison terminée pour l'instant", fontSize = 13.sp, color = TexteGris) }
            } else {
                items(etat.livrees, key = { it.id }) { c ->
                    CarteLivree(
                        c = c,
                        demain = c.clientId?.let { etat.commandeDemainDe(it) },
                        onAnnuler = { onAnnuler(c) },
                        onRattacher = { onRattacher(c) },
                        onModifierDemain = onModifier,
                        onAjouterDemain = onAjouterDemain
                    )
                }
            }

            item { CarteDemain(etat, onModifier, onSupprimer) }
        }
    }
}

// ---------- Bandeau ----------

@Composable
private fun LivraisonsBanner(etat: CommandeUiState) {
    val livrees = etat.livrees.size
    val restantes = etat.aLivrer.size
    val prevues = livrees + restantes

    DonakaCard(isLoading = etat.isLoading) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocalShipping, null, tint = Primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(6.dp))
            Text("Livraisons aujourd'hui", fontWeight = FontWeight.Bold, color = TexteFonce)
        }
        DonakaProgressBar(
            progression = if (prevues == 0) 0f else livrees.toFloat() / prevues,
            titre = "$livrees ${pluriel(livrees, "livrée")} sur $prevues",
            valeurTexte = "$restantes ${pluriel(restantes, "restante")}",
            couleur = Vert
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Stat("Valeur livrée", etat.valeurLivreeAujourdhui, TexteFonce, Modifier.weight(1f))
            Stat("Encaissé", etat.totalEncaisseAujourdhui, Vert, Modifier.weight(1f))
        }
    }
}

@Composable
private fun Stat(titre: String, valeur: Long, couleur: androidx.compose.ui.graphics.Color, modifier: Modifier) {
    Column(modifier) {
        Text(titre, fontSize = 12.sp, color = TexteGris)
        Text(valeur.enMGA(), fontWeight = FontWeight.Bold, color = couleur)
    }
}

// ---------- À livrer ----------

@Composable
private fun CarteALivrer(
    etat: CommandeUiState,
    onLivrer: (Commande) -> Unit,
    onModifier: (Commande) -> Unit,
    onSupprimer: (Commande) -> Unit
) {
    val liste = etat.aLivrer
    val auj = LocalDate.now()

    DonakaCard {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("À livrer aujourd'hui", fontWeight = FontWeight.Bold, color = TexteFonce)
                Text("Coche la case une fois livré", fontSize = 12.sp, color = TexteGris)
            }
            DonakaBadge("${liste.size} ${pluriel(liste.size, "restant")}", type = TypeBadge.INFO)
        }

        if (liste.isEmpty()) {
            Text("Rien à livrer pour l'instant", fontSize = 13.sp, color = TexteGris)
        } else {
            liste.forEachIndexed { i, c ->
                if (i > 0) HorizontalDivider(color = SurfaceMoyenne)
                LigneALivrer(
                    c = c, enRetard = c.date < auj,
                    onLivrer = { onLivrer(c) },
                    onModifier = { onModifier(c) },
                    onSupprimer = { onSupprimer(c) }
                )
            }
        }
    }
}

@Composable
private fun LigneALivrer(
    c: Commande,
    enRetard: Boolean,
    onLivrer: () -> Unit,
    onModifier: () -> Unit,
    onSupprimer: () -> Unit
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onLivrer) {
            Icon(
                Icons.Default.RadioButtonUnchecked,
                contentDescription = "Marquer livré : ${c.clientNom}",
                tint = Primary, modifier = Modifier.size(28.dp)
            )
        }
        Column(Modifier.weight(1f)) {
            Text(
                c.clientNom, fontWeight = FontWeight.Bold, color = TexteFonce,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            Text(c.resume, fontSize = 12.sp, color = TexteGris, maxLines = 2, overflow = TextOverflow.Ellipsis)
            val infos = buildList {
                if (enRetard) add("En retard · ${c.date.format(formatJour)}")
                if (c.heure.isNotBlank()) add(c.heure)
            }
            if (infos.isNotEmpty()) {
                Text(
                    infos.joinToString(" • "), fontSize = 11.sp, fontWeight = FontWeight.Medium,
                    color = if (enRetard) Rouge else Ambre
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(c.total.enMGA(), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Primary)
        DonakaOverflowMenu(
            listOf(
                ActionMenu("Modifier", Icons.Default.Edit) { onModifier() },
                ActionMenu("Supprimer", Icons.Default.Delete, danger = true) { onSupprimer() }
            )
        )
    }
}

// ---------- Livrée ----------

@Composable
private fun CarteLivree(
    c: Commande,
    demain: Commande?,
    onAnnuler: () -> Unit,
    onRattacher: () -> Unit,
    onModifierDemain: (Commande) -> Unit,
    onAjouterDemain: (String) -> Unit
) {
    DonakaCard {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(VertClair.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.Check, null, tint = Vert, modifier = Modifier.size(20.dp)) }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    c.clientNom, fontWeight = FontWeight.Bold, color = TexteFonce,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                DonakaBadge("Livré ${c.livreeA?.format(formatHeure).orEmpty()}", type = TypeBadge.SUCCES)
            }

            DonakaOverflowMenu(
                buildList {
                    if (c.nonPrevu) add(ActionMenu("Ajouter comme nouveau client", Icons.Default.PersonAdd) { onRattacher() })
                    add(ActionMenu("Annuler la livraison", Icons.Default.Cancel, danger = true) { onAnnuler() })
                }
            )
        }

        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(SurfaceMoyenne.copy(alpha = 0.5f))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(c.resume, Modifier.weight(1f), fontSize = 13.sp, color = TexteFonce)
                Spacer(Modifier.width(8.dp))
                Text(c.total.enMGA(), fontWeight = FontWeight.Bold, color = TexteFonce)
            }
            LignePaiement(c)
        }

        if (c.clientId == null) {
            DonakaBadge("Client non prévu", type = TypeBadge.NEUTRE)
            DonakaButton(
                "Ajouter comme nouveau client", onRattacher,
                style = StyleBouton.CONTOUR, icone = Icons.Default.PersonAdd, pleineLargeur = true
            )
        } else {
            ChipDemain(
                demain = demain,
                onClick = { if (demain != null) onModifierDemain(demain) else onAjouterDemain(c.clientId) }
            )
        }
    }
}

@Composable
private fun LignePaiement(c: Commande) {
    when {
        c.resteACredit <= 0 -> Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Payments, null, tint = Vert, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Payé · ${c.mode?.libelle.orEmpty()}", fontSize = 12.sp, color = Vert, fontWeight = FontWeight.Medium)
        }

        c.montantPaye <= 0 ->
            DonakaBadge("À CRÉDIT · ${c.resteACredit.enMGA()}", type = TypeBadge.ALERTE)

        else -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "Payé ${c.montantPaye.enMGA()} · ${c.mode?.libelle.orEmpty()}",
                fontSize = 12.sp, color = Vert, fontWeight = FontWeight.Medium
            )
            DonakaBadge("À crédit : ${c.resteACredit.enMGA()}", type = TypeBadge.ALERTE)
        }
    }
}

@Composable
private fun ChipDemain(demain: Commande?, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(SurfaceMoyenne.copy(alpha = 0.5f))
            .padding(start = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (demain != null) Icons.Default.EventAvailable else Icons.Default.Event, null,
            tint = if (demain != null) Vert else TexteGris, modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            if (demain != null) "Demain : ${demain.nbArticles} art. (${demain.resume})"
            else "Pas de commande pour demain",
            Modifier.weight(1f), fontSize = 12.sp, color = TexteFonce, maxLines = 2, overflow = TextOverflow.Ellipsis
        )
        TextButton(onClick = onClick) {
            Text(if (demain != null) "Modifier" else "Ajouter", color = Primary, fontWeight = FontWeight.Bold)
        }
    }
}

// ---------- Commandes de demain ----------

@Composable
private fun CarteDemain(
    etat: CommandeUiState,
    onModifier: (Commande) -> Unit,
    onSupprimer: (Commande) -> Unit
) {
    val liste = etat.commandesDemain
    val pieces = liste.sumOf { it.nbArticles }

    DonakaCard {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Commandes de demain", fontWeight = FontWeight.Bold, color = TexteFonce)
            DonakaBadge("$pieces ${pluriel(pieces, "pièce")}", type = TypeBadge.INFO)
        }
        if (liste.isEmpty()) {
            Text("Aucune commande pour demain", fontSize = 13.sp, color = TexteGris)
        } else {
            liste.forEachIndexed { i, c ->
                if (i > 0) HorizontalDivider(color = SurfaceMoyenne)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            c.clientNom, fontWeight = FontWeight.SemiBold, color = TexteFonce,
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                        Text(c.resume, fontSize = 12.sp, color = TexteGris, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        if (c.heure.isNotBlank()) Text(c.heure, fontSize = 11.sp, color = Ambre)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(c.total.enMGA(), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Primary)
                    DonakaOverflowMenu(
                        listOf(
                            ActionMenu("Modifier", Icons.Default.Edit) { onModifier(c) },
                            ActionMenu("Supprimer", Icons.Default.Delete, danger = true) { onSupprimer(c) }
                        )
                    )
                }
            }
        }
    }
}