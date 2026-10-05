package com.example.donaka100.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fournees")
data class FourneeEntity(
    @PrimaryKey val id: String,
    val date: Long,
    val heure: Long,
    val annuleeA: Long?
)
