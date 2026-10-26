package com.enonymous1.googlemapspoi.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Poi::class], version = 1, exportSchema = false)
abstract class PoiDatabase : RoomDatabase() {
    abstract fun poiDao(): PoiDao

    companion object {
        fun create(context: Context): PoiDatabase =
            Room.databaseBuilder(context, PoiDatabase::class.java, "poi-database").build()
    }
}
