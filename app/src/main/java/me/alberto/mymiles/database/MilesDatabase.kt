package me.alberto.mymiles.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Miles::class], version = 1)
@TypeConverters(Converter::class)
abstract class MilesDatabase : RoomDatabase() {
    abstract val milesDao: MilesDao

    companion object {
        private lateinit var INSTANCE: MilesDatabase
        fun initDatabase(context: Context): MilesDatabase {
            synchronized(MilesDatabase::class) {
                if (!Companion::INSTANCE.isInitialized) {
                    INSTANCE = Room.databaseBuilder(
                        context,
                        MilesDatabase::class.java,
                        "miles-database"
                    ).build()
                }
            }
            return INSTANCE
        }

        fun getDatabase(): MilesDatabase {
            return INSTANCE
        }
    }
}