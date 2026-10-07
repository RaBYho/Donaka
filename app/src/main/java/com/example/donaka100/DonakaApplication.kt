package com.example.donaka100

import android.app.Application
import com.example.donaka100.data.DataStoreReglagesRepository
import com.example.donaka100.data.ReglagesRepository
import com.example.donaka100.data.dataStore
import com.example.donaka100.data.local.DonakaDatabase

class DonakaApplication : Application() {

    val database: DonakaDatabase by lazy {
        DonakaDatabase.getDatabase(this)
    }

    val reglagesRepository: ReglagesRepository by lazy {
        DataStoreReglagesRepository(dataStore)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: DonakaApplication
            private set
    }
}
