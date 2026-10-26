package com.enonymous1.googlemapspoi

import com.enonymous1.googlemapspoi.data.ExifMigration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ExifMigrationTest {

    @Test
    fun photoWithoutCoordinatesIsNotImported() {
        val legacy = ExifMigration.LegacyPhoto(
            title = "Somewhere",
            description = "No GPS",
            latitude = null,
            longitude = null,
            photoPath = "/Pictures/map_images/IMG_1.jpg",
        )
        assertNull(ExifMigration.toPoi(legacy))
    }

    @Test
    fun titleFallsBackToFileNameWhenExifTitleIsMissing() {
        val poi = ExifMigration.toPoi(
            ExifMigration.LegacyPhoto(
                title = null,
                description = "Desc",
                latitude = 38.0,
                longitude = -78.5,
                photoPath = "/Pictures/map_images/JPEG_20231225_120000_.jpg",
            )
        )
        assertEquals("JPEG_20231225_120000_", poi?.title)
        assertEquals(38.0, poi?.latitude)
        assertEquals(-78.5, poi?.longitude)
    }

    @Test
    fun missingDescriptionBecomesEmptyString() {
        val poi = ExifMigration.toPoi(
            ExifMigration.LegacyPhoto(
                title = "Titled",
                description = null,
                latitude = 38.0,
                longitude = -78.5,
                photoPath = "/Pictures/map_images/IMG_1.jpg",
            )
        )
        assertEquals("", poi?.description)
        assertEquals("/Pictures/map_images/IMG_1.jpg", poi?.photoPath)
    }
}
