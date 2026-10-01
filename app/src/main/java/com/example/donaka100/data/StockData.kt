package com.example.donaka100.data

import java.time.LocalDateTime

enum class TypeMouvementStock(val libelle: String) {
    ENTREE("Entrée"), SORTIE("Sortie"), AJUSTEMENT("Ajustement")
}

/** Trace de chaque changement de quantité. Servira aux onglets Achats et Dépenses. */
data class MouvementStock(
    val id: String,
    val ingredientId: String,
    val ingredientNom: String,              // gardé : l'ingrédient peut être supprimé plus tard
    val dateHeure: LocalDateTime,
    val type: TypeMouvementStock,
    val quantite: Double,                   // signée : + entrée, - sortie
    val motif: String = "",
    val montant: Long? = null,              // prix payé (achats)
    val fournisseur: String = ""
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
    val unite: UniteStock,                  // ignorée si l'ingrédient existe déjà
    val quantite: Double,
    val montant: Long?,
    val fournisseur: String
)