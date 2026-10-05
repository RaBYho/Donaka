package com.example.donaka100.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.donaka100.data.ModeReglement

@Entity(
    tableName = "achats",
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
        Index(value = ["dateHeure"])
    ]
)
data class AchatEntity(
    @PrimaryKey val id: String,
    val fournisseurId: String?,
    val fournisseurNom: String,
    val ingredientId: String,
    val ingredientNom: String,
    val dateHeure: Long,
    val quantite: Double,
    val montant: Long?,
    val mode: ModeReglement?,
    val echeance: Long?,
    val annule: Boolean
)
