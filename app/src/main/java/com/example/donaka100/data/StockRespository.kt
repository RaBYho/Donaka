package com.example.donaka100.data

import kotlinx.coroutines.delay
import java.util.UUID
import kotlin.math.abs
import kotlin.time.Duration.Companion.milliseconds

interface StockRepository {
    suspend fun getIngredients(): List<Ingredient>
    suspend fun getProduits(): List<Produit>
    suspend fun getFournees(): List<Fournee>
    suspend fun getMouvements(): List<MouvementStock>

    suspend fun creerIngredient(i: NouvelIngredient)
    suspend fun modifierIngredient(id: String, i: NouvelIngredient)
    suspend fun supprimerIngredient(id: String)
    suspend fun ajuster(id: String, nouvelleQuantite: Double, motif: String)
    /** Retourne true si l'ingrédient a été créé par cet achat */
    suspend fun acheter(a: NouvelAchat): Boolean
}

class FakeStockRepository(avecDemo: Boolean = true) : StockRepository {

    private val db = FakeBackend

    init { if (avecDemo) db.chargerDemo() }

    private fun utiliseDansRecette(id: String) =
        db.produits.any { p -> !p.archive && p.recette.any { it.ingredientId == id } }

    override suspend fun getIngredients(): List<Ingredient> { delay(500.milliseconds); return db.ingredients }
    override suspend fun getProduits() = db.produits
    override suspend fun getFournees() = db.fournees
    override suspend fun getMouvements() = db.mouvements

    override suspend fun creerIngredient(i: NouvelIngredient) {
        delay(300.milliseconds)
        require(db.ingredients.none { it.nom.cleNom() == i.nom.cleNom() }) { "Ingrédient déjà existant" }
        val ing = Ingredient(
            UUID.randomUUID().toString(), i.nom.nettoyerNom(), i.unite, i.quantite, i.seuil,
            i.rayon.nettoyerNom(), i.fournisseur.nettoyerNom()
        )
        db.ingredients += ing
        if (i.quantite > 0) {
            db.noter(ing.id, ing.nom, TypeMouvementStock.ENTREE, i.quantite, "Stock initial")
        }
    }

    override suspend fun modifierIngredient(id: String, i: NouvelIngredient) {
        delay(300.milliseconds)
        val ancien = db.ingredients.first { it.id == id }
        require(db.ingredients.none { it.id != id && it.nom.cleNom() == i.nom.cleNom() }) { "Nom déjà pris" }
        require(ancien.unite == i.unite || !utiliseDansRecette(id)) { "Unité verrouillée" }

        db.ingredients = db.ingredients.map {
            if (it.id != id) it
            else it.copy(
                nom = i.nom.nettoyerNom(), unite = i.unite, quantite = i.quantite, seuil = i.seuil,
                rayon = i.rayon.nettoyerNom(), fournisseur = i.fournisseur.nettoyerNom()
            )
        }
        val ecart = i.quantite - ancien.quantite
        if (abs(ecart) > 0.0005) {
            db.noter(id, i.nom.nettoyerNom(), TypeMouvementStock.AJUSTEMENT, ecart, "Correction de la fiche")
        }
    }

    override suspend fun supprimerIngredient(id: String) {
        delay(300.milliseconds)
        check(!utiliseDansRecette(id)) { "Ingrédient utilisé dans une recette" }
        db.ingredients = db.ingredients.filterNot { it.id == id }
        // Les produits archivés ne sont plus modifiables : on nettoie leur recette
        db.produits = db.produits.map {
            if (it.archive) it.copy(recette = it.recette.filterNot { l -> l.ingredientId == id }) else it
        }
    }

    override suspend fun ajuster(id: String, nouvelleQuantite: Double, motif: String) {
        delay(300.milliseconds)
        val ing = db.ingredients.first { it.id == id }
        val ecart = nouvelleQuantite - ing.quantite
        if (abs(ecart) < 0.0005) return
        db.ingredients = db.ingredients.map {
            if (it.id == id) it.copy(quantite = nouvelleQuantite.coerceAtLeast(0.0)) else it
        }
        db.noter(id, ing.nom, TypeMouvementStock.AJUSTEMENT, ecart, motif.ifBlank { "Ajustement après comptage" })
    }

    override suspend fun acheter(a: NouvelAchat): Boolean {
        delay(300.milliseconds)
        val cle = a.nom.cleNom()
        val fournisseur = a.fournisseur.nettoyerNom()
        var cree = false

        var ing = db.ingredients.firstOrNull { it.nom.cleNom() == cle }
        if (ing == null) {
            ing = Ingredient(UUID.randomUUID().toString(), a.nom.nettoyerNom(), a.unite, 0.0, 0.0, "", fournisseur)
            db.ingredients += ing
            cree = true
        }
        val id = ing.id
        db.ingredients = db.ingredients.map {
            if (it.id != id) it
            else it.copy(
                quantite = it.quantite + a.quantite,
                fournisseur = fournisseur.ifBlank { it.fournisseur }
            )
        }
        db.noter(id, ing.nom, TypeMouvementStock.ENTREE, a.quantite, "Achat", a.montant, fournisseur)
        return cree
    }
}