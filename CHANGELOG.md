# Changelog

## 2.0.0 — 2026-10-26

Modernization on top of the v1 history (2023-12-25):

- Rewrote the app in Kotlin with a single activity, Navigation, ViewModel + StateFlow, and a Room database; the EXIF-as-database model is gone.
- Added the features v1's README promised but its code lacked: search (title, description, category), categories, editing, and directions via Google Maps.
- Added a one-time migration that imports v1 `SAVED` EXIF photos from `Pictures/map_images` into Room, skipping `DELETED` photos and photos without GPS coordinates.
- Fixed v1 bugs: missing FileProvider declaration, uncreated photo folder, commented-out `setLatLong`, the new-marker title reading a different EXIF tag than the form wrote, main-thread image decoding, and `RuntimeException` crashes on IO errors.
- Toolchain: AGP 8.13.0, Gradle 8.14.3, Kotlin DSL + version catalog, compile/target SDK 36, minSdk 24, Java 17, Material 3.
- Package renamed from `com.example.google_maps_poi` to `com.enonymous1.googlemapspoi`.
- Added MIT LICENSE, GitHub Actions CI (unit tests + debug APK), and unit tests for search, directions, and the migration mapping.
- Delete now removes the POI row for real; the photo file itself is kept.

## 1.0 — 2023-12-25

Original Java app: map with photo-based POIs stored as JPEGs in `Pictures/map_images` with title, description, and SAVED/DELETED state in EXIF tags.
