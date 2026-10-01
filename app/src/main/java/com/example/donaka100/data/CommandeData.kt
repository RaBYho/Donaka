package com.example.donaka100.data

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

enum class TypeClient { EPICERIE, BOULANGERIE, RESTAURANT, AUTRE }

data class Client(
    val id: String = "",
    val nom: String = "",
    val telephone: String = "",          // chiffres seulement : "0341234567"
    val quartier: String = "",
    val plafondCreance: Long = 0,        // 0 = pas de plafond
    val resteDu: Long = 0,
    val detailDette: String = "",
    val type: TypeClient = TypeClient.AUTRE
) {
    val aJour: Boolean get() = resteDu <= 0
    val plafondAtteint: Boolean get() = plafondCreance > 0 && resteDu >= plafondCreance
}

/** Ce que le formulaire envoie (création ET modification) */
data class NouveauClient(
    val nom: String,
    val telephone: String,
    val quartier: String,
    val plafondCreance: Long
)

enum class ModeReglement(val libelle: String) {
    ESPECES("Espèces"),
    MVOLA("MVola"),
    AIRTEL_ORANGE("Airtel / Orange"),
    BANCAIRE("Bancaire")
}

/** ENCAISSE = argent reçu à la livraison, A_CREDIT = livraison non payée, REGLEMENT = créance payée plus tard */
enum class TypeMouvement { ENCAISSE, A_CREDIT, REGLEMENT }

data class Encaissement(
    val id: String = "",
    val numero: String = "",                 // "ENC-0001" ou "CRD-0002"
    val clientNom: String = "Client direct",
    val dateHeure: LocalDateTime = LocalDateTime.now(),
    val mode: ModeReglement? = null,         // null quand c'est à crédit
    val montant: Long = 0,
    val note: String = "",
    val type: TypeMouvement = TypeMouvement.ENCAISSE,
    val commandeId: String? = null
) {
    /** Faux pour une ligne « à crédit » : ce n'est pas de l'argent en caisse */
    val argentRecu: Boolean get() = type != TypeMouvement.A_CREDIT
}

/** Quantité d'un ingrédient pour UN lot, dans l'unité de l'ingrédient (0,1 kg = 100 g) */
data class LigneRecette(
    val ingredientId: String = "",
    val quantiteParLot: Double = 0.0
)

/** prixGros = ce que paie le client de la commande, prixPublic = vente au comptoir */
data class Produit(
    val id: String = "",
    val nom: String = "",
    val prixGros: Long = 0,
    val prixPublic: Long = 0,
    val piecesParLot: Int = 0,
    val recette: List<LigneRecette> = emptyList(),
    val archive: Boolean = false,
    val categorie: String = "",
    val pivotId: String? = null
) {
    val aRecette: Boolean get() = piecesParLot > 0 && recette.isNotEmpty()
    val margeRevendeur: Long get() = prixPublic - prixGros

    /** Le pivot choisi, sinon le premier ingrédient de la recette */
    val pivot: LigneRecette?
        get() = recette.firstOrNull { it.ingredientId == pivotId } ?: recette.firstOrNull()
}

/** Le prix est figé au moment de la commande */
data class LigneArticle(
    val produitId: String = "",
    val nom: String = "",
    val quantite: Int = 0,
    val prixUnitaire: Long = 0
) {
    val montant: Long get() = quantite * prixUnitaire
}

data class Commande(
    val id: String = "",
    val clientId: String? = null,            // null = client non prévu
    val clientNom: String = "",
    val nonPrevu: Boolean = false,
    val date: LocalDate = LocalDate.now(),
    val heure: String = "",                  // "08:00", facultatif
    val lignes: List<LigneArticle> = emptyList(),
    val livreeA: LocalTime? = null,          // null = pas encore livrée
    val montantPaye: Long = 0,
    val mode: ModeReglement? = null
) {
    val total: Long get() = lignes.sumOf { it.montant }
    val nbArticles: Int get() = lignes.sumOf { it.quantite }
    val livree: Boolean get() = livreeA != null
    val resteACredit: Long get() = if (livree) total - montantPaye else 0
    val resume: String get() = lignes.joinToString(", ") { "${it.quantite}× ${it.nom}" }
}

/** Ce que les formulaires envoient */
data class LigneDemande(val produitId: String, val quantite: Int)

data class NouvelleCommande(
    val clientId: String,
    val date: LocalDate,
    val heure: String,
    val lignes: List<LigneDemande>
)