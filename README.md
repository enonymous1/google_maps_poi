# Google Maps POI

[![Android CI](https://github.com/enonymous1/google_maps_poi/actions/workflows/android.yml/badge.svg)](https://github.com/enonymous1/google_maps_poi/actions/workflows/android.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

**Your places, pinned on a map, with a photo, a category, and directions — stored in a real database, not in photo metadata.**

Long-press the map (or tap +) to drop a point of interest, give it a title, description, and optional category, snap a photo, and save. Tap a marker to see its details, edit it, get directions to it, or delete it. Search filters markers by title, description, or category.

## Why this exists

The v1 app (Dec 2023) had a clever idea and a fragile implementation: a POI only existed as a JPEG in `Pictures/map_images`, with its title, description, and SAVED/DELETED state hidden in EXIF tags. That meant no editing, no real delete, no search or categories despite the README promising them, and coordinates that depended on the camera app's GPS because the app's own `setLatLong` call was commented out. v2 keeps the idea and replaces the storage with Room, so a POI is a row you can actually manage.

## Quick start

1. Clone the repository:

   ```bash
   git clone https://github.com/enonymous1/google_maps_poi.git
   ```

2. Get a Google Maps API key: in the [Google Cloud Console](https://console.cloud.google.com/), enable **Maps SDK for Android**, and create an API key restricted to Android apps, to this package (`com.enonymous1.googlemapspoi`) and your debug/release SHA-1 fingerprints.
3. Put the key in `local.properties` in the project root (this file is git-ignored, never commit it):

   ```
   MAPS_API_KEY=YOUR_API_KEY
   ```

   The secrets Gradle plugin injects it into the manifest. v1 left the manifest value empty, so the old README step could never actually enable the map.
4. Open the project in Android Studio, connect a device or emulator with Google Play services, and run it.

## Usage

- **Create:** long-press the map, or tap + to use your current location. Fill in title and description, add a category if you want one, snap a photo with the camera button, and save.
- **View:** tap a marker, then tap its info window for the full details and photo.
- **Search:** tap the search icon on the map toolbar and type; markers filter live by title, description, or category.
- **Edit:** from details, tap the edit icon. Location stays with the POI; title, description, category, and photo can change.
- **Directions:** from details, tap Get directions. This opens Google Maps navigation, falling back to a generic `geo:` intent if the Maps app is not installed.
- **Delete:** from details, tap delete and confirm. This deletes the POI row only — the photo file itself is kept, so deleting a POI can never destroy a photo that predates the database.

## Migrating from v1

On first launch, v2 scans the old `Pictures/map_images` folder once and imports any photo whose EXIF `MAKER_NOTE` is `SAVED` and which has GPS coordinates, using its EXIF title and description. Photos marked `DELETED` and photos without coordinates are skipped, and nothing is copied or moved — imported rows keep pointing at the original files. A flag in SharedPreferences records that the import ran, so deleting every POI afterwards does not resurrect the old folder. After that, Room is the only source of truth. The same import also covers the 2025 CE01 variant, which used the same folder and EXIF tags and wrote its own GPS coordinates, so its SAVED photos import the same way. New photos go to the app's own external Pictures directory (`poi_photos`), which needs no storage permission, and are shared with the camera app through a properly declared FileProvider (v1 called `FileProvider.getUriForFile` with no provider in its manifest).

## What changed in v2.0.0

- Java → Kotlin, single-activity app with Navigation, ViewModel + StateFlow, and a Room database.
- Toolchain: AGP 8.13.0, Gradle 8.14.3, Kotlin DSL + version catalog, compile/target SDK 36, minSdk 24, Java 17.
- Location: Fused Location Provider instead of GPS-only `LocationManager`; no leaked location updates.
- Images: Coil loads photos off the main thread instead of full-size `BitmapFactory.decodeFile` on it.
- Material 3, edge-to-edge, and a real dark theme.
- Package renamed from the placeholder `com.example.google_maps_poi` to `com.enonymous1.googlemapspoi`.
- Tests: unit tests for search, directions URIs, and the EXIF migration mapping. CI runs `testDebugUnitTest` and builds a debug APK on every push to `main`.

<details>
<summary>Architecture</summary>

- `data/Poi.kt`, `PoiDao.kt`, `PoiDatabase.kt` — Room entity, DAO, database (version 1).
- `data/PoiRepository.kt` — data access plus the once-only legacy migration gate.
- `data/ExifMigration.kt` — scans the v1 folder and maps legacy EXIF photos to `Poi` rows.
- `data/PoiSearch.kt`, `data/Directions.kt` — pure, unit-tested search filtering and directions URIs.
- `ui/PoiViewModel.kt` — exposes the filtered POI list as a StateFlow.
- `ui/map/MapFragment.kt` — map, markers, search, Fused Location.
- `ui/form/FormFragment.kt` — create/edit form, camera capture via `TakePicture` + FileProvider.
- `ui/details/DetailsFragment.kt` — details, edit, delete, directions.

</details>

## Non-goals

- Not a social or shared map: POIs are local to the device, there is no account, sync, or server.
- Not a photo manager: deleting a POI deliberately does not delete its photo file.
- Not a navigation app: directions hand off to Google Maps rather than reimplementing routing.

## License

MIT — see [LICENSE](LICENSE).
