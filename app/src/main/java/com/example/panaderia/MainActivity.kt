package com.example.panaderia

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import java.io.File
import android.widget.LinearLayout
import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import android.view.View
import androidx.lifecycle.Observer
import androidx.core.content.ContextCompat
import android.graphics.Color
import androidx.constraintlayout.widget.ConstraintLayout
import android.widget.EditText

data class Ingrediente(
    val ingrediente: String,
    val cantidad: String
)

data class Receta(
    val nombre: String,
    val porciones: String, // 👈 cambia a String porque en el JSON está entre comillas
    val ingredientes: List<Ingrediente>
)

// El archivo JSON tiene un array de objetos con una propiedad "receta"
data class RecetaWrapper(
    val receta: Receta
)

// 👇 2️⃣ ViewModel (también afuera del fragment)
class RecetasViewModel : ViewModel() {
    var receta: List<Receta>? = null
}
class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var themeManager: ThemeManager

    private lateinit var viewModel: RecetasViewModel
    private lateinit var viewModel2: RecetasViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔹 Crear o recuperar el ViewModel clásico
        viewModel = ViewModelProvider(this)[RecetasViewModel::class.java]
        viewModel2 = ViewModelProvider(this)[RecetasViewModel::class.java]

        // 🔹 Leer JSON solo si no se ha cargado antes y da vacio si no hay recetas
        try {
            if (viewModel.receta == null) {
                /*val json = assets.open("recetas.json")
                    .bufferedReader()
                    .use { it.readText() }*/
                val file = File(filesDir, "recetas.json")
                val json = file.readText()
                if (!file.exists()) {
                    Log.d("LecturaJSON", "No se encontró el archivo")
                }else{
                    Log.d("LecturaJSON", "Se encontró el archivo $json")
                }
                val gson = Gson()
                val respuesta = gson.fromJson(json, Array<RecetaWrapper>::class.java).toList()
                viewModel.receta = respuesta.map { it.receta }
            }
        }catch (e: Exception){
            viewModel.receta = emptyList()
        }
        Log.d("LecturaJSON", "ViewModel cargado con ${viewModel.receta?.size} recetas. Contenido: ${viewModel.receta}")

        try {
            if (viewModel2.receta == null) {
                /*val json = assets.open("recetas.json")
                    .bufferedReader()
                    .use { it.readText() }*/
                val file = File(filesDir, "mojes.json")
                val json = file.readText()
                if (!file.exists()) {
                    Log.d("LecturaJSON", "No se encontró el archivo")
                }else{
                    Log.d("LecturaJSON", "Se encontró el archivo $json")
                }
                val gson = Gson()
                val respuesta = gson.fromJson(json, Array<RecetaWrapper>::class.java).toList()
                viewModel2.receta = respuesta.map { it.receta }
            }
        }catch (e: Exception){
            viewModel2.receta = emptyList()
        }


        // --- INICIO DE LOS CAMBIOS DE TEMA ---
        // 2. INICIALIZA Y APLICA EL TEMA GUARDADO ANTES DE NADA
        themeManager = ThemeManager(this)
        themeManager.applyTheme()
        // --- FIN DE LOS CAMBIOS DE TEMA ---
        setContentView(R.layout.main_layout)

        // --- Configuración de Toolbar y Drawer ---
        val topAppBar: MaterialToolbar = findViewById(R.id.topAppBar)
        setSupportActionBar(topAppBar)

        drawerLayout = findViewById(R.id.drawer_layout)

        val navigationViewTop: NavigationView = findViewById(R.id.navigation_view_top)
        val iconDarkMode: ImageView = findViewById(R.id.icon_dark_mode)
        val iconLightMode: ImageView = findViewById(R.id.icon_light_mode)
        // ...


        // 1. Configuración del Toggle
        // Asegúrate de que el listener se configure de esta manera



        toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            topAppBar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)


        // 2. Habilita el botón de navegación en la Toolbar.
        // Esto le dice a la Toolbar que muestre un botón de navegación (el toggle se encargará del ícono).
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toggle.syncState()

        // --- Cargar el Fragment inicial ---
        if (savedInstanceState == null) {
            replaceFragment(pedidos()) // Carga pedidos al iniciar la app
            navigationViewTop.setCheckedItem(R.id.nav_pedidos) // Marca "Inicio" en el menú
        }

        // 3. Manejar los clics en los ítems del menú

        // Primero, creamos una variable para el listener.
        val navItemSelectedListener = NavigationView.OnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_pedidos -> replaceFragment(pedidos())
                R.id.nav_recetas -> replaceFragment(recetas())
                R.id.nav_mojes_tiempos -> replaceFragment(mojes())
                // Agrega aquí los casos para el menú inferior (ej. R.id.nav_settings)
            }
            // Cierra el menú al seleccionar
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // CAMBIO 3 (CORREGIDO): Asigna el listener solo al menú superior
        navigationViewTop.setNavigationItemSelectedListener(navItemSelectedListener)

        // boton dark mode
        iconDarkMode.setOnClickListener {
            // Ahora, en lugar de cambiarlo directamente, le decimos a nuestro manager que lo guarde
            themeManager.saveTheme(ThemeManager.Theme.DARK)
            // No es necesario cerrar el drawer aquí, ya que al cambiar de tema
            // la actividad se recreará de todos modos.
        }

        //boton modo claro
        iconLightMode.setOnClickListener {
            themeManager.saveTheme(ThemeManager.Theme.LIGHT)
        }

        //listener del boton de atras
        supportFragmentManager.addOnBackStackChangedListener {
            val stackHeight = supportFragmentManager.backStackEntryCount
            toggle.isDrawerIndicatorEnabled = true
            toggle.syncState()
            val fragmentActual = supportFragmentManager.findFragmentById(R.id.fragment_container)
            when (fragmentActual) {
                is pedidos -> navigationViewTop.setCheckedItem(R.id.nav_pedidos)
                is recetas -> navigationViewTop.setCheckedItem(R.id.nav_recetas)
                is nueva_receta -> navigationViewTop.setCheckedItem(R.id.oculto1)//marca el item oculto para que paresca que no hay nada marcado
            }
        }

        //boton opciones girando y actuando
        val btnPrincipal = findViewById<LinearLayout>(R.id.opciones)
        var menuAbierto = false
        val btn1 = findViewById<TextView>(R.id.opcion1)
        val btn2 = findViewById<TextView>(R.id.opcion2)
        val btn3 = findViewById<TextView>(R.id.opcion3)

        btnPrincipal.setOnClickListener {
            val rotation = if (menuAbierto) 0f else 45f
            val rotate = ObjectAnimator.ofFloat(btnPrincipal, "rotation", rotation)
            rotate.duration = 300
            rotate.interpolator = OvershootInterpolator()
            rotate.start()
            if (menuAbierto) {
                // Ocultar menú con retardo escalonado (de arriba hacia abajo)
                animateButton(btn3, 0, false)
                animateButton(btn2, 50, false)
                animateButton(btn1, 100, false)
            } else {
                // Mostrar menú con retardo escalonado (de abajo hacia arriba)
                animateButton(btn1, 0, true)
                animateButton(btn2, 50, true)
                animateButton(btn3, 100, true)
            }
            menuAbierto = !menuAbierto
        }

        //cargar recetas en el contenedor invisible
        val menuLayout = findViewById<LinearLayout>(R.id.cajaRecetas)
        updateMenu(menuLayout)

        //boton de mojes
        val oculto = findViewById<ConstraintLayout>(R.id.contenedorOculto)
        btn2.setOnClickListener{
            btn1.visibility = View.GONE
            btn2.visibility = View.GONE
            btn3.visibility = View.GONE
            btnPrincipal.visibility = View.GONE
            btnPrincipal.rotation = 0f
            menuAbierto = false
            oculto.visibility = View.VISIBLE
        }

        //boton de cerrar mojes
        val cerrarMojes = findViewById<LinearLayout>(R.id.cerrarMojes)
        cerrarMojes.setOnClickListener{
            oculto.visibility = View.GONE
            btnPrincipal.visibility = View.VISIBLE
            val typedValue = android.util.TypedValue()
            theme.resolveAttribute(android.R.attr.colorBackground, typedValue, true)
            for (i in 0 until menuLayout.childCount) {
                val child = menuLayout.getChildAt(i)
                child.setBackgroundColor(typedValue.data)
                child.tag = false
            }
        }

        //boton de guardar moje
        val guardarMoje = findViewById<TextView>(R.id.enviarMoje)
        guardarMoje.setOnClickListener{
            val cantidad = findViewById<EditText>(R.id.cantidadMojes).text.toString()
            for (item in viewModel.receta ?: emptyList()) {
                val mojePorciones = item.porciones.toIntOrNull() ?: 0

            }
        }

    }

    // Función de ayuda para reemplazar el fragment en el contenedor
    private fun replaceFragment(fragment: Fragment) {
        val fragmentactual = supportFragmentManager.findFragmentById(R.id.fragment_container)

        if (fragmentactual is nueva_receta) {
            if (fragmentactual.tieneContenidoSinGuardar()) {
            // Mostrar alerta y solo cambiar si el usuario confirma
                alerta(this, onConfirm = {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit()
                }, onCancel = {
                    val navigationViewTop: NavigationView = findViewById(R.id.navigation_view_top)
                    navigationViewTop.setCheckedItem(R.id.oculto1)
                })
            }else{
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit()
            }
        } else {
            // Cambiar directamente si no está en nueva_receta
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

    }
    fun alerta(context: Context, onConfirm: () -> Unit,onCancel: () -> Unit = {}) {
        AlertDialog.Builder(this)
            .setMessage("Se perderá la receta actual. ¿Está seguro?")
            .setPositiveButton("Sí") { dialog, _ ->
                dialog.dismiss()
                onConfirm() // 👉 Ejecuta la acción si el usuario confirma
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
                onCancel()
            }
            .show()
    }
    // 4. Este método es CRUCIAL. Se asegura de que al tocar el ícono, el toggle lo maneje.
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Si el toggle maneja el evento de clic, devuelve true.
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
    fun animateButton(button: View, delay: Long, show: Boolean) {
        if (show) {
            button.visibility = View.VISIBLE
            button.scaleX = 0f
            button.scaleY = 0f
            button.alpha = 0f
            button.translationY = 50f  // comienza un poco abajo

            button.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .translationY(0f)  // sube a su posición
                .setStartDelay(delay)
                .setDuration(300)
                .setInterpolator(OvershootInterpolator())
                .start()
        } else {
            button.animate()
                .scaleX(0f)
                .scaleY(0f)
                .alpha(0f)
                .translationY(50f) // vuelve a bajar
                .setStartDelay(delay)
                .setDuration(200)
                .withEndAction { button.visibility = View.GONE }
                .start()
        }
    }
    private fun updateMenu(menuLayout: LinearLayout) {
        menuLayout.removeAllViews()
        val inflater = layoutInflater
        // 🔹 Accedemos directamente al ViewModel
        for (item in viewModel.receta ?: emptyList()) {
            val cadaReceta = inflater.inflate(R.layout.bloque_moje_recetas, menuLayout, false)
            cadaReceta.findViewById<TextView>(R.id.nombreReceta1).text = item.nombre
            cadaReceta.findViewById<TextView>(R.id.porcionesReceta1).text = item.porciones+" u/s"
            val typedValue = android.util.TypedValue()
            val typedValue2 = android.util.TypedValue()
            theme.resolveAttribute(android.R.attr.colorBackground, typedValue, true)
            theme.resolveAttribute(com.google.android.material.R.attr.colorOutline, typedValue2, true)
            cadaReceta.setOnClickListener {
                for (i in 0 until menuLayout.childCount) {
                    val child = menuLayout.getChildAt(i)
                    child.setBackgroundColor(typedValue.data)
                    child.tag = false
                }
                cadaReceta.setBackgroundColor(typedValue2.data)
                cadaReceta.tag = true
            }
            menuLayout.addView(cadaReceta)
        }
    }
}
