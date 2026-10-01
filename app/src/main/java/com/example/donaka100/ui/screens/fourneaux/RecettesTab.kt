package com.example.donaka100.ui.screens.fourneaux

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.donaka100.data.Ingredient
import com.example.donaka100.data.Produit
import com.example.donaka100.ui.FourneauxUiState
import com.example.donaka100.ui.TriProduit
import com.example.donaka100.ui.components.*
import com.example.donaka100.ui.theme.*

@Composable
fun RecettesTab(
    etat: FourneauxUiState,
    modifier: Modifier,
    onNouveau: () -> Unit,
    onModifier: (Produit) -> Unit,
    onSupprimer: (Produit) -> Unit,
    onRecherche: (String) -> Unit,
    onCategorie: (String?) -> Unit,
    onTri: (TriProduit) -> Unit
) {
    val liste = etat.produitsAffiches
    val filtreActif = etat.rechercheProduit.isNotBlank() || etat.categorieFiltre != null

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        userScrollEnabled = !etat.isLoading
    ) {
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DonakaButton(
                    "Ajouter un Produit", onNouveau,
                    icone = Icons.Default.AddCircle, modifier = Modifier.weight(1f)
                )
                MenuTri(etat.tri, onTri)
            }
        }
        item {
            DonakaSearchBar(
                value = etat.rechercheProduit, onValueChange = onRecherche,
                placeholder = "Rechercher un produit, formule…"
            )
        }
        if (etat.categories.isNotEmpty()) {
            item {
                DonakaFilterChips(
                    options = listOf<String?>(null) + etat.categories,
                    selected = etat.categorieFiltre?.takeIf { it in etat.categories },
                    onSelect = onCategorie,
                    label = { it ?: "Tous (${etat.produitsActifs.size})" }
                )
            }
        }

        when {
            etat.isLoading -> items(3) { DonakaCard(isLoading = true) {} }

            liste.isEmpty() -> item {
                Column(
                    Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        if (filtreActif) Icons.Default.SearchOff else Icons.Default.MenuBook,
                        null, tint = TexteGris.copy(alpha = 0.5f), modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (filtreActif) "Aucun résultat" else "Aucun produit",
                        fontWeight = FontWeight.SemiBold, color = TexteFonce
                    )
                    Text(
                        if (filtreActif) "Essaie une autre recherche ou une autre catégorie."
                        else "Ajoute tes produits avec leurs prix et leur recette par lot.",
                        color = TexteGris, textAlign = TextAlign.Center, fontSize = 13.sp
                    )
                }
            }

            else -> items(liste, key = { it.id }) { p ->
                ProduitCard(p, etat.ingredients, { onModifier(p) }, { onSupprimer(p) })
            }
        }
    }
}

@Composable
private fun MenuTri(tri: TriProduit, onTri: (TriProduit) -> Unit) {
    var ouvert by remember { mutableStateOf(false) }
    Box {
        DonakaIconButton(Icons.Default.Tune, "Trier les produits", { ouvert = true })
        DropdownMenu(expanded = ouvert, onDismissRequest = { ouvert = false }) {
            TriProduit.entries.forEach { t ->
                DropdownMenuItem(
                    text = {
                        Text(t.libelle, fontWeight = if (t == tri) FontWeight.Bold else FontWeight.Normal)
                    },
                    trailingIcon = {
                        if (t == tri) Icon(Icons.Default.Check, null, tint = Primary)
                    },
                    onClick = { ouvert = false; onTri(t) }
                )
            }
        }
    }
}

private fun Produit.icone(): ImageVector {
    val c = categorie.lowercase()
    val n = nom.lowercase()
    return when {
        c.contains("pât") || n.contains("gâteau") -> Icons.Default.Cake
        c.contains("viennois") || n.contains("croissant") || n.contains("brioche") -> Icons.Default.BreakfastDining
        else -> Icons.Default.BakeryDining
    }
}

@Composable
private fun ProduitCard(
    p: Produit,
    ingredients: List<Ingredient>,
    onModifier: () -> Unit,
    onSupprimer: () -> Unit
) {
    DonakaCard {
        // En-tête : icône, nom, catégorie, prix
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Box(
                Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(PrimaireClair),
                contentAlignment = Alignment.Center
            ) { Icon(p.icone(), null, tint = Primary, modifier = Modifier.size(26.dp)) }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    p.nom, fontWeight = FontWeight.Bold, color = TexteFonce,
                    maxLines = 2, overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (p.categorie.isNotBlank()) DonakaBadge(p.categorie.trim(), type = TypeBadge.NEUTRE)
                    if (p.piecesParLot > 0) Text("Lot de ${p.piecesParLot} pcs", fontSize = 11.sp, color = TexteGris)
                }
            }

            Spacer(Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(p.prixGros.enMGA(), fontWeight = FontWeight.Bold, color = Primary)
                Text("l'unité (gros)", fontSize = 11.sp, color = TexteGris)
                Text("Public ${p.prixPublic.enMGA()}", fontSize = 11.sp, color = TexteGris)
            }
        }

        // Pivot et résumé de la recette
        val pivot = p.pivot
        if (p.aRecette && pivot != null) {
            val ingPivot = ingredients.firstOrNull { it.id == pivot.ingredientId }
            val autres = p.recette.filter { it.ingredientId != pivot.ingredientId }

            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                    .background(AmbreClair.copy(alpha = 0.5f)).padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Star, null, tint = Ambre, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "Pivot : ${ingPivot?.nom ?: "ingrédient supprimé"}",
                        fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Ambre
                    )
                    if (autres.isNotEmpty()) {
                        Text(
                            "+ " + autres.joinToString(", ") { l ->
                                val ing = ingredients.firstOrNull { it.id == l.ingredientId }
                                if (ing != null) "${ing.nom} (${l.quantiteParLot.avecUnite(ing.unite)})"
                                else "ingrédient supprimé"
                            },
                            fontSize = 11.sp, color = TexteGris, maxLines = 2, overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                if (ingPivot != null) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        pivot.quantiteParLot.avecUnite(ingPivot.unite),
                        fontWeight = FontWeight.Bold, color = Ambre
                    )
                }
            }
        } else {
            DonakaBadge("Recette manquante", type = TypeBadge.ALERTE)
        }

        HorizontalDivider(color = SurfaceMoyenne)

        // Pied : marge revendeur (le coût de revient viendra avec Stock), actions
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Marge revendeur : ${p.margeRevendeur.enMGA()}",
                Modifier.weight(1f), fontSize = 12.sp, color = TexteGris
            )
            DonakaButton("Modifier", onModifier, style = StyleBouton.SECONDAIRE, icone = Icons.Default.EditNote)
            DonakaOverflowMenu(
                listOf(ActionMenu("Supprimer", Icons.Default.Delete, danger = true) { onSupprimer() })
            )
        }
    }
}