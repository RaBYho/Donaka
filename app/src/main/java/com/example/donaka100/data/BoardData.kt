package com.example.donaka100.data

data class Operation(
    val id: String = "",
    val titre: String = "",
    val heure: String = "",
    val detail: String = "",
    val montant: Long = 0        // positif = entrée, négatif = sortie
)


data class StockSurveille(
    val id: String = "",
    val nom: String = "",
    val quantiteKg: Int = 0,
    val seuilKg: Int = 0
) {
    // Calculé ici, donc le backend n'envoie que des quantités brutes
    val statut: StatutStock
        get() = statutStock(quantiteKg.toDouble(), seuilKg.toDouble())
}

data class LigneCommande(
    val nom: String = "",
    val quantite: Int = 0
)

data class BoardData(
    val nomUtilisateur: String = "",
    val fournilOuvert: Boolean = false,
    val chiffreAffaires: Long = 0,
    val achatsEtFrais: Long = 0,
    val variationVeille: Int = 0,                 // en %
    val stocks: List<StockSurveille> = emptyList(),
    val commandesDemain: List<LigneCommande> = emptyList(),
    val heureLivraison: String = "",
    val nbCreances: Int = 0,
    val totalCreances: Long = 0,
    val dernieresOperations: List<Operation> = emptyList()
) {
    val tresorerie: Long get() = chiffreAffaires - achatsEtFrais
    val totalPiecesDemain: Int get() = commandesDemain.sumOf { it.quantite }
    val stocksSousSurveillance: List<StockSurveille>
        get() = stocks.filter { it.statut == StatutStock.CRITIQUE || it.statut == StatutStock.SEUIL_JUSTE }
}

/** Ce que l'app envoie au backend quand on enregistre une vente */
data class NouvelleVente(
    val libelle: String,
    val detail: String,
    val montant: Long,
    val aCredit: Boolean
)