package com.enonymous1.googlemapspoi

import android.app.Application
import android.content.Context
import com.enonymous1.googlemapspoi.data.PoiDatabase
import com.enonymous1.googlemapspoi.data.PoiRepository

class PoiApplication : Application() {
    val database: PoiDatabase by lazy { PoiDatabase.create(this) }

    val repository: PoiRepository by lazy {
        PoiRepository(
            database.poiDao(),
            getSharedPreferences("poi_prefs", Context.MODE_PRIVATE),
        )
    }
}
