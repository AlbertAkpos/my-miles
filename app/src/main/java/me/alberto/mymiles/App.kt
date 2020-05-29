package me.alberto.mymiles

import android.app.Application
import me.alberto.mymiles.database.MilesDatabase

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        MilesDatabase.initDatabase(this)
    }
}