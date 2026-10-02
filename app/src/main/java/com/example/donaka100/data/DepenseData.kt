package com.example.donaka100.data

import java.time.LocalDate
import java.time.LocalDateTime

/** Catégorie réservée : les achats d'ingrédients faits depuis Stock */
const val CATEGORIE_ACHATS = "Achats de stock"

data class Depense(
    val id: String,
    val categorie: String,
    val montant: Long,
    val note: String = "",
    val dateHeure: LocalDateTime,
    val mode: ModeReglement = ModeReglement.ESPECES
)

data class NouvelleDepense(
    val categorie: String,
    val montant: Long,
    val note: String,
    val date: LocalDate,
    val mode: ModeReglement
)