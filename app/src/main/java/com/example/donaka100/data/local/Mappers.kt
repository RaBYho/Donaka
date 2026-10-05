package com.example.donaka100.data.local

import com.example.donaka100.data.Client
import com.example.donaka100.data.Commande
import com.example.donaka100.data.Consommation
import com.example.donaka100.data.Depense
import com.example.donaka100.data.Encaissement
import com.example.donaka100.data.FicheFournisseur
import com.example.donaka100.data.Fournee
import com.example.donaka100.data.Ingredient
import com.example.donaka100.data.LigneArticle
import com.example.donaka100.data.LigneFournee
import com.example.donaka100.data.LigneRecette
import com.example.donaka100.data.MouvementStock
import com.example.donaka100.data.Produit
import com.example.donaka100.data.local.entity.ClientEntity
import com.example.donaka100.data.local.entity.CommandeEntity
import com.example.donaka100.data.local.entity.ConsommationEntity
import com.example.donaka100.data.local.entity.DepenseEntity
import com.example.donaka100.data.local.entity.FourneeEntity
import com.example.donaka100.data.local.entity.FournisseurEntity
import com.example.donaka100.data.local.entity.IngredientEntity
import com.example.donaka100.data.local.entity.LigneCommandeEntity
import com.example.donaka100.data.local.entity.LigneFourneeEntity
import com.example.donaka100.data.local.entity.LigneRecetteEntity
import com.example.donaka100.data.local.entity.MouvementStockEntity
import com.example.donaka100.data.local.entity.PaiementEntity
import com.example.donaka100.data.local.entity.ProduitEntity
import com.example.donaka100.data.local.model.ClientWithCommandesAndPaiements
import com.example.donaka100.data.local.model.CommandeWithDetails
import com.example.donaka100.data.local.model.FourneeWithDetails
import com.example.donaka100.data.local.model.ProduitWithRecette
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

// Ingrédient Mappers
fun IngredientEntity.toDomain(): Ingredient = Ingredient(
    id = id,
    nom = nom,
    unite = unite,
    quantite = quantite,
    seuil = seuil,
    rayon = rayon,
    fournisseur = fournisseur
)

fun Ingredient.toEntity(archive: Boolean = false): IngredientEntity = IngredientEntity(
    id = id,
    nom = nom,
    unite = unite,
    quantite = quantite,
    seuil = seuil,
    rayon = rayon,
    fournisseur = fournisseur,
    archive = archive
)

// Produit Mappers
fun ProduitWithRecette.toDomain(): Produit = Produit(
    id = produit.id,
    nom = produit.nom,
    prixGros = produit.prixGros,
    prixPublic = produit.prixPublic,
    piecesParLot = produit.piecesParLot,
    recette = recette.map { it.toDomain() },
    archive = produit.archive,
    categorie = produit.categorie,
    pivotId = produit.pivotIngredientId ?: recette.firstOrNull { it.estPivot }?.ingredientId
)

fun LigneRecetteEntity.toDomain(): LigneRecette = LigneRecette(
    ingredientId = ingredientId,
    quantiteParLot = quantiteParLot
)

fun Produit.toEntity(): ProduitEntity = ProduitEntity(
    id = id,
    nom = nom,
    prixGros = prixGros,
    prixPublic = prixPublic,
    piecesParLot = piecesParLot,
    categorie = categorie,
    pivotIngredientId = pivotId ?: pivot?.ingredientId,
    archive = archive
)

fun LigneRecette.toEntity(produitId: String, pivotId: String?): LigneRecetteEntity = LigneRecetteEntity(
    produitId = produitId,
    ingredientId = ingredientId,
    quantiteParLot = quantiteParLot,
    estPivot = ingredientId == pivotId
)

// Fournee Mappers
fun FourneeWithDetails.toDomain(): Fournee = Fournee(
    id = fournee.id,
    date = LocalDate.ofEpochDay(fournee.date),
    heure = LocalDateTime.ofInstant(Instant.ofEpochMilli(fournee.heure), ZoneOffset.UTC),
    lignes = lignes.map { it.toDomain() },
    consommations = consommations.map { it.toDomain() },
    annuleeA = fournee.annuleeA?.let { LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneOffset.UTC) }
)

fun LigneFourneeEntity.toDomain(): LigneFournee = LigneFournee(
    produitId = produitId,
    nom = nomProduit,
    quantite = quantite,
    quantitePlanifiee = quantitePlanifiee
)

fun ConsommationEntity.toDomain(): Consommation = Consommation(
    ingredientId = ingredientId,
    nom = nomIngredient,
    unite = unite,
    requis = requis,
    deduit = deduit
)

fun Fournee.toEntity(): FourneeEntity = FourneeEntity(
    id = id,
    date = date.toEpochDay(),
    heure = heure.toInstant(ZoneOffset.UTC).toEpochMilli(),
    annuleeA = annuleeA?.toInstant(ZoneOffset.UTC)?.toEpochMilli()
)

fun LigneFournee.toEntity(fourneeId: String, id: String): LigneFourneeEntity = LigneFourneeEntity(
    id = id,
    fourneeId = fourneeId,
    produitId = produitId,
    nomProduit = nom,
    quantite = quantite,
    quantitePlanifiee = quantitePlanifiee
)

fun Consommation.toEntity(fourneeId: String, id: String): ConsommationEntity = ConsommationEntity(
    id = id,
    fourneeId = fourneeId,
    ingredientId = ingredientId,
    nomIngredient = nom,
    unite = unite,
    requis = requis,
    deduit = deduit
)

