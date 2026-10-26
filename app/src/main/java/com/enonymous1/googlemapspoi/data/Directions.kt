package com.enonymous1.googlemapspoi.data

import android.net.Uri

/** Builds the intent URIs for handing a POI to Google Maps navigation. */
object Directions {
    fun navigationUri(latitude: Double, longitude: Double): Uri =
        Uri.parse("google.navigation:q=$latitude,$longitude")

    fun mapsUri(latitude: Double, longitude: Double): Uri =
        Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude")
}
