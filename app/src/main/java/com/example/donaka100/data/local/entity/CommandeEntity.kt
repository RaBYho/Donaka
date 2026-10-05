package com.example.donaka100.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "commandes",
    foreignKeys = [
        ForeignKey(
            entity = ClientEntity::class,
            parentColumns = ["id"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["clientId"]),
        Index(value = ["date"]),
        Index(value = ["echeance"])
    ]
)
data class CommandeEntity(
    @PrimaryKey val id: String,
    val clientId: String?, // null = client de passage / non prévu
    val clientNom: String,
    val nonPrevu: Boolean, // true = vente au comptoir / client direct
    val date: Long, // Epoch day for LocalDate
    val heureSouhaitee: String, // "08:00" ou ""
    val livreeA: Long?, // Epoch millis for LocalDateTime/LocalTime ou null
    val echeance: Long, // Epoch day (par défaut date + 7 jours)
    val archive: Boolean // Soft-delete
)
