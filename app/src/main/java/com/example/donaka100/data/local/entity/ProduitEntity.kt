package com.example.donaka100.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "produits")
data class ProduitEntity(
    @PrimaryKey val id: String,
    val nom: String,
    val prixGros: Long,
    val prixPublic: Long,
    val piecesParLot: Int,
    val categorie: String,
    val pivotIngredientId: String?,
    val archive: Boolean
)
