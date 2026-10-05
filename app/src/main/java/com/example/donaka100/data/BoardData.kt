package com.example.donaka100.data

import java.time.LocalDateTime

data class Operation(
    val id: String = "",
    val titre: String = "",
    val heure: String = "",
    val detail: String = "",
    val montant: Long = 0        // positif = entrée, négatif = sortie
)

data class LigneCommande(
    val nom: String = "",
    val quantite: Int = 0
)

data class BoardData(
    val nomUtilisateur: String = "",
    val fournilOuvert: Boolean? = null,           // null = inconnu : le badge est masqué
    val chiffreAffaires: Long = 0,                // valeur livrée + ventes comptoir
    val encaisse: Long = 0,                       // argent réellement reçu
    val creditDuJour: Long = 0,                   // livré mais non payé
    val achatsEtFrais: Long = 0,
    val variationVeille: Int? = null,             // en %, null si hier = 0
    val stocks: List<Ingredient> = emptyList(),
    val commandesDemain: List<LigneCommande> = emptyList(),
    val heureLivraison: String = "",
    val nbCreances: Int = 0,
    val totalCreances: Long = 0,
    val dernieresOperations: List<Operation> = emptyList(),
    // Données des formulaires (+ Vente, + Sortie)
    val fournisseurs: List<String> = emptyList(),
    val produits: List<Produit> = emptyList(),
    val categoriesDepense: List<String> = emptyList()
) {
    val tresorerie: Long get() = encaisse - achatsEtFrais
    val totalPiecesDemain: Int get() = commandesDemain.sumOf { it.quantite }
    val stocksSousSurveillance: List<Ingredient>
        get() = stocks
            .filter { it.statut == StatutStock.CRITIQUE || it.statut == StatutStock.SEUIL_JUSTE }
            .sortedWith(compareBy({ it.statut.priorite }, { it.nom.lowercase() }))
}

/** Vente au comptoir : prix public, payée comptant */
data class NouvelleVenteComptoir(
    val lignes: List<LigneDemande>,
    val mode: ModeReglement
)

data class VenteComptoir(
    val id: String,
    val dateHeure: LocalDateTime,
    val lignes: List<LigneArticle>,
    val mode: ModeReglement
) {
    val total: Long get() = lignes.sumOf { it.montant }
    val resume: String get() = lignes.joinToString(", ") { "${it.quantite}× ${it.nom}" }
}