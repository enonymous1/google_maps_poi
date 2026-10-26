package com.enonymous1.googlemapspoi

import com.enonymous1.googlemapspoi.data.Poi
import com.enonymous1.googlemapspoi.data.PoiSearch
import org.junit.Assert.assertEquals
import org.junit.Test

class PoiSearchTest {

    private val pois = listOf(
        Poi(title = "Blue Ridge Overlook", description = "Sunset spot", category = "viewpoint", latitude = 38.0, longitude = -78.5),
        Poi(title = "Gus Kitchen", description = "Best breakfast", category = "food", latitude = 38.1, longitude = -78.6),
        Poi(title = "Trailhead", description = "Blue Ridge trail start", category = "", latitude = 38.2, longitude = -78.7),
    )

    @Test
    fun blankQueryReturnsEverything() {
        assertEquals(pois, PoiSearch.filter(pois, "   "))
    }

    @Test
    fun matchesTitleCaseInsensitively() {
        assertEquals(listOf(pois[0]), PoiSearch.filter(pois, "blue ridge overlook"))
    }

    @Test
    fun matchesDescription() {
        assertEquals(listOf(pois[1]), PoiSearch.filter(pois, "breakfast"))
    }

    @Test
    fun matchesCategory() {
        assertEquals(listOf(pois[1]), PoiSearch.filter(pois, "FOOD"))
    }

    @Test
    fun noMatchReturnsEmpty() {
        assertEquals(emptyList<Poi>(), PoiSearch.filter(pois, "nowhere"))
    }

    @Test
    fun normaliseCategoryTrimsWhitespace() {
        assertEquals("food", PoiSearch.normaliseCategory("  food  "))
    }
}
