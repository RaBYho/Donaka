package com.example.donaka100.data

data class Reglages(
    val nomChef: String = "",
    val nomBoulangerie: String = "",
    val premierLancementTermine: Boolean = false,
    // Emplacements pour les lots futurs (avec valeurs par défaut, sans effet pour l'instant)
    val heureRappelSoir: String = "18:00",
    val heureLivraisonParDefaut: String = "08:00",
    val notifProduction: Boolean = true,
    val notifCommandes: Boolean = true,
    val notifStockAlerte: Boolean = true,
    val notifPaiements: Boolean = true,
    val notifRappelsSoir: Boolean = true,
    val delaiVerrouillageMinutes: Int = 1,
    val pinActive: Boolean = false,
    val sauvegardeAutoActive: Boolean = false
)
