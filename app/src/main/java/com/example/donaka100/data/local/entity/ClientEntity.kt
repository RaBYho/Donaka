package com.example.donaka100.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.donaka100.data.TypeClient

@Entity(tableName = "clients")
data class ClientEntity(
    @PrimaryKey val id: String,
    val nom: String,
    val telephone: String,
    val quartier: String,
    val plafondCreance: Long, // Ariary entiers (0 = pas de plafond)
    val type: TypeClient,
    val archive: Boolean // Soft-delete
)
