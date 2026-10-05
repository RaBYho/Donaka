package com.example.donaka100.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.donaka100.data.ModeReglement
import com.example.donaka100.data.TypeMouvementStock
import com.example.donaka100.data.UniteStock

@Entity(
    tableName = "mouvements_stock",
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
data class MouvementStockEntity(
    @PrimaryKey val id: String,
    val ingredientId: String,
    val ingredientNom: String,
    val dateHeure: Long,
    val type: TypeMouvementStock,
    val quantite: Double,
    val motif: String,
    val montant: Long?,
    val fournisseur: String,
    val estAchat: Boolean,
    val mode: ModeReglement?,
    val annule: Boolean,
    val unite: UniteStock
)
