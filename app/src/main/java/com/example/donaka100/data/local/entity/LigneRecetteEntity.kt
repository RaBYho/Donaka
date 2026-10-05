package com.example.donaka100.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "lignes_recette",
    primaryKeys = ["produitId", "ingredientId"],
    foreignKeys = [
        ForeignKey(
            entity = ProduitEntity::class,
            parentColumns = ["id"],
            childColumns = ["produitId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = IngredientEntity::class,
            parentColumns = ["id"],
            childColumns = ["ingredientId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["produitId"]),
        Index(value = ["ingredientId"])
    ]
)
data class LigneRecetteEntity(
    val produitId: String,
    val ingredientId: String,
    val quantiteParLot: Double,
    val estPivot: Boolean
)
