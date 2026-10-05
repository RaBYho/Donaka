package com.example.donaka100.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "prix_ingredients",
    foreignKeys = [
        ForeignKey(
            entity = IngredientEntity::class,
            parentColumns = ["id"],
            childColumns = ["ingredientId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["ingredientId"]),
        Index(value = ["dateEnregistrement"])
    ]
)
data class PrixIngredientEntity(
    @PrimaryKey val id: String,
    val ingredientId: String,
    val prixParUnite: Long,
    val dateEnregistrement: Long,
    val fournisseur: String
)
