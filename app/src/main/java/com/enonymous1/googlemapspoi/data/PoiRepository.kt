package com.enonymous1.googlemapspoi.data

import android.content.SharedPreferences
import android.os.Environment
import kotlinx.coroutines.flow.Flow
import java.io.File

class PoiRepository(
    private val dao: PoiDao,
    private val prefs: SharedPreferences,
) {
    fun observeAll(): Flow<List<Poi>> = dao.observeAll()

    suspend fun getById(id: Long): Poi? = dao.getById(id)

    suspend fun upsert(poi: Poi): Long = dao.upsert(poi)

    suspend fun delete(poi: Poi) = dao.delete(poi)

    /**
     * Imports v1 EXIF POIs exactly once. The flag lives in SharedPreferences
     * (not "is the database empty?") so that deleting every POI afterwards
     * does not resurrect the legacy folder on the next launch.
     */
    @Suppress("DEPRECATION")
    suspend fun migrateLegacyIfNeeded() {
        if (prefs.getBoolean(KEY_LEGACY_MIGRATED, false)) return
        val folder = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            ExifMigration.LEGACY_FOLDER_NAME,
        )
        val imported = ExifMigration.scan(folder)
        if (imported.isNotEmpty()) {
            dao.insertAll(imported)
        }
        prefs.edit().putBoolean(KEY_LEGACY_MIGRATED, true).apply()
    }

    companion object {
        const val KEY_LEGACY_MIGRATED = "legacy_exif_migrated"
    }
}
