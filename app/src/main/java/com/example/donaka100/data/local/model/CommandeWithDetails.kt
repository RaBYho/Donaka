package com.example.donaka100.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.example.donaka100.data.TypeMouvement
import com.example.donaka100.data.local.entity.CommandeEntity
import com.example.donaka100.data.local.entity.LigneCommandeEntity
import com.example.donaka100.data.local.entity.PaiementEntity

data class CommandeWithDetails(
    @Embedded val commande: CommandeEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "commandeId"
    )
    val lignes: List<LigneCommandeEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "commandeId"
    )
    val paiements: List<PaiementEntity>
) {
    val total: Long get() = lignes.sumOf { it.quantite.toLong() * it.prixUnitaireFige }
    val montantPaye: Long get() = paiements.filter { !it.annule && it.type != TypeMouvement.A_CREDIT }.sumOf { it.montant }
    val resteACredit: Long get() = if (commande.livreeA != null) (total - montantPaye).coerceAtLeast(0L) else 0L
}
