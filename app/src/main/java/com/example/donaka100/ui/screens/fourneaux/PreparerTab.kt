package com.example.donaka100.ui.screens.fourneaux

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.donaka100.data.Besoin
import com.example.donaka100.data.Fournee
import com.example.donaka100.ui.FourneauxUiState
import com.example.donaka100.ui.LignePlan
import com.example.donaka100.ui.components.*
import com.example.donaka100.ui.theme.*
import java.time.format.DateTimeFormatter

private val formatHeure = DateTimeFormatter.ofPattern("HH:mm")

@Composable
fun PreparerTab(
    etat: FourneauxUiState,
    modifier: Modifier,
    onLancer: () -> Unit,
    onAnnuler: (Fournee) -> Unit,
    onVoirRecettes: () -> Unit
) {
    val plan = etat.plan
    val besoins = etat.besoinsPlan
    val manquants = besoins.filterNot { it.suffisant }
    val fournee = etat.fourneeDemain

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        userScrollEnabled = !etat.isLoading
    ) {
        // 1. Production requise
        item {
            DonakaCard(isLoading = etat.isLoading) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("PRODUCTION REQUISE", fontSize = 11.sp, color = TexteGris, fontWeight = FontWeight.SemiBold)
                        Text("Commandes Clients Renseignées", fontWeight = FontWeight.Bold, color = TexteFonce)
                    }
                    DonakaBadge("${plan.size} Produit${if (plan.size > 1) "s" else ""}", type = TypeBadge.NEUTRE)
                }
                if (plan.isEmpty()) {
                    Text("Aucune commande pour demain", fontSize = 13.sp, color = TexteGris)
                } else {
                    plan.chunked(3).forEach { rangee ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rangee.forEach { Tuile(it, Modifier.weight(1f)) }
                            repeat(3 - rangee.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Sync, null, tint = Primary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Agrégé automatiquement à partir des commandes de demain.",
                        fontSize = 12.sp, color = TexteGris
                    )
                }
            }
        }

        if (!etat.isLoading) {
            // 2. Recettes manquantes
            if (etat.produitsSansRecette.isNotEmpty()) {
                item {
                    DonakaCard(containerColor = AmbreClair) {
                        Text("Recette manquante", fontWeight = FontWeight.Bold, color = Ambre)
                        Text(
                            etat.produitsSansRecette.joinToString(", ") { it.nom } +
                                    " : les ingrédients ne sont pas calculés.",
                            fontSize = 13.sp, color = Ambre
                        )
                        DonakaButton("Voir les recettes", onVoirRecettes, style = StyleBouton.TEXTE)
                    }
                }
            }

            // 3. Matières premières
            item {
                DonakaCard {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Matières Premières", fontWeight = FontWeight.Bold, color = TexteFonce)
                            Text("Recettes appliquées aux commandes", fontSize = 12.sp, color = TexteGris)
                        }
                        DonakaBadge("Calculé", type = TypeBadge.NEUTRE)
                    }

                    if (besoins.isEmpty()) {
                        Text("Aucun ingrédient à calculer pour l'instant", fontSize = 13.sp, color = TexteGris)
                    } else {
                        besoins.forEach { LigneBesoin(it) }
                    }

                    if (manquants.isNotEmpty()) {
                        Column(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(RougeClair).padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Error, null, tint = Rouge, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Déficit détecté", fontWeight = FontWeight.Bold, color = Rouge)
                            }
                            manquants.forEach {
                                Text(
                                    "${it.manque.avecUnite(it.ingredient.unite)} de ${it.ingredient.nom} manquant",
                                    fontSize = 13.sp, color = Rouge
                                )
                            }
                            Text(
                                "Impossible d'honorer toute la production sans réapprovisionnement.",
                                fontSize = 12.sp, color = Rouge
                            )
                        }
                    }
                }
            }

            // 4. Action
            item {
                if (fournee == null) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        DonakaButton(
                            "Lancer & Valider la Production", onLancer,
                            icone = Icons.Default.RocketLaunch,
                            enabled = plan.isNotEmpty(), pleineLargeur = true
                        )
                        Text(
                            "Déduit automatiquement les matières premières du stock lors de la validation.",
                            fontSize = 12.sp, color = TexteGris, textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    CarteValidee(etat, fournee, onAnnuler)
                }
            }
        }
    }
}

@Composable
private fun Tuile(l: LignePlan, modifier: Modifier) {
    Column(
        modifier
            .heightIn(min = 110.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceMoyenne.copy(alpha = 0.5f))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            Modifier.size(32.dp).clip(CircleShape).background(PrimaireClair),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Default.BakeryDining, null, tint = Primary, modifier = Modifier.size(18.dp)) }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(l.quantite.toString(), fontSize = 26.sp, fontWeight = FontWeight.Bold, color = TexteFonce)
            Text("Unités", fontSize = 11.sp, color = TexteGris)
        }
        Text(
            l.nom, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TexteFonce,
            maxLines = 1, overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun LigneBesoin(b: Besoin) {
    val ok = b.suffisant
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            .background(if (ok) SurfaceMoyenne.copy(alpha = 0.5f) else RougeClair.copy(alpha = 0.6f))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(SurfaceBlanche),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Default.Inventory2, null, tint = if (ok) Primary else Rouge, modifier = Modifier.size(20.dp)) }

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(
                b.ingredient.nom, fontWeight = FontWeight.Bold, color = TexteFonce,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            Text(
                "Dispo : ${b.ingredient.quantite.avecUnite(b.ingredient.unite)}",
                fontSize = 12.sp, color = if (ok) TexteGris else Rouge
            )
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                b.requis.avecUnite(b.ingredient.unite), fontWeight = FontWeight.Bold,
                color = if (ok) TexteFonce else Rouge
            )
            DonakaBadge(
                if (ok) "Suffisant" else "Insuffisant",
                type = if (ok) TypeBadge.SUCCES else TypeBadge.ERREUR
            )
        }
    }
}

@Composable
private fun CarteValidee(etat: FourneauxUiState, f: Fournee, onAnnuler: (Fournee) -> Unit) {
    // Commandes ajoutées après la validation : la production ne les couvre plus
    val enPlus = etat.plan.filter { p ->
        p.quantite > (f.lignes.firstOrNull { it.produitId == p.produitId }?.quantite ?: 0)
    }

    DonakaCard(containerColor = VertClair.copy(alpha = 0.3f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CheckCircle, null, tint = Vert, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(8.dp))
            Column {
                Text("Production validée", fontWeight = FontWeight.Bold, color = Vert)
                Text("à ${f.heure.format(formatHeure)} · ${f.totalPieces} pièces", fontSize = 12.sp, color = TexteGris)
            }
        }
        Text(
            f.lignes.joinToString(", ") { "${it.quantite}× ${it.nom}" },
            fontSize = 13.sp, color = TexteFonce
        )
        if (enPlus.isNotEmpty()) {
            Text(
                "Commandes ajoutées depuis la validation : " + enPlus.joinToString(", ") { it.nom },
                fontSize = 12.sp, color = Ambre, fontWeight = FontWeight.Medium
            )
        }
        DonakaButton(
            "Annuler cette fournée", { onAnnuler(f) },
            style = StyleBouton.CONTOUR, icone = Icons.Default.Undo, pleineLargeur = true
        )
    }
}