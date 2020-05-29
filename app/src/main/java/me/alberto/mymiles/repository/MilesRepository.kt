package me.alberto.mymiles.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.alberto.mymiles.database.Miles
import me.alberto.mymiles.database.MilesDatabase

class MilesRepository(private val milesDatabase: MilesDatabase) {
    fun getMiles() = milesDatabase.milesDao.getMiles()

    suspend fun addMiles(miles: Miles) {
        withContext(Dispatchers.IO) {
            milesDatabase.milesDao.addMiles(miles)
        }
    }
}