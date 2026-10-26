package com.enonymous1.googlemapspoi

import com.enonymous1.googlemapspoi.data.Directions
import org.junit.Assert.assertEquals
import org.junit.Test

class DirectionsTest {

    @Test
    fun navigationUriUsesGoogleNavigationScheme() {
        assertEquals(
            "google.navigation:q=38.0,-78.5",
            Directions.navigationUri(38.0, -78.5).toString(),
        )
    }

    @Test
    fun mapsUriUsesGeoScheme() {
        assertEquals(
            "geo:38.0,-78.5?q=38.0,-78.5",
            Directions.mapsUri(38.0, -78.5).toString(),
        )
    }
}
