package com.example.donaka100.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.donaka100.data.local.DonakaDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "donaka_reglages")

interface ReglagesRepository {
    fun observeReglages(): Flow<Reglages>
    suspend fun setNomChef(nom: String)
    suspend fun setNomBoulangerie(nom: String)
    suspend fun setPremierLancementTermine(termine: Boolean)
    suspend fun effacerTout(database: DonakaDatabase)
}

class DataStoreReglagesRepository(
    private val dataStore: DataStore<Preferences>
) : ReglagesRepository {

    private object Keys {
        val NOM_CHEF = stringPreferencesKey("nom_chef")
        val NOM_BOULANGERIE = stringPreferencesKey("nom_boulangerie")
        val PREMIER_LANCEMENT = booleanPreferencesKey("premier_lancement_termine")
        val HEURE_RAPPEL_SOIR = stringPreferencesKey("heure_rappel_soir")
        val HEURE_LIVRAISON_DEFAUT = stringPreferencesKey("heure_livraison_defaut")
        val NOTIF_PRODUCTION = booleanPreferencesKey("notif_production")
        val NOTIF_COMMANDES = booleanPreferencesKey("notif_commandes")
        val NOTIF_STOCK_ALERTE = booleanPreferencesKey("notif_stock_alerte")
        val NOTIF_PAIEMENTS = booleanPreferencesKey("notif_paiements")
        val NOTIF_RAPPELS_SOIR = booleanPreferencesKey("notif_rappels_soir")
        val DELAI_VERROUILLAGE = intPreferencesKey("delai_verrouillage_minutes")
        val PIN_ACTIVE = booleanPreferencesKey("pin_active")
        val SAUVEGARDE_AUTO = booleanPreferencesKey("sauvegarde_auto_active")
    }

    override fun observeReglages(): Flow<Reglages> {
        return dataStore.data.map { prefs ->
            Reglages(
                nomChef = prefs[Keys.NOM_CHEF] ?: "",
                nomBoulangerie = prefs[Keys.NOM_BOULANGERIE] ?: "",
                premierLancementTermine = prefs[Keys.PREMIER_LANCEMENT] ?: false,
                heureRappelSoir = prefs[Keys.HEURE_RAPPEL_SOIR] ?: "18:00",
                heureLivraisonParDefaut = prefs[Keys.HEURE_LIVRAISON_DEFAUT] ?: "08:00",
                notifProduction = prefs[Keys.NOTIF_PRODUCTION] ?: true,
                notifCommandes = prefs[Keys.NOTIF_COMMANDES] ?: true,
                notifStockAlerte = prefs[Keys.NOTIF_STOCK_ALERTE] ?: true,
                notifPaiements = prefs[Keys.NOTIF_PAIEMENTS] ?: true,
                notifRappelsSoir = prefs[Keys.NOTIF_RAPPELS_SOIR] ?: true,
                delaiVerrouillageMinutes = prefs[Keys.DELAI_VERROUILLAGE] ?: 1,
                pinActive = prefs[Keys.PIN_ACTIVE] ?: false,
                sauvegardeAutoActive = prefs[Keys.SAUVEGARDE_AUTO] ?: false
            )
        }
    }

    override suspend fun setNomChef(nom: String) {
        dataStore.edit { prefs -> prefs[Keys.NOM_CHEF] = nom.trim() }
    }

    override suspend fun setNomBoulangerie(nom: String) {
        dataStore.edit { prefs -> prefs[Keys.NOM_BOULANGERIE] = nom.trim() }
    }

    override suspend fun setPremierLancementTermine(termine: Boolean) {
        dataStore.edit { prefs -> prefs[Keys.PREMIER_LANCEMENT] = termine }
    }

    override suspend fun effacerTout(database: DonakaDatabase) {
        database.clearAllTables()
        dataStore.edit { prefs -> prefs.clear() }
    }
}
