package com.example.donaka100.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.example.donaka100.data.TypeMouvement
import com.example.donaka100.data.local.entity.ClientEntity
import com.example.donaka100.data.local.entity.CommandeEntity
import com.example.donaka100.data.local.entity.PaiementEntity

data class ClientWithCommandesAndPaiements(
    @Embedded val client: ClientEntity,
    @Relation(
        entity = CommandeEntity::class,
        parentColumn = "id",
        entityColumn = "clientId"
    )
    val commandes: List<CommandeWithDetails>,
    @Relation(
        parentColumn = "id",
        entityColumn = "clientId"
    )
    val paiements: List<PaiementEntity>
) {
    val totalLivre: Long
        get() = commandes
            .filter { !it.commande.archive && it.commande.livreeA != null }
            .sumOf { it.total }

    val totalPaye: Long
        get() = paiements
            .filter { !it.annule && it.type != TypeMouvement.A_CREDIT }
            .sumOf { it.montant }

    val resteDu: Long get() = (totalLivre - totalPaye).coerceAtLeast(0L)
}
