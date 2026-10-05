package com.example.donaka100.data.local

import com.example.donaka100.data.ModeReglement
import com.example.donaka100.data.TypeMouvement
import com.example.donaka100.data.local.entity.PaiementEntity
import com.example.donaka100.data.local.model.CommandeWithDetails
import java.util.UUID

object FifoImputationHelper {

    /**
     * Applique un paiement global pour un client selon la règle FIFO:
     * Apure en priorité la plus ancienne commande livrée non encore totalement réglée.
     *
     * @param clientId ID du client
     * @param clientNom Nom du client
     * @param montantTotal Montant total versé en Ariary entiers
     * @param mode Mode de règlement (Espèces, MVola, etc.)
     * @param note Note / référence du paiement
     * @param dateHeure Horodatage du paiement en epoch millis
     * @param commandesLivrees Liste des commandes livrées du client, triées par date croissante
     * @return Liste des PaiementEntity à insérer en BDD
     */
    fun imputerPaiementFIFO(
        clientId: String,
        clientNom: String,
        montantTotal: Long,
        mode: ModeReglement,
        note: String,
        dateHeure: Long,
        commandesLivrees: List<CommandeWithDetails>
    ): List<PaiementEntity> {
        val paiementsAInserer = mutableListOf<PaiementEntity>()
        var resteAImputer = montantTotal

        // Parcours FIFO des commandes livrées (de la plus ancienne à la plus récente)
        for (item in commandesLivrees) {
            if (resteAImputer <= 0) break

            val soldeCommande = item.resteACredit
            if (soldeCommande > 0) {
                val montantPortion = minOf(resteAImputer, soldeCommande)
                val numero = "REG-" + UUID.randomUUID().toString().take(6).uppercase()

                paiementsAInserer.add(
                    PaiementEntity(
                        id = UUID.randomUUID().toString(),
                        numero = numero,
                        clientId = clientId,
                        clientNom = clientNom,
                        commandeId = item.commande.id,
                        montant = montantPortion,
                        dateHeure = dateHeure,
                        mode = mode,
                        type = TypeMouvement.REGLEMENT,
                        note = note,
                        annule = false
                    )
                )

                resteAImputer -= montantPortion
            }
        }

        // S'il reste du montant (acompte / solde créditeur), on l'enregistre comme paiement global client
        if (resteAImputer > 0) {
            val numero = "REG-" + UUID.randomUUID().toString().take(6).uppercase()
            paiementsAInserer.add(
                PaiementEntity(
                    id = UUID.randomUUID().toString(),
                    numero = numero,
                    clientId = clientId,
                    clientNom = clientNom,
                    commandeId = null,
                    montant = resteAImputer,
                    dateHeure = dateHeure,
                    mode = mode,
                    type = TypeMouvement.REGLEMENT,
                    note = if (note.isBlank()) "Acompte / Avance client" else note,
                    annule = false
                )
            )
        }

        return paiementsAInserer
    }
}
