package com.example.donaka100

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.roundToLong

class FourneeTest {
    @Test
    fun testFourneeDeduction() {
        val stockInitial = 1.0
        val besoin = 0.25
        val deduit = Math.min(besoin, stockInitial)
        val stockApresProduction = stockInitial - deduit
        assertEquals(0.75, stockApresProduction, 0.001)

        val stockRestaure = stockApresProduction + deduit
        assertEquals(1.0, stockRestaure, 0.001)
    }

    @Test
    fun testCalculPrixParUniteDecimal() {
        val montant = 10000L
        val quantite = 2.5
        val prixParUnite = (montant.toDouble() / quantite).roundToLong()
        assertEquals(4000L, prixParUnite)
    }
}
