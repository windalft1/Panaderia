package com.example.panaderia

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.realtime.Realtime

/**
 * Configuración del cliente de Supabase
 * 
 * Para usar este cliente:
 * 1. Reemplaza SUPABASE_URL con la URL de tu proyecto Supabase
 * 2. Reemplaza SUPABASE_KEY con tu clave anónima (anon key) de Supabase
 * 
 * Puedes obtener estos valores desde tu dashboard de Supabase:
 * https://app.supabase.com/project/_/settings/api
 */
object SupabaseClient {
    
    val client = createSupabaseClient(
        supabaseUrl = "https://tfkenvefmvsjbuwfzajn.supabase.co",  // Reemplaza aquí
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRma2VudmVmbXZzamJ1d2Z6YWpuIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjA1MjE2NzQsImV4cCI6MjA3NjA5NzY3NH0.qW4NxW6XYFao44JFTkp-4DbeWHmm3TG8jRj6iw0RrPI"  // Reemplaza aquí
    ) {
        install(Postgrest)  // Para operaciones de base de datos
        //install(Auth)       // Para autenticación
        install(Storage)    // Para almacenamiento de archivos
        install(Realtime)   // Para actualizaciones en tiempo real
    }
}

/**
 * Ejemplo de uso del cliente Supabase:
 * 
 * // Para consultar datos:
 * val response = SupabaseClient.client.from("recetas").select()
 * 
 * // Para insertar datos:
 * SupabaseClient.client.from("recetas").insert(mapOf(
 *     "nombre" to "Pan de masa madre",
 *     "porciones" to 8
 * ))
 * 
 * // Para autenticación:
 * SupabaseClient.client.auth.signInWith(Email) {
 *     email = "usuario@ejemplo.com"
 *     password = "contraseña"
 * }
 */
