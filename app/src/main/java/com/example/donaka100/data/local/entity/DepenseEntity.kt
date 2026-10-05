package com.example.donaka100.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.donaka100.data.ModeReglement

@Entity(tableName = "depenses")
data class DepenseEntity(
    @PrimaryKey val id: String,
    val categorie: String,
    val montant: Long,
    val note: String,
    val dateHeure: Long,
    val mode: ModeReglement,
    val annule: Boolean
)
