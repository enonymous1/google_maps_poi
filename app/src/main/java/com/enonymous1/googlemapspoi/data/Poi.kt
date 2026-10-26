package com.enonymous1.googlemapspoi.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A point of interest. This replaces the v1 storage model, in which a POI
 * only existed as a JPEG in Pictures/map_images with its title, description
 * and SAVED/DELETED state hidden in EXIF tags. A POI now has a real row:
 * it can be edited, searched, categorised and actually deleted.
 */
@Entity(tableName = "pois")
data class Poi(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val category: String = "",
    val latitude: Double,
    val longitude: Double,
    /** Absolute path of the POI photo, or null for a photo-less POI. */
    val photoPath: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)
