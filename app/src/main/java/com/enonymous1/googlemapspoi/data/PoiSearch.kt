package com.enonymous1.googlemapspoi.data

/** Pure search logic, kept out of the UI so it is unit-testable without Android. */
object PoiSearch {
    fun filter(pois: List<Poi>, query: String): List<Poi> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return pois
        val needle = trimmed.lowercase()
        return pois.filter { poi ->
            poi.title.lowercase().contains(needle) ||
                poi.description.lowercase().contains(needle) ||
                poi.category.lowercase().contains(needle)
        }
    }

    fun normaliseCategory(raw: String): String = raw.trim()
}
