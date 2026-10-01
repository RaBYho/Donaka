package com.example.donaka100.ui.forms

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.donaka100.data.*
import com.example.donaka100.ui.components.*
import com.example.donaka100.ui.theme.*

private data class LigneForm(
    val uid: Int,
    val nom: String = "",
    val unite: UniteStock = UniteStock.KG,
    val quantite: String = ""
)

@Composable
fun ProduitFormSheet(
    ingredients: List<Ingredient>,      // sert uniquement aux suggestions
    categories: List<String>,
    initial: Produit? = null,
    onDismiss: () -> Unit,
    onSave: (NouveauProduit) -> Unit
) {
    var nom by remember { mutableStateOf(initial?.nom.orEmpty()) }
    var categorie by remember { mutableStateOf(initial?.categorie.orEmpty()) }
    var gros by remember { mutableStateOf(initial?.prixGros?.toString().orEmpty()) }
    var prixPub by remember { mutableStateOf(initial?.prixPublic?.toString().orEmpty()) }
    var lot by remember { mutableStateOf(initial?.piecesParLot?.takeIf { it > 0 }?.toString().orEmpty()) }

    val lignes = remember {
        mutableStateListOf<LigneForm>().apply {
            initial?.recette?.forEachIndexed { i, r ->
                val ing = ingredients.firstOrNull { it.id == r.ingredientId }
                add(LigneForm(i, ing?.nom.orEmpty(), ing?.unite ?: UniteStock.KG, r.quantiteParLot.enQuantite()))
            }
        }
    }
    var prochainUid by remember { mutableIntStateOf(initial?.recette?.size ?: 0) }
    var pivotUid by remember {
        mutableStateOf(
            initial?.recette?.indexOfFirst { it.ingredientId == initial.pivotId }?.takeIf { it >= 0 }
        )
    }
    var tentative by remember { mutableStateOf(false) }

    val grosL = gros.toLongOrNull() ?: 0L
    val pubL = prixPub.toLongOrNull() ?: 0L
    val pieces = lot.toIntOrNull() ?: 0

    fun existant(nomSaisi: String) = ingredients.firstOrNull { it.nom.cleNom() == nomSaisi.cleNom() }

    val valides = lignes.mapNotNull { l ->
        val q = l.quantite.enDecimal()
        if (l.nom.isNotBlank() && q != null && q > 0)
            LigneRecetteSaisie(l.nom.nettoyerNom(), existant(l.nom)?.unite ?: l.unite, q)
        else null
    }
    val incompletes = valides.size != lignes.size
    val doublons = valides.map { it.nom.cleNom() }.distinct().size != valides.size
    val ok = nom.isNotBlank() && grosL > 0 && pubL > 0 && !incompletes && !doublons &&
            (lignes.isEmpty() || pieces > 0)

    // Le pivot choisi s'il existe encore, sinon la première ligne
    val pivotLigneUid = if (lignes.any { it.uid == pivotUid }) pivotUid else lignes.firstOrNull()?.uid
    val pivotNom = lignes.firstOrNull { it.uid == pivotLigneUid }?.nom?.takeIf { it.isNotBlank() }

    DonakaFormSheet(
        titre = if (initial == null) "Nouveau produit" else "Modifier le produit",
        onDismiss = onDismiss,
        onValider = {
            tentative = true
            if (ok) {
                onSave(
                    NouveauProduit(
                        nom = nom.trim(), prixGros = grosL, prixPublic = pubL,
                        piecesParLot = if (valides.isEmpty()) 0 else pieces,
                        recette = valides, categorie = categorie.trim(),
                        pivotNom = pivotNom
                    )
                )
            }
        }
    ) {
        DonakaTextField(
            value = nom, onValueChange = { nom = it },
            label = "Nom du produit *",
            error = if (tentative && nom.isBlank()) "Le nom est obligatoire" else null
        )
        DonakaTextField(
            value = categorie, onValueChange = { categorie = it },
            label = "Catégorie (facultatif)",
            helper = "Ex : Pâtisserie, Boulangerie, Viennoiserie"
        )
        if (categories.isNotEmpty()) {
            DonakaFilterChips(
                options = categories,
                selected = categorie.trim(),
                onSelect = { categorie = it },
                label = { it }
            )
        }
        DonakaAmountField(
            value = gros, onValueChange = { gros = it },
            label = "Prix de gros (ce que paie le client)",
            error = if (tentative && grosL <= 0) "Le prix doit être supérieur à 0" else null
        )
        DonakaAmountField(
            value = prixPub, onValueChange = { prixPub = it },
            label = "Prix public (vente au comptoir)",
            error = if (tentative && pubL <= 0) "Le prix doit être supérieur à 0" else null
        )
        if (grosL > 0 && pubL in 1 until grosL) {
            Text("Le prix public est inférieur au prix de gros.", color = Ambre, fontSize = 12.sp)
        }

        Text("Recette par lot", fontWeight = FontWeight.Bold, color = TexteFonce)

        DonakaTextField(
            value = lot,
            onValueChange = { lot = it.filter(Char::isDigit).take(4) },
            label = "1 lot donne",
            suffix = "pièces",
            keyboardType = KeyboardType.Number,
            error = if (tentative && lignes.isNotEmpty() && pieces <= 0)
                "Indique combien de pièces donne un lot" else null
        )

        lignes.forEachIndexed { i, l ->
            val ing = existant(l.nom)
            val estPivot = l.uid == pivotLigneUid
            val suggestions =
                if (l.nom.isBlank()) emptyList()
                else ingredients.filter {
                    it.nom.cleNom().contains(l.nom.cleNom()) && it.nom.cleNom() != l.nom.cleNom()
                }.take(4)

            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DonakaTextField(
                        value = l.nom,
                        onValueChange = { lignes[i] = l.copy(nom = it) },
                        label = "Ingrédient",
                        placeholder = "Ex : Farine T55",
                        helper = when {
                            l.nom.isBlank() -> null
                            ing != null -> "Déjà en stock"
                            else -> "Nouveau : sera ajouté au stock"
                        }
                    )
                    if (suggestions.isNotEmpty()) {
                        DonakaFilterChips(
                            options = suggestions,
                            selected = null,
                            onSelect = { lignes[i] = l.copy(nom = it.nom, unite = it.unite) },
                            label = { it.nom }
                        )
                    }
                    if (l.nom.isNotBlank() && ing == null) {
                        DonakaDropdownField(
                            label = "Unité",
                            options = UniteStock.entries.map { it.libelle },
                            selected = l.unite.libelle,
                            onSelect = { choix ->
                                lignes[i] = l.copy(unite = UniteStock.entries.first { it.libelle == choix })
                            }
                        )
                    }
                    DonakaTextField(
                        value = l.quantite,
                        onValueChange = { saisie ->
                            lignes[i] = l.copy(
                                quantite = saisie.filter { c -> c.isDigit() || c == ',' || c == '.' }.take(8)
                            )
                        },
                        label = "Quantité par lot",
                        suffix = (ing?.unite ?: l.unite).libelle,
                        keyboardType = KeyboardType.Decimal
                    )
                }
                Column {
                    IconButton(onClick = { pivotUid = l.uid }) {
                        Icon(
                            if (estPivot) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = if (estPivot) "Ingrédient pivot" else "Définir comme pivot",
                            tint = if (estPivot) Ambre else TexteGris
                        )
                    }
                    IconButton(onClick = { lignes.removeAt(i) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Retirer l'ingrédient", tint = Rouge)
                    }
                }
            }
        }

        if (tentative && incompletes) {
            Text("Complète ou supprime les lignes incomplètes (nom et quantité).", color = Rouge, fontSize = 12.sp)
        }
        if (tentative && doublons) {
            Text("Un ingrédient apparaît deux fois.", color = Rouge, fontSize = 12.sp)
        }
        DonakaButton(
            "Ajouter un ingrédient",
            { lignes.add(LigneForm(prochainUid)); prochainUid++ },
            style = StyleBouton.CONTOUR, icone = Icons.Default.Add, pleineLargeur = true
        )
        Text(
            "★ = ingrédient pivot, la base de la recette (en général la farine). " +
                    "Un ingrédient nouveau est ajouté au stock avec une quantité de 0. " +
                    "Quantités dans l'unité du stock : 100 g = 0,1 kg.",
            fontSize = 12.sp, color = TexteGris
        )
    }
}