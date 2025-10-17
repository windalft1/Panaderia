# Configuración de Supabase en Panaderia

## ✅ Dependencias Instaladas

Se han añadido todas las dependencias necesarias de Supabase a tu proyecto:

- **Supabase SDK 3.0.3** (BOM para gestión de versiones)
- **Postgrest** - Para operaciones de base de datos
- **Auth** - Para autenticación de usuarios
- **Storage** - Para almacenamiento de archivos
- **Realtime** - Para actualizaciones en tiempo real
- **Ktor Client** - Cliente HTTP para Android
- **Kotlinx Serialization** - Para serialización JSON

## 📋 Cambios Realizados

### 1. Archivos Gradle Actualizados
- `gradle/libs.versions.toml` - Añadidas versiones de Supabase, Ktor y Serialization
- `app/build.gradle.kts` - Añadidas dependencias y plugin de serialización
- `minSdk` actualizado de 24 a 26 (requerido por Supabase)

### 2. Permisos
- Añadido `INTERNET` permission en `AndroidManifest.xml`

### 3. Cliente de Supabase
- Creado `SupabaseClient.kt` con configuración base y ejemplos

## 🚀 Cómo Usar Supabase

### Paso 1: Crear Proyecto en Supabase
1. Ve a [supabase.com](https://supabase.com) y crea una cuenta
2. Crea un nuevo proyecto
3. Ve a Settings > API para obtener:
   - **Project URL** (algo como `https://xxx.supabase.co`)
   - **Anon/Public Key** (clave anónima para el cliente)

### Paso 2: Configurar el Cliente
Edita `app/src/main/java/com/example/panaderia/SupabaseClient.kt`:

```kotlin
val client = createSupabaseClient(
    supabaseUrl = "https://tfkenvefmvsjbuwfzajn.supabase.co",  // Reemplaza aquí
    supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRma2VudmVmbXZzamJ1d2Z6YWpuIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjA1MjE2NzQsImV4cCI6MjA3NjA5NzY3NH0.qW4NxW6XYFao44JFTkp-4DbeWHmm3TG8jRj6iw0RrPI"  // Reemplaza aquí
) {
    install(Postgrest)
    install(Auth)
    install(Storage)
    install(Realtime)
}
```

### Paso 3: Crear Tablas en Supabase

En el SQL Editor de Supabase, crea las tablas para tu app:

```sql
-- Tabla de ingredientes
CREATE TABLE ingredientes (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    nombre TEXT NOT NULL,
    cantidad TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc', NOW())
);

-- Tabla de recetas
CREATE TABLE recetas (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    nombre TEXT NOT NULL,
    porciones INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc', NOW())
);

-- Tabla de ingredientes de receta
CREATE TABLE receta_ingredientes (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    receta_id UUID REFERENCES recetas(id) ON DELETE CASCADE,
    ingrediente_id UUID REFERENCES ingredientes(id),
    cantidad TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc', NOW())
);

-- Tabla de mojes
CREATE TABLE mojes (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    nombre TEXT NOT NULL,
    descripcion TEXT,
    tiempo_inicio TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc', NOW())
);
```

### Paso 4: Ejemplos de Uso

#### Consultar Recetas
```kotlin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

@Serializable
data class Receta(
    val id: String? = null,
    val nombre: String,
    val porciones: Int
)

// En una función suspend o coroutine
suspend fun obtenerRecetas(): List<Receta> = withContext(Dispatchers.IO) {
    val response = SupabaseClient.client
        .from("recetas")
        .select()
        .decodeList<Receta>()
    response
}
```

#### Insertar Nueva Receta
```kotlin
suspend fun crearReceta(nombre: String, porciones: Int) = withContext(Dispatchers.IO) {
    SupabaseClient.client
        .from("recetas")
        .insert(Receta(nombre = nombre, porciones = porciones))
}
```

#### Actualizar Receta
```kotlin
suspend fun actualizarReceta(id: String, nombre: String, porciones: Int) = withContext(Dispatchers.IO) {
    SupabaseClient.client
        .from("recetas")
        .update({
            set("nombre", nombre)
            set("porciones", porciones)
        }) {
            filter {
                eq("id", id)
            }
        }
}
```

#### Eliminar Receta
```kotlin
suspend fun eliminarReceta(id: String) = withContext(Dispatchers.IO) {
    SupabaseClient.client
        .from("recetas")
        .delete {
            filter {
                eq("id", id)
            }
        }
}
```

#### Autenticación (Opcional)
```kotlin
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email

suspend fun registrarUsuario(email: String, password: String) {
    SupabaseClient.client.auth.signUpWith(Email) {
        this.email = email
        this.password = password
    }
}

suspend fun iniciarSesion(email: String, password: String) {
    SupabaseClient.client.auth.signInWith(Email) {
        this.email = email
        this.password = password
    }
}

suspend fun cerrarSesion() {
    SupabaseClient.client.auth.signOut()
}
```

## 📝 Migrar desde JSON Local

Para migrar de los archivos JSON locales a Supabase:

1. **Lee los datos actuales de JSON**
2. **Insértalos en Supabase** usando las funciones de arriba
3. **Actualiza tus ViewModels** para usar Supabase en lugar de archivos JSON

Ejemplo:
```kotlin
// Antes (JSON)
// Leer de archivo recetas.json

// Después (Supabase)
viewModelScope.launch {
    val recetas = obtenerRecetas()
    _recetas.value = recetas
}
```

## ✅ Verificación

La configuración de Gradle ha sido validada. Aunque no podemos compilar completamente en Replit (requiere Android SDK), las dependencias están correctamente configuradas.

Para compilar localmente:
```bash
./gradlew assembleDebug
```

## 📚 Recursos

- [Documentación Supabase Kotlin](https://supabase.com/docs/reference/kotlin/introduction)
- [Guía Supabase Android](https://supabase.com/docs/guides/getting-started/quickstarts/kotlin)
- [GitHub supabase-kt](https://github.com/supabase-community/supabase-kt)

## 🔒 Seguridad

⚠️ **IMPORTANTE**: 
- Nunca subas tu API key al código
- Usa variables de entorno o gestión segura de secretos
- La "anon key" es segura para el cliente, pero configura Row Level Security (RLS) en Supabase para proteger tus datos
