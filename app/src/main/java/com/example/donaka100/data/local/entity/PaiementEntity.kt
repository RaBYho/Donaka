package com.example.donaka100.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.donaka100.data.ModeReglement
import com.example.donaka100.data.TypeMouvement

@Entity(
    tableName = "paiements",
    foreignKeys = [
        ForeignKey(
            entity = ClientEntity::class,
            parentColumns = ["id"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = CommandeEntity::class,
            parentColumns = ["id"],
            childColumns = ["commandeId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["clientId"]),
        Index(value = ["commandeId"]),
        Index(value = ["dateHeure"])
    ]
)
data class PaiementEntity(
    @PrimaryKey val id: String,
    val numero: String, // "ENC-0001" ou "REG-0002"
    val clientId: String,
    val clientNom: String,
    val commandeId: String?, // null = paiement global client
    val montant: Long, // Ariary entiers
    val dateHeure: Long, // Epoch millis
    val mode: ModeReglement?, // null si A_CREDIT
    val type: TypeMouvement, // ENCAISSE, A_CREDIT, REGLEMENT
    val note: String,
    val annule: Boolean // Soft-delete
)
