package com.example.donaka100.data

enum class UniteStock(val libelle: String) { KG("kg"), G("g"), L("L"), PIECE("pièces") }

enum class StatutStock(val libelle: String, val priorite: Int) {
    CRITIQUE("Critique", 0),
    SEUIL_JUSTE("Seuil juste", 1),
    A_RENSEIGNER("À renseigner", 2),
    OK("OK", 3)
}

/** Une seule règle pour toute l'app (Stock, Board, Fourneaux) */
fun statutStock(quantite: Double, seuil: Double): StatutStock = when {
    seuil <= 0 -> StatutStock.A_RENSEIGNER
    quantite < seuil -> StatutStock.CRITIQUE
    quantite <= seuil * 1.25 -> StatutStock.SEUIL_JUSTE
    else -> StatutStock.OK
}

data class Ingredient(
    val id: String = "",
    val nom: String = "",
    val unite: UniteStock = UniteStock.KG,
    val quantite: Double = 0.0,
    val seuil: Double = 0.0,
    val rayon: String = "",
    val fournisseur: String = ""
) {
    val statut: StatutStock get() = statutStock(quantite, seuil)
}

/** "  Farine   T55 " -> "Farine T55" */
fun String.nettoyerNom(): String = trim().replace(Regex("\\s+"), " ")

/** Clé de comparaison : "farine t55" == "Farine  T55" */
fun String.cleNom(): String = nettoyerNom().lowercase()