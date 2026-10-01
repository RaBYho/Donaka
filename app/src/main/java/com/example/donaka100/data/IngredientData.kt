package com.example.donaka100.data

enum class UniteStock(val libelle: String) { KG("kg"), G("g"), L("L"), PIECE("pièces") }

/** Provisoire : la page Stock gérera cette liste plus tard */
data class Ingredient(
    val id: String = "",
    val nom: String = "",
    val unite: UniteStock = UniteStock.KG,
    val quantite: Double = 0.0,
    val seuil: Double = 0.0
)
/** "  Farine   T55 " -> "Farine T55" */
fun String.nettoyerNom(): String = trim().replace(Regex("\\s+"), " ")

/** Clé de comparaison : "farine t55" == "Farine  T55" */
fun String.cleNom(): String = nettoyerNom().lowercase()