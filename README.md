# Panaderia - Android Bakery Management App

A Kotlin-based Android application for managing bakery recipes, ingredients, orders, and production batches.

## Features

- 📝 **Recipe Management**: Create, view, and edit recipes with ingredients and portions
- 🛒 **Order Tracking**: Manage customer orders ("pedidos")
- 🥖 **Batch Management**: Track "mojes" (batches/mixes) and timing
- 🌙 **Theme Support**: Switch between dark and light modes
- 💾 **Local Storage**: JSON-based data persistence

## Technical Details

- **Language**: Kotlin 2.0.21
- **Build System**: Gradle 8.13.0
- **UI**: Jetpack Compose + Material Design 3
- **Min SDK**: Android 7.0 (API 24)
- **Target SDK**: Android 36

## Project Structure

```
panaderia/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/panaderia/
│   │   │   ├── MainActivity.kt
│   │   │   ├── recetas.kt
│   │   │   ├── pedidos.kt
│   │   │   ├── mojes.kt
│   │   │   ├── nueva_receta.kt
│   │   │   ├── ThemeManager.kt
│   │   │   └── ui/theme/
│   │   ├── res/
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle/
└── build.gradle.kts
```

## Data Models

- **Ingrediente**: Ingredient with name and quantity
- **Receta**: Recipe with name, portions, and ingredients list
- **RecetaWrapper**: JSON wrapper for recipe data

## Building the App

### Prerequisites
- Android Studio Arctic Fox or later
- JDK 11 or later
- Android SDK with API 36

### Steps
1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Run on an emulator or physical device

### Build Commands
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run tests
./gradlew test
```

## Data Storage

The app stores data in JSON files within the app's internal storage:
- `recetas.json` - All recipes
- `mojes.json` - Batch information

## Development in Replit

⚠️ **Note**: This is an Android application that cannot run directly in Replit's web environment. Replit does not support Android emulators. 

### What You Can Do in Replit:
- ✅ View and edit the source code
- ✅ Modify layouts and resources
- ✅ Manage the project structure
- ✅ Review and refactor code
- ✅ View project information and structure

### What You Cannot Do in Replit:
- ❌ Run the Android app (no emulator support)
- ❌ Test UI functionality
- ❌ Debug runtime behavior
- ❌ Install or test APK files

For actual testing and execution, use Android Studio on a local machine.

## License

This project appears to be a personal/educational project. Please add appropriate license information if needed.
