package com.example.donaka100.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.donaka100.data.UniteStock

@Entity(tableName = "ingredients")
data class IngredientEntity(
    @PrimaryKey val id: String,
    val nom: String,
    val unite: UniteStock,
    val quantite: Double,
    val seuil: Double,
    val rayon: String,
    val fournisseur: String,
    val archive: Boolean
)
