package com.example.donaka100.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.example.donaka100.data.local.entity.ConsommationEntity
import com.example.donaka100.data.local.entity.FourneeEntity
import com.example.donaka100.data.local.entity.LigneFourneeEntity

data class FourneeWithDetails(
    @Embedded val fournee: FourneeEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "fourneeId"
    )
    val lignes: List<LigneFourneeEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "fourneeId"
    )
    val consommations: List<ConsommationEntity>
)
