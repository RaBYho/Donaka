package com.example.donaka100.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lignes_commande",
    foreignKeys = [
        ForeignKey(
            entity = CommandeEntity::class,
            parentColumns = ["id"],
            childColumns = ["commandeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProduitEntity::class,
            parentColumns = ["id"],
            childColumns = ["produitId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["commandeId"]),
        Index(value = ["produitId"])
    ]
)
data class LigneCommandeEntity(
    @PrimaryKey val id: String,
    val commandeId: String,
    val produitId: String,
    val nomProduit: String,
    val quantite: Int,
    val prixUnitaireFige: Long // Prix figé en Ariary entiers
)
