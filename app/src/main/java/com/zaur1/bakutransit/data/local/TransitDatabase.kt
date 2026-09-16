package com.zaur1.bakutransit.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [BusRouteEntity::class, TransitStopEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(TransitConverters::class)
abstract class TransitDatabase : RoomDatabase() {
    abstract fun transitDao(): TransitDao

    companion object {
        @Volatile
        private var INSTANCE: TransitDatabase? = null

        fun getDatabase(context: Context): TransitDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TransitDatabase::class.java,
                    "transit_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
