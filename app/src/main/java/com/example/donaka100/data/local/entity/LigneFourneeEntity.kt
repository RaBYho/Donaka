package com.example.donaka100.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lignes_fournee",
    foreignKeys = [
        ForeignKey(
            entity = FourneeEntity::class,
            parentColumns = ["id"],
            childColumns = ["fourneeId"],
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
        Index(value = ["fourneeId"]),
        Index(value = ["produitId"])
    ]
)
data class LigneFourneeEntity(
    @PrimaryKey val id: String,
    val fourneeId: String,
    val produitId: String,
    val nomProduit: String,
    val quantite: Int,
    val quantitePlanifiee: Int
)
