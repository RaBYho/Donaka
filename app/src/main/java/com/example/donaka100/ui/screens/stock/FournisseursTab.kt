package com.example.donaka100.ui.screens.stock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.donaka100.data.cleNom
import com.example.donaka100.ui.Fournisseur
import com.example.donaka100.ui.StockUiState
import com.example.donaka100.ui.components.*
import com.example.donaka100.ui.theme.*
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

private val formatDate = DateTimeFormatter.ofPattern("d MMM", Locale.FRENCH)

private fun initiales(nom: String): String {
    val tous = nom.split(" ").filter { it.isNotBlank() }
    val mots = tous.filter { it.length > 2 }.ifEmpty { tous }
    return when {
        mots.isEmpty() -> "?"
        mots.size == 1 -> mots[0].take(2).uppercase()
        else -> "${mots[0].first()}${mots[1].first()}".uppercase()
    }
}

/** Une couleur stable par fournisseur */
private fun couleurs(nom: String): Pair<Color, Color> {
    val palette = listOf(
        PrimaireClair to Primary,
        SurfaceMoyenne to TexteFonce,
        AmbreClair to Ambre,
        VertClair.copy(alpha = 0.5f) to Vert
    )
    return palette[abs(nom.cleNom().hashCode()) % palette.size]
}

