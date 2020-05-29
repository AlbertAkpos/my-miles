package me.alberto.mymiles.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface MilesDao {
    @Query("SELECT * from distance_table")
    fun getMiles(): LiveData<List<Miles>>

    @Insert
    suspend fun addMiles(farmer: Miles)

}