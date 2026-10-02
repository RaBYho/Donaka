package com.example.donaka100.ui.screens.stock

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.donaka100.data.Ingredient
import com.example.donaka100.data.StatutStock
import com.example.donaka100.ui.FiltreStock
import com.example.donaka100.ui.StockUiState
import com.example.donaka100.ui.components.*
import com.example.donaka100.ui.theme.*

@Composable
fun InventaireTab(
    etat: StockUiState,
    modifier: Modifier,
    onAchat: () -> Unit,
    onNouveau: () -> Unit,
    onRecherche: (String) -> Unit,
    onFiltre: (FiltreStock) -> Unit,
    onCommander: (Ingredient) -> Unit,
    onAjuster: (Ingredient) -> Unit,
    onModifier: (Ingredient) -> Unit,
    onSupprimer: (Ingredient) -> Unit
) {
    val liste = etat.ingredientsAffiches
    val critiques = etat.critiques

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
                    "Effectuer un Achat", onAchat,
                    icone = Icons.Default.AddShoppingCart, modifier = Modifier.weight(1f)
                )
                DonakaIconButton(Icons.Default.Add, "Nouvel ingrédient", onNouveau)
            }
        }

        when {
            etat.isLoading -> items(3) { DonakaCard(isLoading = true) {} }

            etat.ingredients.isEmpty() -> item {
                MessageVide(
                    Icons.Default.Inventory2, "Aucun ingrédient",
                    "Ils s'ajoutent tout seuls quand tu écris une recette, ou avec le bouton +."
                )
            }

            else -> {
                item {
                    DonakaSearchBar(
                        value = etat.recherche, onValueChange = onRecherche,
                        placeholder = "Rechercher…"
                    )
                }
                item {
                    val options = buildList<FiltreStock> {
                        add(FiltreStock.Tout)
                        add(FiltreStock.Alertes)
                        if (etat.aRenseigner.isNotEmpty()) add(FiltreStock.ARenseigner)
                        etat.rayons.forEach { add(FiltreStock.Rayon(it)) }
                    }
                    DonakaFilterChips(
                        options = options,
                        selected = etat.filtreEffectif,
                        onSelect = onFiltre,
                        label = {
                            when (it) {
                                FiltreStock.Tout -> "Tout (${etat.ingredients.size})"
                                FiltreStock.Alertes -> "Alertes (${critiques.size})"
                                FiltreStock.ARenseigner -> "À renseigner (${etat.aRenseigner.size})"
                                is FiltreStock.Rayon -> it.nom
                            }
                        }
                    )
                }
                if (critiques.isNotEmpty()) {
                    item { BanniereCritique(critiques, onVoir = { onFiltre(FiltreStock.Alertes) }) }
                }

                if (liste.isEmpty()) {
                    item {
                        MessageVide(
                            Icons.Default.SearchOff, "Aucune matière trouvée",
                            "Vérifie l'orthographe ou change de filtre."
                        )
                    }
                } else {
                    items(liste, key = { it.id }) { i ->
                        StockCard(
                            i = i, reserve = etat.reserveJours(i),
                            onCommander = { onCommander(i) }, onAjuster = { onAjuster(i) },
                            onModifier = { onModifier(i) }, onSupprimer = { onSupprimer(i) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BanniereCritique(critiques: List<Ingredient>, onVoir: () -> Unit) {
    val n = critiques.size
    DonakaCard(containerColor = RougeClair) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.NotificationImportant, null, tint = Rouge, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "$n seuil${if (n > 1) "s" else ""} critique${if (n > 1) "s" else ""} atteint${if (n > 1) "s" else ""}",
                    fontWeight = FontWeight.Bold, color = Rouge
                )
                Text(
                    critiques.joinToString(", ") { it.nom } + " sous le quota de sécurité",
                    fontSize = 12.sp, color = Rouge
                )
            }
            DonakaButton("Voir ($n)", onVoir, style = StyleBouton.TEXTE)
        }
    }
}

@Composable
private fun StockCard(
    i: Ingredient,
    reserve: Int?,
    onCommander: () -> Unit,
    onAjuster: () -> Unit,
    onModifier: () -> Unit,
    onSupprimer: () -> Unit
) {
    val statut = i.statut
    val couleur: Color = when (statut) {
        StatutStock.CRITIQUE -> Rouge
        StatutStock.SEUIL_JUSTE -> Ambre
        StatutStock.OK -> Vert
        StatutStock.A_RENSEIGNER -> TexteGris
    }
    val typeBadge = when (statut) {
        StatutStock.CRITIQUE -> TypeBadge.ERREUR
        StatutStock.SEUIL_JUSTE -> TypeBadge.ALERTE
        StatutStock.OK -> TypeBadge.SUCCES
        StatutStock.A_RENSEIGNER -> TypeBadge.NEUTRE
    }
    // La barre pleine = 3 × le seuil
    val niveau = if (i.seuil > 0) (i.quantite / (i.seuil * 3)).toFloat().coerceIn(0f, 1f) else 0f

    val phrase = when {
        statut == StatutStock.A_RENSEIGNER -> "Définis le seuil d'alerte (et la quantité si besoin)"
        reserve == null -> when (statut) {
            StatutStock.CRITIQUE -> "Sous le seuil de sécurité"
            StatutStock.SEUIL_JUSTE -> "Proche du seuil"
            else -> "Stock suffisant"
        }
        reserve == 0 -> "Réserve : moins d'une production"
        else -> "Réserve ≈ $reserve jour${if (reserve > 1) "s" else ""} de production"
    }

    DonakaCard {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(i.nom, fontWeight = FontWeight.Bold, color = TexteFonce)
                val infos = listOf(i.fournisseur.trim(), i.rayon.trim()).filter { it.isNotEmpty() }
                if (infos.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storefront, null, tint = TexteGris, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(infos.joinToString(" • "), fontSize = 12.sp, color = TexteGris)
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            DonakaBadge(statut.libelle, type = typeBadge)
        }

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
            Text(i.quantite.enQuantite(), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = couleur)
            Spacer(Modifier.width(4.dp))
            Text(
                i.unite.libelle, color = couleur, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Spacer(Modifier.weight(1f))
            if (i.seuil > 0) {
                Text("Seuil : ${i.seuil.avecUnite(i.unite)}", fontSize = 12.sp, color = TexteGris)
            }
        }

        if (statut != StatutStock.A_RENSEIGNER) {
            DonakaProgressBar(progression = niveau, couleur = couleur)
        }

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                phrase, Modifier.weight(1f), fontSize = 12.sp,
                color = if (statut == StatutStock.CRITIQUE) Rouge else TexteGris,
                fontWeight = if (statut == StatutStock.CRITIQUE) FontWeight.Medium else FontWeight.Normal
            )
            Spacer(Modifier.width(8.dp))
            when (statut) {
                StatutStock.CRITIQUE ->
                    DonakaButton("Commander", onCommander, icone = Icons.Default.ShoppingCart)
                StatutStock.A_RENSEIGNER ->
                    DonakaButton("Renseigner", onModifier, style = StyleBouton.SECONDAIRE, icone = Icons.Default.Edit)
                else ->
                    DonakaButton("Ajuster", onAjuster, style = StyleBouton.SECONDAIRE, icone = Icons.Default.Tune)
            }
            DonakaOverflowMenu(
                buildList {
                    if (statut == StatutStock.SEUIL_JUSTE) {
                        add(ActionMenu("Commander", Icons.Default.ShoppingCart) { onCommander() })
                    }
                    if (statut == StatutStock.CRITIQUE || statut == StatutStock.A_RENSEIGNER) {
                        add(ActionMenu("Ajuster", Icons.Default.Tune) { onAjuster() })
                    }
                    add(ActionMenu("Modifier", Icons.Default.Edit) { onModifier() })
                    add(ActionMenu("Supprimer", Icons.Default.Delete, danger = true) { onSupprimer() })
                }
            )
        }
    }
}

@Composable
private fun MessageVide(icone: androidx.compose.ui.graphics.vector.ImageVector, titre: String, message: String) {
    Column(
        Modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icone, null, tint = TexteGris.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(8.dp))
        Text(titre, fontWeight = FontWeight.SemiBold, color = TexteFonce)
        Text(message, color = TexteGris, textAlign = TextAlign.Center, fontSize = 13.sp)
    }
}