@Composable
fun FournisseursTab(
    etat: StockUiState,
    modifier: Modifier,
    onNouveau: () -> Unit,
    onRecherche: (String) -> Unit,
    onRayon: (String?) -> Unit,
    onAppeler: (Fournisseur) -> Unit,
    onCommander: (Fournisseur) -> Unit,
    onModifier: (Fournisseur) -> Unit,
    onVoirAchats: (Fournisseur) -> Unit,
    onSupprimerFiche: (Fournisseur) -> Unit
) {
    val tous = etat.listeFournisseurs
    val liste = etat.fournisseursAffiches

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        userScrollEnabled = !etat.isLoading
    ) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Indicateur("Fournisseurs", tous.size.toString(), TexteFonce, etat.isLoading, Modifier.weight(1f))
                Indicateur("Dépensé ce mois", etat.depenseMois.enMGA(), Rouge, etat.isLoading, Modifier.weight(1f))
            }
        }
        item {
            DonakaButton("Nouveau Fournisseur", onNouveau, icone = Icons.Default.PersonAdd, pleineLargeur = true)
        }

        when {
            etat.isLoading -> items(3) { DonakaCard(isLoading = true) {} }

            tous.isEmpty() -> item {
                Vide(
                    Icons.Default.Storefront, "Aucun fournisseur",
                    "Ils apparaissent quand tu en renseignes un dans un ingrédient ou un achat, " +
                            "ou avec le bouton ci-dessus."
                )
            }

            else -> {
                item {
                    DonakaSearchBar(
                        value = etat.rechercheFournisseur, onValueChange = onRecherche,
                        placeholder = "Rechercher un fournisseur, un ingrédient…"
                    )
                }
                if (etat.rayonsFournisseurs.isNotEmpty()) {
                    item {
                        DonakaFilterChips(
                            options = listOf<String?>(null) + etat.rayonsFournisseurs,
                            selected = etat.rayonFournisseur?.takeIf { it in etat.rayonsFournisseurs },
                            onSelect = onRayon,
                            label = { it ?: "Tous (${tous.size})" }
                        )
                    }
                }
                if (liste.isEmpty()) {
                    item { Vide(Icons.Default.SearchOff, "Aucun fournisseur trouvé", "Essaie une autre recherche ou un autre rayon.") }
                } else {
                    items(liste, key = { it.nom.cleNom() }) { f ->
                        CarteFournisseur(
                            f,
                            onAppeler = { onAppeler(f) }, onCommander = { onCommander(f) },
                            onModifier = { onModifier(f) }, onVoirAchats = { onVoirAchats(f) },
                            onSupprimerFiche = { onSupprimerFiche(f) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Indicateur(titre: String, valeur: String, couleur: Color, isLoading: Boolean, modifier: Modifier) {
    DonakaCard(modifier = modifier, isLoading = isLoading) {
        Text(titre.uppercase(), fontSize = 11.sp, color = TexteGris, fontWeight = FontWeight.SemiBold)
        Text(valeur, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = couleur, maxLines = 1)
    }
}

@Composable
private fun CarteFournisseur(
    f: Fournisseur,
    onAppeler: () -> Unit,
    onCommander: () -> Unit,
    onModifier: () -> Unit,
    onVoirAchats: () -> Unit,
    onSupprimerFiche: () -> Unit
) {
    val (fond, texte) = couleurs(f.nom)
    val dernier = f.dernierAchat

    DonakaCard {
        // En-tête
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Box(
                Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(fond),
                contentAlignment = Alignment.Center
            ) { Text(initiales(f.nom), fontWeight = FontWeight.Bold, color = texte) }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    f.nom, fontWeight = FontWeight.Bold, color = TexteFonce,
                    maxLines = 2, overflow = TextOverflow.Ellipsis
                )
                val rayon = f.rayonPrincipal
                if (rayon != null || f.fiche == null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (rayon != null) DonakaBadge(rayon, type = TypeBadge.NEUTRE)
                        if (f.fiche == null) DonakaBadge("Fiche à compléter", type = TypeBadge.ALERTE)
                    }
                }
                if (f.adresse.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = TexteGris, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(f.adresse, fontSize = 12.sp, color = TexteGris, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
            }

            DonakaOverflowMenu(
                buildList {
                    add(
                        ActionMenu(if (f.fiche == null) "Compléter la fiche" else "Modifier", Icons.Default.Edit) {
                            onModifier()
                        }
                    )
                    if (f.achats.isNotEmpty()) {
                        add(ActionMenu("Voir les achats", Icons.AutoMirrored.Filled.ReceiptLong) { onVoirAchats() })
                    }
                    add(ActionMenu("Supprimer le fournisseur", Icons.Default.Delete, danger = true) { onSupprimerFiche() })
                }
            )
        }

        // Contact
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                .background(SurfaceMoyenne.copy(alpha = 0.5f)).padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (f.telephone.isBlank() && f.delai.isBlank()) {
                Text("Aucun contact renseigné", fontSize = 12.sp, color = TexteGris)
            } else {
                if (f.telephone.isNotBlank()) {
                    Icon(Icons.Default.Call, null, tint = Primary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(f.telephone.enTelephone(), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TexteFonce)
                }
                if (f.telephone.isNotBlank() && f.delai.isNotBlank()) {
                    Text("  •  ", color = TexteGris)
                }
                if (f.delai.isNotBlank()) {
                    Icon(Icons.Default.Schedule, null, tint = Vert, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Délai ${f.delai}", fontSize = 12.sp, color = TexteGris)
                }
            }
        }

        // Matières fournies
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("MATIÈRES FOURNIES", fontSize = 11.sp, color = TexteGris, fontWeight = FontWeight.SemiBold)
            if (f.ingredients.isEmpty()) {
                Text("Aucune matière rattachée", fontSize = 13.sp, color = TexteGris)
            } else {
                Text(
                    f.ingredients.joinToString(" • ") { it.nom },
                    fontSize = 13.sp, color = TexteFonce, maxLines = 3, overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Dernier achat
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, null, tint = if (dernier != null) Vert else TexteGris, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    if (dernier != null) "Dernier achat : ${dernier.dateHeure.toLocalDate().format(formatDate)}"
                    else "Aucun achat enregistré",
                    fontSize = 12.sp, color = TexteGris
                )
            }
            dernier?.montant?.let {
                Text(it.enMGA(), fontWeight = FontWeight.Bold, color = TexteFonce)
            }
        }

        // Actions
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (f.telephone.isNotBlank()) {
                DonakaButton(
                    "Appeler", onAppeler, style = StyleBouton.SUCCES,
                    icone = Icons.Default.Call, modifier = Modifier.weight(1f)
                )
            } else {
                DonakaButton(
                    "Ajouter le n°", onModifier, style = StyleBouton.CONTOUR,
                    icone = Icons.Default.Edit, modifier = Modifier.weight(1f)
                )
            }
            DonakaButton(
                "Commander", onCommander, style = StyleBouton.SECONDAIRE,
                icone = Icons.Default.ShoppingCartCheckout, modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun Vide(icone: ImageVector, titre: String, message: String) {
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