package com.example.donaka100.data

import java.time.LocalDateTime

enum class TypeMouvementStock(val libelle: String) {
    ENTREE("Entrée"), SORTIE("Sortie"), AJUSTEMENT("Ajustement")
}

/** Trace de chaque changement de quantité. Servira aux onglets Achats et Dépenses. */
data class MouvementStock(
    val id: String,
    val ingredientId: String,
    val ingredientNom: String,
    val dateHeure: LocalDateTime,
    val type: TypeMouvementStock,
    val quantite: Double,
    val motif: String = "",
    val montant: Long? = null,
    val fournisseur: String = "",
    val estAchat: Boolean = false,           // vrai pour les entrées créées par « Effectuer un achat »
    val mode: ModeReglement? = null,         // comment l'achat a été payé
    val annule: Boolean = false,             // achat annulé : le stock a été retiré
    val unite: UniteStock = UniteStock.KG
)


/** Création ET modification (en modification, la quantité saisie devient un ajustement) */
data class NouvelIngredient(
    val nom: String,
    val unite: UniteStock,
    val quantite: Double,
    val seuil: Double,
    val rayon: String,
    val fournisseur: String
)

data class NouvelAchat(
    val nom: String,
    val unite: UniteStock,
    val quantite: Double,
    val montant: Long?,
    val fournisseur: String,
    val mode: ModeReglement = ModeReglement.ESPECES
)

/** La quantité ne se corrige pas ici : c'est le rôle de « Ajuster » */
data class ModificationAchat(
    val montant: Long?,
    val fournisseur: String,
    val mode: ModeReglement
)