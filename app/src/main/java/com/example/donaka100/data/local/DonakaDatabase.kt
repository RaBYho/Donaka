package com.example.donaka100.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.donaka100.data.local.converters.DonakaTypeConverters
import com.example.donaka100.data.local.dao.AchatDao
import com.example.donaka100.data.local.dao.BoardDao
import com.example.donaka100.data.local.dao.ClientDao
import com.example.donaka100.data.local.dao.CommandeDao
import com.example.donaka100.data.local.dao.DepenseDao
import com.example.donaka100.data.local.dao.FourneeDao
import com.example.donaka100.data.local.dao.FournisseurDao
import com.example.donaka100.data.local.dao.PaiementDao
import com.example.donaka100.data.local.dao.StockDao
import com.example.donaka100.data.local.entity.AchatEntity
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
import com.example.donaka100.data.local.entity.PaiementFournisseurEntity
import com.example.donaka100.data.local.entity.PrixIngredientEntity
import com.example.donaka100.data.local.entity.ProduitEntity

@Database(
    entities = [
        IngredientEntity::class,
        PrixIngredientEntity::class,
        ProduitEntity::class,
        LigneRecetteEntity::class,
        FourneeEntity::class,
        LigneFourneeEntity::class,
        ConsommationEntity::class,
        MouvementStockEntity::class,
        ClientEntity::class,
        CommandeEntity::class,
        LigneCommandeEntity::class,
        PaiementEntity::class,
        FournisseurEntity::class,
        AchatEntity::class,
        PaiementFournisseurEntity::class,
        DepenseEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DonakaTypeConverters::class)
abstract class DonakaDatabase : RoomDatabase() {

    abstract fun stockDao(): StockDao
    abstract fun fourneeDao(): FourneeDao
    abstract fun clientDao(): ClientDao
    abstract fun commandeDao(): CommandeDao
    abstract fun paiementDao(): PaiementDao
    abstract fun fournisseurDao(): FournisseurDao
    abstract fun achatDao(): AchatDao
    abstract fun depenseDao(): DepenseDao
    abstract fun boardDao(): BoardDao

    companion object {
        @Volatile
        private var INSTANCE: DonakaDatabase? = null

        fun getDatabase(context: Context): DonakaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DonakaDatabase::class.java,
                    "donaka_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
