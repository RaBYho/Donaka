package com.example.donaka100.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fournisseurs")
data class FournisseurEntity(
    @PrimaryKey val id: String,
    val nom: String,
    val telephone: String,
    val adresse: String,
    val delai: String,
    val archive: Boolean
)
