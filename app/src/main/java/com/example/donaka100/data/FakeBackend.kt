package com.example.donaka100.data

import java.time.LocalDate
import java.time.LocalTime

object FakeBackend {
    var clients: List<Client> = emptyList()
    var encaissements: List<Encaissement> = emptyList()
    var commandes: List<Commande> = emptyList()
    var produits: List<Produit> = emptyList()
    var ingredients: List<Ingredient> = emptyList()   // provisoire, géré par Stock plus tard
    var fournees: List<Fournee> = emptyList()
    var compteur: Int = 0

    private var demoCharge = false

    fun chargerDemo() {
        if (demoCharge) return
        demoCharge = true

        ingredients = listOf(
            Ingredient("i1", "Farine T55", UniteStock.KG, 45.0, 50.0),
            Ingredient("i2", "Sucre Blanc", UniteStock.KG, 10.0, 5.0),
            Ingredient("i3", "Beurre 82%", UniteStock.KG, 1.5, 3.0),
            Ingredient("i4", "Levure", UniteStock.KG, 2.0, 0.5)
        )

        produits = listOf(
            Produit("p1", "Gâteau Chocolat", 800, 1000, 10,
                listOf(LigneRecette("i1", 1.2), LigneRecette("i2", 1.0), LigneRecette("i3", 0.8)),
                categorie = "Pâtisserie", pivotId = "i1"),
            Produit("p2", "Baguette Tradition", 400, 500, 20,
                listOf(LigneRecette("i1", 5.0), LigneRecette("i4", 0.1)),
                categorie = "Boulangerie", pivotId = "i1"),
            Produit("p3", "Brioche Pur Beurre", 600, 800, 12,
                listOf(LigneRecette("i1", 1.5), LigneRecette("i2", 0.3), LigneRecette("i3", 0.9)),
                categorie = "Viennoiserie", pivotId = "i1")
        )

        val auj = LocalDate.now()
        val hier = auj.minusDays(1)
        val demain = auj.plusDays(1)
        val (choco, bag, bri) = produits
        fun l(p: Produit, q: Int) = LigneArticle(p.id, p.nom, q, p.prixGros)

        clients = listOf(
            Client("1", "Épicerie Toky", "0341234567", "Tanambao", 200_000, 150_000,
                "Livraison de 60 baguettes", TypeClient.EPICERIE),
            Client("2", "Boulangerie Soa", "0329876543", "Centre Ville", 0, 0, "", TypeClient.BOULANGERIE),
            Client("3", "Hôtel Rest. Ravinala", "0331122233", "Bord de Mer", 300_000, 270_000,
                "Commande viennoiseries buffet", TypeClient.RESTAURANT),
            Client("4", "Kiosque Ankorondrano", "0347654321", "Ankorondrano"),
            Client("5", "Supérette Mahamasina", "0348889900", "Mahamasina")
        )

        commandes = listOf(
            Commande("c1", "1", "Épicerie Toky", false, auj, "10:00", listOf(l(choco, 10), l(bag, 20))),
            Commande("c2", "4", "Kiosque Ankorondrano", false, auj, "11:00", listOf(l(bag, 25))),
            Commande("c3", "5", "Supérette Mahamasina", false, auj, "", listOf(l(bri, 15), l(choco, 10))),
            Commande("c4", "2", "Boulangerie Soa", false, auj, "08:30",
                listOf(l(choco, 5), l(bag, 10), l(bri, 5)),
                LocalTime.of(8, 30), 11_000, ModeReglement.ESPECES),
            Commande("c5", "3", "Hôtel Rest. Ravinala", false, auj, "07:45",
                listOf(l(bag, 15), l(bri, 10)),
                LocalTime.of(7, 45), 12_000, ModeReglement.BANCAIRE),
            Commande("c6", "2", "Boulangerie Soa", false, demain, "", listOf(l(choco, 10), l(bag, 15))),
            Commande("c7", "3", "Hôtel Rest. Ravinala", false, demain, "", listOf(l(bag, 30), l(bri, 10)))
        )

        encaissements = listOf(
            Encaissement("e1", "ENC-0001", "Boulangerie Soa", auj.atTime(8, 30), ModeReglement.ESPECES,
                11_000, "Livraison : 5× Gâteau Chocolat, 10× Baguette Tradition, 5× Brioche Pur Beurre",
                TypeMouvement.ENCAISSE, "c4"),
            Encaissement("e2", "ENC-0002", "Hôtel Rest. Ravinala", auj.atTime(7, 45), ModeReglement.BANCAIRE,
                12_000, "Livraison : 15× Baguette Tradition, 10× Brioche Pur Beurre",
                TypeMouvement.ENCAISSE, "c5"),
            Encaissement("e3", "ENC-0003", "Épicerie Toky", auj.atTime(14, 15), ModeReglement.MVOLA,
                50_000, "Règlement partiel sur livraison 60 baguettes", TypeMouvement.REGLEMENT),
            Encaissement("e4", "ENC-0004", "Boulangerie Soa", hier.atTime(16, 45), ModeReglement.ESPECES,
                100_000, "Acompte commande événement week-end")
        )
        compteur = 4

        fournees = listOf(
            Fournee(
                "f1", auj, auj.atTime(4, 30),
                listOf(LigneFournee("p2", "Baguette Tradition", 62, 60)),
                listOf(
                    Consommation("i1", "Farine T55", UniteStock.KG, 15.5, 15.5),
                    Consommation("i4", "Levure", UniteStock.KG, 0.31, 0.31)
                )
            ),
            Fournee(
                "f2", hier, hier.atTime(5, 30),
                listOf(
                    LigneFournee("p2", "Baguette Tradition", 40, 40),
                    LigneFournee("p3", "Brioche Pur Beurre", 50, 48),
                    LigneFournee("p1", "Gâteau Chocolat", 10, 10)
                ),
                listOf(
                    Consommation("i1", "Farine T55", UniteStock.KG, 18.5, 18.5),
                    Consommation("i3", "Beurre 82%", UniteStock.KG, 4.2, 4.2),
                    Consommation("i2", "Sucre Blanc", UniteStock.KG, 2.0, 2.0)
                )
            ),
            Fournee(
                "f3", auj.minusDays(3), auj.minusDays(4).atTime(20, 15),
                listOf(LigneFournee("p2", "Baguette Tradition", 35, 35)),
                listOf(Consommation("i1", "Farine T55", UniteStock.KG, 8.75, 8.75))
            ),
            Fournee(
                "f4", auj.minusDays(5), auj.minusDays(5).atTime(5, 0),
                listOf(LigneFournee("p1", "Gâteau Chocolat", 15, 15)),
                listOf(Consommation("i1", "Farine T55", UniteStock.KG, 1.8, 1.8)),
                annuleeA = auj.minusDays(5).atTime(7, 0)
            ),
            Fournee(
                "f5", auj.minusMonths(1), auj.minusMonths(1).atTime(5, 30),
                listOf(LigneFournee("p3", "Brioche Pur Beurre", 48, 48)),
                listOf(Consommation("i1", "Farine T55", UniteStock.KG, 6.0, 6.0))
            )
        )
    }
}