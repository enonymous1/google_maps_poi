 **# Google Maps POI**

**An Android app for managing points of interest on Google Maps**

**Manage your personal points of interest (POIs) directly on Google Maps with ease!**

## Features

* Create, delete, and manage POIs with custom markers and detailed information.
* View POIs on a map with clear visual representation.
* Search for POIs by name or category to quickly find what you need.

## Getting Started

1. **Clone the repository:**

   ```bash
   git clone https://github.com/enonymous1/google_maps_poi.git
   ```

2. **Obtain a Google Maps API key:**

   - Visit the Google Cloud Console: [https://console.cloud.google.com/](https://console.cloud.google.com/)
   - Create a new project or select an existing one.
   - Enable the Google Maps Android API for your project.
   - Create an API key and restrict it to Android apps.

3. **Add your API key to the app:**

   - Open the `local.properties` file in the project's root directory.
   - Add the following line, replacing `YOUR_API_KEY` with your actual API key:

     ```
     MAPS_API_KEY=YOUR_API_KEY
     ```

4. **Build and run the app:**

   - Open the project in Android Studio.
   - Connect an Android device or create an emulator.
   - Run the app and start managing your POIs!

## Usage

- **Creating a POI:**
  - Tap the "+" button on the map to add a new POI.
  - Provide details like name, address, category, and description.
  - Save the POI to view it on the map.
- **Viewing POIs:**
  - Tap the "marker" icon to see a list of saved POIs.
  - Tap a POI to view its details and get directions.
- **Deleting a POI:**
  - Long-press a POI on the map or in the list to remove it.
- **Searching for POIs:**
  - Tap the "search" icon to quickly find specific POIs.
  - Enter search terms to filter your POIs and navigate to them easily.
