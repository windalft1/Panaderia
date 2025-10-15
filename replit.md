# Panaderia (Bakery App)

## Overview
This is an Android mobile application built with Kotlin and Jetpack Compose for managing bakery recipes, ingredients, and orders. The app is called "Panaderia" and provides functionality for:
- Managing recipes with ingredients and portions
- Tracking orders ("pedidos")
- Managing "mojes" (batches/mixes) and timing
- Dark/Light theme support
- Local JSON-based data storage

## Project Type
**Android Mobile Application** - This project is NOT a web application and cannot run directly in a web browser or Replit's standard environment.

## Technology Stack
- **Language**: Kotlin 2.0.21
- **Build System**: Gradle 8.13.0
- **UI Framework**: Jetpack Compose + Material Design 3
- **Min SDK**: Android 7.0 (API 24)
- **Target SDK**: Android 36
- **Dependencies**:
  - AndroidX Core, Lifecycle, AppCompat
  - Material Components
  - Gson for JSON parsing
  - Jetpack Compose BOM 2024.09.00

## Project Structure
```
app/src/main/
├── java/com/example/panaderia/
│   ├── MainActivity.kt          # Main activity with navigation drawer
│   ├── recetas.kt              # Recipes fragment
│   ├── pedidos.kt              # Orders fragment
│   ├── mojes.kt                # Batches/mixes fragment
│   ├── nueva_receta.kt         # New recipe creation
│   ├── ThemeManager.kt         # Dark/light theme management
│   └── ui/theme/               # Compose theme files
├── res/                        # Resources (layouts, drawables, etc.)
└── AndroidManifest.xml         # App configuration
```

## Data Models
- **Ingrediente**: Contains ingredient name and quantity
- **Receta**: Recipe with name, portions, and list of ingredients
- **RecetasViewModel**: ViewModel for managing recipe data across fragments

## Local Data Storage
The app uses local JSON files stored in the app's internal storage:
- `recetas.json` - Stores all recipes
- `mojes.json` - Stores batch/mix information

## Limitations on Replit
⚠️ **Important**: This Android application cannot be run or previewed directly in Replit because:
1. Replit does not support Android emulators
2. Android apps require specific hardware/emulator to run
3. The project is designed for mobile devices, not web browsers

## How to Work With This Project

### Option 1: Build APK (if Android SDK tools available)
If Android SDK is available, you could build an APK file, but this would require significant tooling setup.

### Option 2: View and Edit Code
You can browse and edit the Kotlin source code, modify layouts, update resources, and manage the project structure in Replit, but cannot run the app.

### Option 3: Development on Local Machine
To properly develop and test this app:
1. Clone this repository to your local machine
2. Open in Android Studio
3. Run on an Android emulator or physical device

## Recent Changes
- **2025-10-15**: Project imported to Replit from GitHub
- Documentation created for project understanding

## User Preferences
- None specified yet

## Notes
This is a fully-featured Android recipe management app with theme support, data persistence, and material design UI. While the code can be viewed and edited in Replit, actual execution requires an Android environment.
