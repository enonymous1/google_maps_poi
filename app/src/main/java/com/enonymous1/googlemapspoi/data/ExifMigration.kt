package com.enonymous1.googlemapspoi.data

import androidx.exifinterface.media.ExifInterface
import java.io.File

/**
 * One-time import of v1 POIs.
 *
 * v1 stored a POI as a JPEG in Pictures/map_images with:
 * - title in TAG_IMAGE_UNIQUE_ID
 * - description in TAG_IMAGE_DESCRIPTION
 * - "SAVED" (vs "DELETED") in TAG_MAKER_NOTE
 * - coordinates in the EXIF GPS tags
 *
 * Files marked DELETED, files with no GPS coordinates, and unreadable files
 * are skipped, never imported. Imported rows keep pointing at the original
 * photo file, so no photo is copied or moved. This runs at most once per
 * install (see PoiRepository); after that, Room is the only source of truth.
 */
object ExifMigration {
    /** The legacy public folder v1 wrote to. */
    const val LEGACY_FOLDER_NAME = "map_images"

    data class LegacyPhoto(
        val title: String?,
        val description: String?,
        val latitude: Double?,
        val longitude: Double?,
        val photoPath: String,
    )

    /** Pure mapping, unit-testable without touching a real file. */
    fun toPoi(legacy: LegacyPhoto): Poi? {
        val latitude = legacy.latitude ?: return null
        val longitude = legacy.longitude ?: return null
        return Poi(
            title = legacy.title?.takeIf { it.isNotBlank() }
                ?: File(legacy.photoPath).nameWithoutExtension,
            description = legacy.description.orEmpty(),
            category = "",
            latitude = latitude,
            longitude = longitude,
            photoPath = legacy.photoPath,
        )
    }

    fun scan(folder: File): List<Poi> {
        val files = folder.listFiles() ?: return emptyList()
        return files.mapNotNull { file ->
            runCatching {
                val exif = ExifInterface(file.absolutePath)
                if (exif.getAttribute(ExifInterface.TAG_MAKER_NOTE) != "SAVED") {
                    return@runCatching null
                }
                val latLong = exif.latLong ?: return@runCatching null
                toPoi(
                    LegacyPhoto(
                        title = exif.getAttribute(ExifInterface.TAG_IMAGE_UNIQUE_ID),
                        description = exif.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION),
                        latitude = latLong.getOrNull(0),
                        longitude = latLong.getOrNull(1),
                        photoPath = file.absolutePath,
                    )
                )
            }.getOrNull()
        }
    }
}
