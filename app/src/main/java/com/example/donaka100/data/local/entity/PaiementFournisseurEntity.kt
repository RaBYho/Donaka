package com.example.donaka100.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.donaka100.data.ModeReglement

@Entity(
    tableName = "paiements_fournisseur",
    indices = [
        Index(value = ["fournisseurNom"]),
        Index(value = ["achatId"]),
        Index(value = ["dateHeure"])
    ]
)
data class PaiementFournisseurEntity(
    @PrimaryKey val id: String,
    val fournisseurId: String?,
    val fournisseurNom: String,
    val achatId: String?,
    val montant: Long,
    val dateHeure: Long,
    val mode: ModeReglement,
    val note: String,
    val annule: Boolean
)
