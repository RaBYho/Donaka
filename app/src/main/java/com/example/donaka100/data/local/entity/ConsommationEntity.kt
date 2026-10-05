package com.example.donaka100.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.donaka100.data.UniteStock

@Entity(
    tableName = "consommations",
    foreignKeys = [
        ForeignKey(
            entity = FourneeEntity::class,
            parentColumns = ["id"],
            childColumns = ["fourneeId"],
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
        Index(value = ["fourneeId"]),
        Index(value = ["ingredientId"])
    ]
)
data class ConsommationEntity(
    @PrimaryKey val id: String,
    val fourneeId: String,
    val ingredientId: String,
    val nomIngredient: String,
    val unite: UniteStock,
    val requis: Double,
    val deduit: Double
)
