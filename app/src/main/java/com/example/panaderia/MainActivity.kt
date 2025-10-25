package com.example.panaderia

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import java.io.File
import android.widget.LinearLayout
import android.animation.ObjectAnimator
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.core.view.children
import kotlin.collections.mutableMapOf
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import android.view.animation.AccelerateInterpolator
import kotlinx.serialization.Serializable

@Serializable
data class Ingrediente(
    val ingrediente: String,
    val cantidad: String
)
data class IngredienteMoje(
    val ingrediente: String,
    val cantidad: String,
    var tachado: Boolean
)

@Serializable
data class Receta(
    val nombre: String,
    val porciones: String, // 👈 cambia a String porque en el JSON está entre comillas
    val instrucciones: String,
    val ingredientes: List<Ingrediente>
)
data class Moje(
    val nombre: String,
    val moje: String, // 👈 cambia a String porque en el JSON está entre comillas
    val ingredientes: List<IngredienteMoje>
)

// El archivo JSON tiene un array de objetos con una propiedad "receta"
data class RecetaWrapper(
    val receta: Receta
)
data class MojeWrapper(
    val moje: Moje
)

// 👇 2️⃣ ViewModel (también afuera del fragment)
class RecetasViewModel : ViewModel() {
    var receta: List<Receta>? = null
}
class MojeViewModel : ViewModel() {
    var moje: MutableList<Moje> = mutableListOf()
}

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var themeManager: ThemeManager

    private lateinit var viewModel: RecetasViewModel
    private lateinit var viewModel2: MojeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //region 🔹  Crear o recuperar el ViewModel clásico
        viewModel = ViewModelProvider(this)[RecetasViewModel::class.java]
        viewModel2 = ViewModelProvider(this)[MojeViewModel::class.java]

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
            val respuesta = gson.fromJson(json, Array<MojeWrapper>::class.java).toList()
            viewModel2.moje.clear()
            viewModel2.moje.addAll(respuesta.map { it.moje })
        }catch (e: Exception){
            Log.e("LecturaJSON", "Error leyendo JSON", e)
            viewModel2.moje.clear() // lista vacía si hay error
        }
        // endregion

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
                is mojes -> navigationViewTop.setCheckedItem(R.id.nav_mojes_tiempos)
                is nueva_receta -> navigationViewTop.setCheckedItem(R.id.oculto1)//marca el item oculto para que paresca que no hay nada marcado
            }
        }

        //boton opciones girando y actuando
        val btnPrincipal = findViewById<LinearLayout>(R.id.opciones)
        var menuAbierto = false
        val btnPedidos = findViewById<TextView>(R.id.TVBotonPedido)
        val btnMoje = findViewById<TextView>(R.id.TVBotonMoje)
        val btnTiempo = findViewById<TextView>(R.id.TVBotonTiempo)

        //subuda de recetas la primera vez
        /*btn3.setOnClickListener {
            lifecycleScope.launchWhenCreated {
                val recetasLocales = viewModel.receta
                recetasLocales?.forEach { receta ->
                    SupabaseClient.client.postgrest["recetas"].insert(receta)
                }
            }
        }*/

        btnPrincipal.setOnClickListener {
            val rotation = if (menuAbierto) 0f else 45f
            val rotate = ObjectAnimator.ofFloat(btnPrincipal, "rotation", rotation)
            rotate.duration = 300
            rotate.interpolator = OvershootInterpolator()
            rotate.start()
            if (menuAbierto) {
                // Ocultar menú con retardo escalonado (de arriba hacia abajo)
                animateButton(btnTiempo, 0, false)
                animateButton(btnMoje, 50, false)
                animateButton(btnPedidos, 100, false)
            } else {
                // Mostrar menú con retardo escalonado (de abajo hacia arriba)
                animateButton(btnPedidos, 0, true)
                animateButton(btnMoje, 50, true)
                animateButton(btnTiempo, 100, true)
            }
            menuAbierto = !menuAbierto
        }

        //region contenedor mojes
        //cargar recetas en el contenedor invisible
        val menuLayout = findViewById<LinearLayout>(R.id.cajaRecetas)
        updateMenu()

        //boton de mojes
        val bloqueOcultoMoje = findViewById<ConstraintLayout>(R.id.CLMojes)
        val cajaOcultoMoje = findViewById<LinearLayout>(R.id.LLCajaMojes)
        val btnCerrarMojes = findViewById<LinearLayout>(R.id.LLBotonCerrarMojes)
        btnMoje.setOnClickListener{
            if (viewModel.receta.isNullOrEmpty()){
                AlertDialog.Builder(this)
                    .setMessage("No hay recetas aun")
                    .setNegativeButton("Ok") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
                return@setOnClickListener
            }
            animateButton(btnTiempo, 0, false)
            animateButton(btnMoje, 50, false)
            animateButton(btnPedidos, 100, false)
            btnPrincipal.rotation = 0f
            btnCerrarMojes.rotation = 45f
            menuAbierto = false

            cajaOcultoMoje.scaleX = 0.8f
            cajaOcultoMoje.scaleY = 0.8f
            cajaOcultoMoje.alpha = 0f
            bloqueOcultoMoje.visibility = View.VISIBLE

            cajaOcultoMoje.post {
                cajaOcultoMoje.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setDuration(200)
                    .setInterpolator(OvershootInterpolator()) // pequeño rebote
                    .start()
            }
        }

        //boton de cerrar mojes
        btnCerrarMojes.setOnClickListener{
            btnPrincipal.visibility = View.VISIBLE
            //quitar seleccion a las recetas en el contenedor oculto mojes
            val typedValue = android.util.TypedValue()
            theme.resolveAttribute(android.R.attr.colorBackground, typedValue, true)
            for (i in 0 until menuLayout.childCount) {
                val child = menuLayout.getChildAt(i)
                child.setBackgroundColor(typedValue.data)
                child.tag = false
            }

            val rotation = 0f
            val rotate = ObjectAnimator.ofFloat(btnCerrarMojes, "rotation", rotation)
            rotate.duration = 300
            rotate.interpolator = OvershootInterpolator()
            rotate.start()
            cajaOcultoMoje.animate()
                .scaleX(0.8f)
                .scaleY(0.8f)
                .alpha(0f)
                .setDuration(150)
                .setInterpolator(AccelerateInterpolator()) // acelera al final
                .withEndAction {
                    cajaOcultoMoje.scaleX = 1f  // restaurar para la próxima vez
                    cajaOcultoMoje.scaleY = 1f
                    cajaOcultoMoje.alpha = 1f
                    bloqueOcultoMoje.visibility = View.GONE
                }
                .start()
        }

        //listener para cambiar el texto al editar la cantidad de mojes
        val cantidad = findViewById<EditText>(R.id.cantidadMojes)
        cantidad.addTextChangedListener { texto ->
            // Esto se ejecuta cada vez que el texto cambia, texto es la val donde se almacena el texto del edittext
            val nuevoTexto = texto.toString().replace(',', '.').toFloatOrNull()
            cambioCantidadMoje(nuevoTexto,menuLayout)
        }

        //boton de guardar moje
        val guardarMoje = findViewById<TextView>(R.id.enviarMoje)
        guardarMoje.setOnClickListener{
            val cantidad = findViewById<EditText>(R.id.cantidadMojes)
            val mojesGuardados = mutableMapOf<String, Any>()
            val multiIngredientes = mutableListOf<Map<String, Any>>()
            val seleccionado = menuLayout.findViewWithTag<ConstraintLayout>(true)
            val meterNuevoIngrediente: MutableList<IngredienteMoje> = mutableListOf()
            if (seleccionado == null) {
                return@setOnClickListener
            }
            val nombreReceta = seleccionado.findViewById<TextView>(R.id.nombreReceta1).text.toString()
            for (item in viewModel.receta ?: emptyList()) {
                if (item.nombre == nombreReceta) {
                    mojesGuardados["moje"] = cantidad.text.toString()
                    mojesGuardados["nombre"] = nombreReceta
                    for (ingre in item.ingredientes) {
                        val cantidadNieja = ((ingre.cantidad.toFloatOrNull() ?: 0f) * (cantidad.text.toString().toFloatOrNull() ?: 0f)).toString()
                        val poner = mapOf(
                            "ingrediente" to ingre.ingrediente,
                            "cantidad" to cantidadNieja,
                            "tachado" to false
                        )
                        val subMoje = IngredienteMoje(
                            ingrediente = ingre.ingrediente,
                            cantidad = cantidadNieja,
                            tachado = false
                        )
                        meterNuevoIngrediente.add(subMoje)
                        multiIngredientes.add(poner)
                    }
                    mojesGuardados["ingredientes"] = multiIngredientes
                    val datosParaGuardar = mapOf("moje" to mojesGuardados)
                    guardarMojes(datosParaGuardar)
                    btnCerrarMojes.performClick()
                    cantidad.setText("1")
                    val nuevaMoje = Moje(
                        nombre = nombreReceta,
                        moje = cantidad.text.toString(),
                        ingredientes = meterNuevoIngrediente
                    )
                    viewModel2.moje.add(nuevaMoje)
                    val fragmentactual = supportFragmentManager.findFragmentById(R.id.fragment_container)
                    if (fragmentactual is mojes) {
                        val fragmentTransaction = supportFragmentManager.beginTransaction()
                        val fragmentTransaction2 = supportFragmentManager.beginTransaction()
                        fragmentTransaction
                            .detach(fragmentactual)  // desconecta el fragment
                            .commit()
                        fragmentTransaction2
                            .attach(fragmentactual)  // lo vuelve a conectar (recrea la vista)
                            .commit()
                    }
                    replaceFragment(mojes())
                    break
                }
            }
            Log.d("guardado", "Contenido: ${mojesGuardados}")
        }
        // endregion

        //boton de tiempos
        val bloqueOcultoTiempos = findViewById<ConstraintLayout>(R.id.CLTiempos)
        val cajaOcultoTiempos = findViewById<LinearLayout>(R.id.LLCajaTiempos)
        val btnCerrarTiempos = findViewById<LinearLayout>(R.id.LLBotonCerrarTiempos)
        btnTiempo.setOnClickListener{
            animateButton(btnTiempo, 0, false)
            animateButton(btnMoje, 50, false)
            animateButton(btnPedidos, 100, false)
            btnPrincipal.rotation = 0f
            btnCerrarTiempos.rotation = 45f
            menuAbierto = false

            cajaOcultoTiempos.scaleX = 0.8f
            cajaOcultoTiempos.scaleY = 0.8f
            cajaOcultoTiempos.alpha = 0f
            bloqueOcultoTiempos.visibility = View.VISIBLE

            cajaOcultoTiempos.post {
                cajaOcultoTiempos.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setDuration(200)
                    .setInterpolator(OvershootInterpolator()) // pequeño rebote
                    .start()
            }

            val contenedorBotonesNumeros = findViewById<ConstraintLayout>(R.id.CLBotonesTiempo)
            for (i in 0 until contenedorBotonesNumeros.childCount) {
                val vista = contenedorBotonesNumeros.getChildAt(i)
                vista.setOnClickListener {
                    numerosTemporizador(vista as TextView)
                }
            }
        }

        //boton de cerrar tiempos
        btnCerrarTiempos.setOnClickListener{
            btnPrincipal.visibility = View.VISIBLE
            //quitar seleccion a las recetas en el contenedor oculto mojes
            /*val typedValue = android.util.TypedValue()
            theme.resolveAttribute(android.R.attr.colorBackground, typedValue, true)
            for (i in 0 until menuLayout.childCount) {
                val child = menuLayout.getChildAt(i)
                child.setBackgroundColor(typedValue.data)
                child.tag = false
            }*/

            val rotation = 0f
            val rotate = ObjectAnimator.ofFloat(btnCerrarTiempos, "rotation", rotation)
            rotate.duration = 300
            rotate.interpolator = OvershootInterpolator()
            rotate.start()
            cajaOcultoTiempos.animate()
                .scaleX(0.8f)
                .scaleY(0.8f)
                .alpha(0f)
                .setDuration(150)
                .setInterpolator(AccelerateInterpolator()) // acelera al final
                .withEndAction {
                    cajaOcultoTiempos.scaleX = 1f  // restaurar para la próxima vez
                    cajaOcultoTiempos.scaleY = 1f
                    cajaOcultoTiempos.alpha = 1f
                    bloqueOcultoTiempos.visibility = View.GONE
                }
                .start()
        }

    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
            currentFocus!!.clearFocus()
        }
        return super.dispatchTouchEvent(ev)
    }
    // Función de ayuda para reemplazar el fragment en el contenedor
    private fun replaceFragment(fragment: Fragment) {
        val fragmentactual = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (fragmentactual == fragment) return

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
            if (fragment is recetas) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment, "RECETAS_FRAGMENT_TAG")
                    .addToBackStack(null)
                    .commit()
            }else {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        }
        val bloqueOcultoMoje = findViewById<ConstraintLayout>(R.id.CLMojes)
        val btnCerrarMojes = findViewById<LinearLayout>(R.id.LLBotonCerrarMojes)
        val mojeVisible = bloqueOcultoMoje.visibility
        if (mojeVisible == View.VISIBLE){
            btnCerrarMojes.performClick()
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
    fun updateMenu() {
        val menuLayout = findViewById<LinearLayout>(R.id.cajaRecetas)
        menuLayout.removeAllViews()
        val inflater = layoutInflater
        // 🔹 Accedemos directamente al ViewModel
        for (item in viewModel.receta ?: emptyList()) {
            val cadaReceta = inflater.inflate(R.layout.bloque_moje_recetas, menuLayout, false)
            cadaReceta.findViewById<TextView>(R.id.nombreReceta1).text = item.nombre
            val porcionesTextView = cadaReceta.findViewById<TextView>(R.id.porcionesReceta1)
            porcionesTextView.text = "${item.porciones} u/s"
            porcionesTextView.tag = item.porciones
            val typedValue = android.util.TypedValue()
            val typedValue2 = android.util.TypedValue()
            theme.resolveAttribute(android.R.attr.colorBackground, typedValue, true)
            theme.resolveAttribute(com.google.android.material.R.attr.colorOutline, typedValue2, true)
            cadaReceta.setOnClickListener {
                menuLayout.children.forEach { child ->
                    child.setBackgroundColor(typedValue.data)
                    child.tag = false
                }
                cadaReceta.setBackgroundColor(typedValue2.data)
                cadaReceta.tag = true
            }
            cadaReceta.id = item.nombre.hashCode()
            menuLayout.addView(cadaReceta)
        }
    }
    private fun cambioCantidadMoje(texto: Float?,menuLayout: LinearLayout) {
        if (texto != null) {
            menuLayout.children.forEach { child ->
                val porcionesTextView = child.findViewById<TextView>(R.id.porcionesReceta1)
                val cantidad = when(val tagValue = porcionesTextView.tag) {
                    is Float -> tagValue
                    is Double -> tagValue.toFloat()
                    is Int -> tagValue.toFloat()
                    is String -> tagValue.toFloatOrNull() ?: 0f
                    else -> 0f
                }
                porcionesTextView.text = "${cantidad * texto} u/s"
            }
        }
    }
    private fun guardarMojes(recetaEnvueltas: Map<String, Any>) {
        val nombreArchivo = "mojes.json"
        val gson = GsonBuilder().setPrettyPrinting().create() // Usamos el "bonito" desde el principio
        val listaDeRecetas: MutableList<Map<String, Any>>

        try {
            // 1. INTENTAR LEER EL ARCHIVO EXISTENTE
            val archivo = File(filesDir, nombreArchivo)
            if (archivo.exists() && archivo.readText().isNotBlank()) {
                val jsonExistente = archivo.readText()
                val tipoLista = object : TypeToken<MutableList<Map<String, Any>>>() {}.type
                listaDeRecetas = gson.fromJson(jsonExistente, tipoLista)
            } else {
                // Si el archivo no existe o está vacío, crea una lista nueva
                listaDeRecetas = mutableListOf()
            }

            // 2. AÑADIR LA NUEVA RECETA (ya envuelta) A LA LISTA
            listaDeRecetas.add(recetaEnvueltas)

            // 3. CONVERTIR LA LISTA ACTUALIZADA A JSON
            val jsonActualizado = gson.toJson(listaDeRecetas)

            // 4. ESCRIBIR LA LISTA COMPLETA DE VUELTA AL ARCHIVO
            openFileOutput(nombreArchivo, Context.MODE_PRIVATE).use { outputStream ->
                outputStream.write(jsonActualizado.toByteArray())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            AlertDialog.Builder(this)
                .setMessage("Error al guardar la receta: ${e.message}")
                .setNegativeButton("Ok") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }
    fun notificarNuevaReceta() {
        val fragment = supportFragmentManager.findFragmentByTag("RECETAS_FRAGMENT_TAG") as? recetas
        fragment?.mostrarRecetas()
    }

    fun numerosTemporizador(vista: TextView){
        val horas = findViewById<TextView>(R.id.TVHoras)
        val minutos = findViewById<TextView>(R.id.TVMinutos)
        val segundos = findViewById<TextView>(R.id.TVSegundos)
        val numeroTexto = vista.text.toString()
        if(segundos.text.toString().startsWith("0")){
            val escribirNumero = (segundos.text.toString()).drop(1) + numeroTexto
            segundos.text = escribirNumero
        }else if(minutos.text.toString().startsWith("0")){
            val escribirNumero = (minutos.text.toString()).drop(1) + numeroTexto
            minutos.text = escribirNumero
        }else if(horas.text.toString().startsWith("0")){
            val escribirNumero = (horas.text.toString()).drop(1) + numeroTexto
            horas.text = escribirNumero
        }

    }

}
