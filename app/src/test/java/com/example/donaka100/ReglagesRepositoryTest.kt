package com.example.donaka100

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.example.donaka100.data.DataStoreReglagesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ReglagesRepositoryTest {

    @get:Rule
    val tmpFolder = TemporaryFolder()

    @Test
    fun testEcritureEtLectureReglagesDataStore() = runBlocking {
        val testFile = tmpFolder.newFile("test_reglages.preferences_pb")
        val testDataStore = PreferenceDataStoreFactory.create(
            produceFile = { testFile }
        )
        val repository = DataStoreReglagesRepository(testDataStore)

        // 1. Lecture initiale par défaut
        val initial = repository.observeReglages().first()
        assertEquals("", initial.nomChef)
        assertEquals("", initial.nomBoulangerie)
        assertFalse(initial.premierLancementTermine)
        assertEquals("18:00", initial.heureRappelSoir)
        assertEquals("08:00", initial.heureLivraisonParDefaut)
        assertTrue(initial.notifProduction)

        // 2. Écriture du nom du chef et du nom de la boulangerie
        repository.setNomChef("Hoby")
        repository.setNomBoulangerie("Boulangerie Artisanale Donaka")
        repository.setPremierLancementTermine(true)

        // 3. Re-lecture et assertions
        val modifie = repository.observeReglages().first()
        assertEquals("Hoby", modifie.nomChef)
        assertEquals("Boulangerie Artisanale Donaka", modifie.nomBoulangerie)
        assertTrue(modifie.premierLancementTermine)
    }
}
