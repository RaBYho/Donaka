package com.example.donaka100.data

import java.time.LocalDate
import java.time.LocalDateTime

/** Une ligne de recette telle que le chef la tape : un nom, pas un identifiant */
data class LigneRecetteSaisie(
    val nom: String,
    val unite: UniteStock,          // ignorée si l'ingrédient existe déjà : son unité est reprise
    val quantiteParLot: Double
)

data class NouveauProduit(
    val nom: String,
    val prixGros: Long,
    val prixPublic: Long,
    val piecesParLot: Int,
    val recette: List<LigneRecetteSaisie>,
    val categorie: String = "",
    val pivotNom: String? = null
)

/** Ingrédient nécessaire pour un plan de production */
data class Besoin(val ingredient: Ingredient, val requis: Double) {
    val manque: Double get() = (requis - ingredient.quantite).coerceAtLeast(0.0)
    val suffisant: Boolean get() = manque < 0.0005
}

data class LigneFournee(
    val produitId: String,
    val nom: String,
    val quantite: Int,             // réellement produit
    val quantitePlanifiee: Int     // prévu par les commandes
)

data class Consommation(
    val ingredientId: String,
    val nom: String,
    val unite: UniteStock,
    val requis: Double,
    val deduit: Double             // ce qui a réellement été retiré du stock (plafonné à 0)
) {
    val manque: Double get() = (requis - deduit).coerceAtLeast(0.0)
}

data class Fournee(
    val id: String,
    val date: LocalDate,           // le jour pour lequel on produit
    val heure: LocalDateTime,      // le moment de la validation
    val lignes: List<LigneFournee>,
    val consommations: List<Consommation>,
    val annuleeA: LocalDateTime? = null   // non nul = annulée, ingrédients remis en stock
) {
    val annulee: Boolean get() = annuleeA != null
    val totalPieces: Int get() = lignes.sumOf { it.quantite }
}