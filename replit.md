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

## How to Work With This Project in Replit

### What You Can Do in Replit
- **View and Edit Code**: Browse and modify Kotlin source files
- **Update Resources**: Edit layouts (XML), drawables, and string resources
- **View Project Info**: The workflow displays project structure and build instructions
- **Review Dependencies**: Examine build.gradle.kts files and dependency configuration

### What You Cannot Do in Replit
- Run the Android app (no emulator support)
- Test UI functionality
- Debug runtime behavior
- Install the APK

## How to Actually Run This App

### Local Development (Recommended)
1. Clone this repository to your local machine
2. Open in Android Studio
3. Sync Gradle files
4. Run on an Android emulator or physical device

### Build Commands (Local Machine)
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK  
./gradlew assembleRelease

# Run tests
./gradlew test
```

## Recent Changes
- **2025-10-15**: Project imported to Replit from GitHub
  - Created comprehensive documentation (README.md, replit.md)
  - Set up informational workflow displaying project structure
  - Installed Java/Gradle tooling for future development needs
  - Configured environment for code editing and review

## User Preferences
- None specified yet

## Notes
This is a fully-featured Android recipe management app with theme support, data persistence, and material design UI. While the code can be viewed and edited in Replit, actual execution requires an Android environment.
