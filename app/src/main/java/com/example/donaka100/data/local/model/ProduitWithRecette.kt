package com.example.donaka100.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.example.donaka100.data.local.entity.LigneRecetteEntity
import com.example.donaka100.data.local.entity.ProduitEntity

data class ProduitWithRecette(
    @Embedded val produit: ProduitEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "produitId"
    )
    val recette: List<LigneRecetteEntity>
)
