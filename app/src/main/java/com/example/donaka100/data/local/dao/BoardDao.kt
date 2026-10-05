package com.example.donaka100.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BoardDao {

    // --- Encaissé & Sorties du jour ---
    @Query("""
        SELECT SUM(montant) FROM paiements 
        WHERE annule = 0 AND type != 'A_CREDIT' AND dateHeure >= :debut AND dateHeure <= :fin
    """)
    fun getSumEncaissementsPeriode(debut: Long, fin: Long): Flow<Long?>

    @Query("""
        SELECT SUM(montant) FROM depenses 
        WHERE annule = 0 AND dateHeure >= :debut AND dateHeure <= :fin
    """)
    fun getSumDepensesPeriode(debut: Long, fin: Long): Flow<Long?>

    @Query("""
        SELECT SUM(montant) FROM paiements_fournisseur 
        WHERE annule = 0 AND dateHeure >= :debut AND dateHeure <= :fin
    """)
    fun getSumPaiementsFournisseursPeriode(debut: Long, fin: Long): Flow<Long?>

    @Query("""
        SELECT SUM(montant) FROM achats 
        WHERE annule = 0 AND montant IS NOT NULL AND dateHeure >= :debut AND dateHeure <= :fin
    """)
    fun getSumAchatsPeriode(debut: Long, fin: Long): Flow<Long?>

    // --- Globales (Trésorerie, Créances, Dettes) ---
    @Query("SELECT SUM(montant) FROM paiements WHERE annule = 0 AND type != 'A_CREDIT'")
    fun getSumTotalEncaissementsClients(): Flow<Long?>

    @Query("SELECT SUM(montant) FROM depenses WHERE annule = 0")
    fun getSumTotalDepenses(): Flow<Long?>

    @Query("SELECT SUM(montant) FROM paiements_fournisseur WHERE annule = 0")
    fun getSumTotalPaiementsFournisseurs(): Flow<Long?>

    @Query("SELECT SUM(montant) FROM achats WHERE annule = 0 AND montant IS NOT NULL")
    fun getSumTotalAchatsFournisseurs(): Flow<Long?>
}