// MouvementStock Mappers
fun MouvementStockEntity.toDomain(): MouvementStock = MouvementStock(
    id = id,
    ingredientId = ingredientId,
    ingredientNom = ingredientNom,
    dateHeure = LocalDateTime.ofInstant(Instant.ofEpochMilli(dateHeure), ZoneOffset.UTC),
    type = type,
    quantite = quantite,
    motif = motif,
    montant = montant,
    fournisseur = fournisseur,
    estAchat = estAchat,
    mode = mode,
    annule = annule,
    unite = unite
)

fun MouvementStock.toEntity(): MouvementStockEntity = MouvementStockEntity(
    id = id,
    ingredientId = ingredientId,
    ingredientNom = ingredientNom,
    dateHeure = dateHeure.toInstant(ZoneOffset.UTC).toEpochMilli(),
    type = type,
    quantite = quantite,
    motif = motif,
    montant = montant,
    fournisseur = fournisseur,
    estAchat = estAchat,
    mode = mode,
    annule = annule,
    unite = unite
)

// Client Mappers
fun ClientWithCommandesAndPaiements.toDomain(): Client = Client(
    id = client.id,
    nom = client.nom,
    telephone = client.telephone,
    quartier = client.quartier,
    plafondCreance = client.plafondCreance,
    resteDu = resteDu,
    type = client.type
)

fun ClientEntity.toDomain(resteDuCalculer: Long = 0L): Client = Client(
    id = id,
    nom = nom,
    telephone = telephone,
    quartier = quartier,
    plafondCreance = plafondCreance,
    resteDu = resteDuCalculer,
    type = type
)

fun Client.toEntity(archive: Boolean = false): ClientEntity = ClientEntity(
    id = id,
    nom = nom,
    telephone = telephone,
    quartier = quartier,
    plafondCreance = plafondCreance,
    type = type,
    archive = archive
)

// Commande Mappers
fun CommandeWithDetails.toDomain(): Commande {
    val dateTimeLivree = commande.livreeA?.let {
        LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneOffset.UTC).toLocalTime()
    }
    val dernierPaiementMode = paiements.lastOrNull { !it.annule }?.mode

    return Commande(
        id = commande.id,
        clientId = commande.clientId,
        clientNom = commande.clientNom,
        nonPrevu = commande.nonPrevu,
        date = LocalDate.ofEpochDay(commande.date),
        heure = commande.heureSouhaitee,
        lignes = lignes.map { it.toDomain() },
        livreeA = dateTimeLivree,
        montantPaye = montantPaye,
        mode = dernierPaiementMode
    )
}

fun LigneCommandeEntity.toDomain(): LigneArticle = LigneArticle(
    produitId = produitId,
    nom = nomProduit,
    quantite = quantite,
    prixUnitaire = prixUnitaireFige
)

fun Commande.toEntity(archive: Boolean = false): CommandeEntity {
    val epochDate = date.toEpochDay()
    val epochEcheance = date.plusDays(7).toEpochDay()
    val epochLivreeA = livreeA?.let {
        date.atTime(it).toInstant(ZoneOffset.UTC).toEpochMilli()
    }

    return CommandeEntity(
        id = id,
        clientId = clientId,
        clientNom = clientNom,
        nonPrevu = nonPrevu,
        date = epochDate,
        heureSouhaitee = heure,
        livreeA = epochLivreeA,
        echeance = epochEcheance,
        archive = archive
    )
}

fun LigneArticle.toEntity(commandeId: String, id: String): LigneCommandeEntity = LigneCommandeEntity(
    id = id,
    commandeId = commandeId,
    produitId = produitId,
    nomProduit = nom,
    quantite = quantite,
    prixUnitaireFige = prixUnitaire
)

// Paiement / Encaissement Mappers
fun PaiementEntity.toDomain(): Encaissement = Encaissement(
    id = id,
    numero = numero,
    clientNom = clientNom,
    dateHeure = LocalDateTime.ofInstant(Instant.ofEpochMilli(dateHeure), ZoneOffset.UTC),
    mode = mode,
    montant = montant,
    note = note,
    type = type,
    commandeId = commandeId
)

fun Encaissement.toEntity(clientId: String, annule: Boolean = false): PaiementEntity = PaiementEntity(
    id = id,
    numero = numero,
    clientId = clientId,
    clientNom = clientNom,
    commandeId = commandeId,
    montant = montant,
    dateHeure = dateHeure.toInstant(ZoneOffset.UTC).toEpochMilli(),
    mode = mode,
    type = type,
    note = note,
    annule = annule
)

// Fournisseur Mappers
fun FournisseurEntity.toDomain(): FicheFournisseur = FicheFournisseur(
    id = id,
    nom = nom,
    telephone = telephone,
    adresse = adresse,
    delai = delai
)

fun FicheFournisseur.toEntity(archive: Boolean = false): FournisseurEntity = FournisseurEntity(
    id = id,
    nom = nom,
    telephone = telephone,
    adresse = adresse,
    delai = delai,
    archive = archive
)

// Depense Mappers
fun DepenseEntity.toDomain(): Depense = Depense(
    id = id,
    categorie = categorie,
    montant = montant,
    note = note,
    dateHeure = LocalDateTime.ofInstant(Instant.ofEpochMilli(dateHeure), ZoneOffset.UTC),
    mode = mode
)

fun Depense.toEntity(annule: Boolean = false): DepenseEntity = DepenseEntity(
    id = id,
    categorie = categorie,
    montant = montant,
    note = note,
    dateHeure = dateHeure.toInstant(ZoneOffset.UTC).toEpochMilli(),
    mode = mode,
    annule = annule
)
