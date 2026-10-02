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
    suspend fun modifierAchat(mouvementId: String, m: ModificationAchat)
    suspend fun annulerAchat(mouvementId: String)
    suspend fun getFiches(): List<FicheFournisseur>
    suspend fun creerFournisseur(f: NouveauFournisseur)
    /** Crée la fiche si le fournisseur n'en avait pas. Un changement de nom est propagé partout. */
    suspend fun modifierFournisseur(ancienNom: String, f: NouveauFournisseur)
    suspend fun supprimerFournisseur(nom: String)
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
        db.noter(
            id, ing.nom, TypeMouvementStock.ENTREE, a.quantite, "Achat", a.montant, fournisseur,
            estAchat = true, mode = a.mode
        )
        return cree
    }
    override suspend fun modifierAchat(mouvementId: String, m: ModificationAchat) {
        delay(300.milliseconds)
        val mv = db.mouvements.first { it.id == mouvementId }
        check(mv.estAchat && !mv.annule) { "Achat non modifiable" }
        db.mouvements = db.mouvements.map {
            if (it.id == mouvementId)
                it.copy(montant = m.montant, fournisseur = m.fournisseur.nettoyerNom(), mode = m.mode)
            else it
        }
    }

    override suspend fun annulerAchat(mouvementId: String) {
        delay(300.milliseconds)
        val mv = db.mouvements.first { it.id == mouvementId }
        check(mv.estAchat && !mv.annule) { "Achat déjà annulé" }
        val ing = checkNotNull(db.ingredients.firstOrNull { it.id == mv.ingredientId }) { "Ingrédient supprimé" }
        check(ing.quantite + 0.0005 >= mv.quantite) { "Stock insuffisant pour annuler" }

        db.ingredients = db.ingredients.map {
            if (it.id == ing.id) it.copy(quantite = (it.quantite - mv.quantite).coerceAtLeast(0.0)) else it
        }
        db.mouvements = db.mouvements.map { if (it.id == mouvementId) it.copy(annule = true) else it }
        db.noter(ing.id, ing.nom, TypeMouvementStock.SORTIE, -mv.quantite, "Annulation d'achat")
    }
    override suspend fun getFiches() = db.fiches

    override suspend fun creerFournisseur(f: NouveauFournisseur) {
        delay(300.milliseconds)
        require(db.fiches.none { it.nom.cleNom() == f.nom.cleNom() }) { "Fiche déjà existante" }
        db.fiches += FicheFournisseur(
                    UUID.randomUUID().toString(), f.nom.nettoyerNom(), f.telephone,
                    f.adresse.nettoyerNom(), f.delai.nettoyerNom()
                )
    }

    override suspend fun modifierFournisseur(ancienNom: String, f: NouveauFournisseur) {
        delay(300.milliseconds)
        val ancienneCle = ancienNom.cleNom()
        val nouveauNom = f.nom.nettoyerNom()
        val nouvelleCle = nouveauNom.cleNom()
        require(nouvelleCle == ancienneCle || db.fiches.none { it.nom.cleNom() == nouvelleCle }) { "Nom déjà pris" }

        val existante = db.fiches.firstOrNull { it.nom.cleNom() == ancienneCle }
        db.fiches =
            if (existante != null) db.fiches.map {
                if (it.id == existante.id)
                    it.copy(nom = nouveauNom, telephone = f.telephone,
                        adresse = f.adresse.nettoyerNom(), delai = f.delai.nettoyerNom())
                else it
            }
            else db.fiches + FicheFournisseur(
                UUID.randomUUID().toString(), nouveauNom, f.telephone,
                f.adresse.nettoyerNom(), f.delai.nettoyerNom()
            )

        // Le nouveau nom suit partout : ingrédients et achats passés
        db.ingredients = db.ingredients.map {
            if (it.fournisseur.cleNom() == ancienneCle) it.copy(fournisseur = nouveauNom) else it
        }
        db.mouvements = db.mouvements.map {
            if (it.fournisseur.cleNom() == ancienneCle) it.copy(fournisseur = nouveauNom) else it
        }
    }

    override suspend fun supprimerFournisseur(nom: String) {
        delay(300.milliseconds)
        val cle = nom.cleNom()
        db.fiches = db.fiches.filterNot { it.nom.cleNom() == cle }
        db.ingredients = db.ingredients.map {
            if (it.fournisseur.cleNom() == cle) it.copy(fournisseur = "") else it
        }
        // Les mouvements d'achat gardent le nom : c'est l'historique
    }
}