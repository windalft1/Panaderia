package com.example.panaderia

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

class ThemeManager(private val context: Context) {

    // SharedPreferences es un archivo XML donde guardamos datos simples.
    // Lo nombramos "theme_prefs" y lo hacemos privado para esta app.
    private val prefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    // Estas son las claves que usaremos para guardar y leer los datos.
    private val KEY_THEME = "app_theme"

    // Definimos constantes para los posibles temas, para evitar errores de tipeo.
    object Theme {
        const val LIGHT = 0
        const val DARK = 1
        const val SYSTEM = 2 // Podrías añadir un botón para esto en el futuro
    }

    /**
     * Guarda la elección de tema del usuario.
     */
    fun saveTheme(themeMode: Int) {
        prefs.edit().putInt(KEY_THEME, themeMode).apply()
        applyTheme() // Aplica el tema inmediatamente después de guardarlo
    }

    /**
     * Lee el tema guardado. Si es la primera vez que se abre la app,
     * devuelve el tema claro por defecto.
     */
    private fun getSavedTheme(): Int {
        // El segundo parámetro de getInt es el valor por defecto que se devuelve
        // si no se encuentra la clave. Aquí está la clave de tu petición:
        // la primera vez, devolverá LIGHT.
        return prefs.getInt(KEY_THEME, Theme.LIGHT)
    }

    /**
     * Aplica el tema guardado a toda la aplicación.
     * Esta función debe ser llamada al iniciar la app.
     */
    fun applyTheme() {
        when (getSavedTheme()) {
            Theme.LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            Theme.DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            // Aquí podrías manejar el caso del tema del sistema si lo añades en el futuro
            // Theme.SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}
