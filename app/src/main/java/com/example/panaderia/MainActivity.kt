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
import android.content.Intent
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
import androidx.constraintlayout.widget.ConstraintSet
import kotlinx.serialization.Serializable
import android.graphics.Color
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlin.sequences.forEach

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

    private var listaContenedoresTemporizadores: List<Pair<String,String>> = emptyList()

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

        //region 🔹 Configuración del tema y menu desplegable

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

        //endregion

        //region 🔹 recargar temporizadores activos

        //endregion

        //subuda de recetas la primera vez
        /*btn3.setOnClickListener {
            lifecycleScope.launchWhenCreated {
                val recetasLocales = viewModel.receta
                recetasLocales?.forEach { receta ->
                    SupabaseClient.client.postgrest["recetas"].insert(receta)
                }
            }
        }*/

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

        //region 🔹 contenedor mojes
        //cargar recetas en el contenedor invisible
        val menuLayout = findViewById<LinearLayout>(R.id.cajaRecetas)
        updateMenu()

        val bloqueOcultoMoje = findViewById<ConstraintLayout>(R.id.CLMojes)
        val cajaOcultoMoje = findViewById<LinearLayout>(R.id.LLCajaMojes)
        val btnCerrarMojes = findViewById<LinearLayout>(R.id.LLBotonCerrarMojes)

        //boton mostrar contenedor de mojes
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
        // endregion

        //region 🔹 Contenedor y acciones temporizadores

        val bloqueOcultoTiempos = findViewById<ConstraintLayout>(R.id.CLTiempos)
        val cajaOcultoTiempos = findViewById<LinearLayout>(R.id.LLCajaTiempos)

        val btnCerrarTiempos = findViewById<LinearLayout>(R.id.LLBotonCerrarTiempos)
        val btnIniciar = findViewById<TextView>(R.id.TVBotonIniciarTiempos)
        val btnAgregarTemporizador = findViewById<View>(R.id.VBotonAgregarTemporizadores)
        val btnAgregarEtiqueta = findViewById<View>(R.id.VBotonAgregarEtiqueta)

        val horas = findViewById<TextView>(R.id.TVHoras)
        val minutos = findViewById<TextView>(R.id.TVMinutos)
        val segundos = findViewById<TextView>(R.id.TVSegundos)
        val cajaTemporizadores = findViewById<ConstraintLayout>(R.id.CLCajaTemporizadoresGuardados)

        val inflater = layoutInflater

        val typedValue = android.util.TypedValue()
        val typedValue2 = android.util.TypedValue()
        theme.resolveAttribute(android.R.attr.colorBackground, typedValue, true)
        theme.resolveAttribute(com.google.android.material.R.attr.colorOutline, typedValue2, true)

        val contenedorEtiquetas = findViewById<LinearLayout>(R.id.LLCajaEtiquetas)

        //cargar los temporizadores guardados
        cargarTemporizadores()
        //cargar las etiquetas guardadas
        cargarEtiquetas()
        //boton mostrar bloque de temporizadores
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

        //boton sin etiqueta
        val btnSinEtiqueta = findViewById<TextView>(R.id.TVSinEtiqueta)
        btnSinEtiqueta.setOnClickListener {
            contenedorEtiquetas.children.forEach { child ->
                if (child.tag?.toString() == "agregarEtiqueta" || child.tag?.toString() == "VNoColor") return@forEach
                child.setBackgroundColor(typedValue.data)
                child.tag = false
            }
            btnSinEtiqueta.setBackgroundColor(typedValue2.data)
            btnSinEtiqueta.tag = true
        }

        //boton agregar etiquetas
        btnAgregarEtiqueta.setOnClickListener{
            val edicionExiste = findViewById<LinearLayout>(R.id.LLcontenedorEdicionEtiquetasNuevas)
            if (edicionExiste != null){
                return@setOnClickListener
            }
            val altoDP = 1
            val altoPX = (altoDP * resources.displayMetrics.density).toInt()
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                altoPX // alto en píxeles, por ejemplo
            )
            val index = contenedorEtiquetas.childCount-1
            val nuevaEtiquetaEdicion = inflater.inflate(R.layout.bloque_editar_nueva_etiqueta, contenedorEtiquetas, false)
            val btnCancelarNuevaEtiqueta = nuevaEtiquetaEdicion.findViewById<View>(R.id.TVCancelarNuevaEtiqueta)
            val btnAgregarNuevaEtiqueta = nuevaEtiquetaEdicion.findViewById<View>(R.id.IVGuardarNuevaEtiqueta)
            btnCancelarNuevaEtiqueta.setOnClickListener{
                contenedorEtiquetas.removeView(nuevaEtiquetaEdicion)
            }
            btnAgregarNuevaEtiqueta.setOnClickListener {
                val nuevoView = View(this)
                nuevoView.layoutParams = params
                nuevoView.setBackgroundColor(Color.parseColor("#000000"))
                nuevoView.tag = "VNoColor"
                val nuevaEtiqueta = inflater.inflate(R.layout.bloque_etiquetas_temporizadores, contenedorEtiquetas, false)
                val etiquetaOscurecer = nuevaEtiqueta.findViewById<View>(R.id.VOscurecerTemporizador)
                val etiquetaCancelar = nuevaEtiqueta.findViewById<View>(R.id.TVCancelarBorrado)
                val etiquetaBorrar = nuevaEtiqueta.findViewById<View>(R.id.IVborrarTemporizador)
                val textview1 = nuevaEtiqueta.findViewById<TextView>(R.id.TVPonerTextoEtiquetaNueva)
                textview1.text = nuevaEtiquetaEdicion.findViewById<TextView>(R.id.ETNuevaEtiqueta).text
                textview1.setOnClickListener {
                    contenedorEtiquetas.children.forEach { child ->
                        if (child.tag?.toString() == "agregarEtiqueta" || child.tag?.toString() == "VNoColor") return@forEach
                        child.setBackgroundColor(typedValue.data)
                        child.tag = false
                    }
                    nuevaEtiqueta.setBackgroundColor(typedValue2.data)
                    nuevaEtiqueta.tag = true
                }
                textview1.setOnLongClickListener {
                    etiquetaOscurecer.visibility = View.VISIBLE
                    etiquetaCancelar.visibility = View.VISIBLE
                    etiquetaBorrar.visibility = View.VISIBLE
                    false
                }
                etiquetaCancelar.setOnClickListener{
                    etiquetaOscurecer.visibility = View.GONE
                    etiquetaCancelar.visibility = View.GONE
                    etiquetaBorrar.visibility = View.GONE
                }
                etiquetaBorrar.setOnClickListener{
                    contenedorEtiquetas.removeView(nuevaEtiqueta)
                    val gson = GsonBuilder().setPrettyPrinting().create()
                    val archivo = File(filesDir, "etiquetas.json")
                    val etiquetas: MutableList<String> = if (archivo.exists() && archivo.readText().isNotBlank()) {
                        val gson = Gson()
                        val tipoLista = object : TypeToken<MutableList<String>>() {}.type
                        gson.fromJson(archivo.readText(), tipoLista)
                    } else mutableListOf()
                    etiquetas.remove(textview1.text.toString())
                    archivo.writeText(gson.toJson(etiquetas))
                }
                contenedorEtiquetas.removeView(nuevaEtiquetaEdicion)
                contenedorEtiquetas.addView(nuevaEtiqueta, index)
                contenedorEtiquetas.addView(nuevoView, index+1)
                guardarJsonSencillo("etiquetas",textview1.text.toString())
            }
            contenedorEtiquetas.addView(nuevaEtiquetaEdicion, index)
        }

        //boton agregar temporizadores
        btnAgregarTemporizador.setOnClickListener{
            val temporizadorArreglado = convertirTiempo(horas.text.toString(), minutos.text.toString(), segundos.text.toString())
            val subtag = temporizadorArreglado.replace(":","")
            val tag = subtag.toInt()
            if (temporizadorArreglado == "00:00:00"){
                return@setOnClickListener
            }
            val hijoRepetido = cajaTemporizadores.children.firstOrNull { it.tag == tag }
            if (hijoRepetido != null) {return@setOnClickListener}
            val temporizador = inflater.inflate(R.layout.bloque_temporizadores_guardados, cajaTemporizadores, false)
            val ponerTexto = temporizador.findViewById<TextView>(R.id.TVNuevoTemporizador)
            ponerTexto.text = temporizadorArreglado
            temporizador.id = View.generateViewId()
            temporizador.tag = tag
            temporizador.setOnClickListener{
                val trozos = subtag.chunked(2)
                horas.text = trozos[0]
                minutos.text = trozos[1]
                segundos.text = trozos[2]
            }
            val btnBorrado = temporizador.findViewById<View>(R.id.IVborrarTemporizador)
            val btnCancelar = temporizador.findViewById<View>(R.id.TVCancelarBorrado)
            val oscurecerTemporizador = temporizador.findViewById<View>(R.id.VOscurecerTemporizador)
            temporizador.setOnLongClickListener{
                btnBorrado.visibility = View.VISIBLE
                btnCancelar.visibility = View.VISIBLE
                oscurecerTemporizador.visibility = View.VISIBLE
                temporizador.isClickable = false
                false
            }
            btnCancelar.setOnClickListener{
                btnBorrado.visibility = View.GONE
                btnCancelar.visibility = View.GONE
                oscurecerTemporizador.visibility = View.GONE
                temporizador.isClickable = true
            }
            btnBorrado.setOnClickListener{
                cajaTemporizadores.removeView(temporizador)
                val gson = GsonBuilder().setPrettyPrinting().create()
                val archivo = File(filesDir, "temporizadores.json")
                val temporizadores: MutableList<String> = if (archivo.exists() && archivo.readText().isNotBlank()) {
                    val gson = Gson()
                    val tipoLista = object : TypeToken<MutableList<String>>() {}.type
                    gson.fromJson(archivo.readText(), tipoLista)
                } else mutableListOf()
                temporizadores.remove(temporizadorArreglado)
                archivo.writeText(gson.toJson(temporizadores))
                ordenarTemporizadores(cajaTemporizadores,findViewById(R.id.VBotonAgregarTemporizadores))
            }
            cajaTemporizadores.addView(temporizador)
            ordenarTemporizadores(cajaTemporizadores,btnAgregarTemporizador)
            guardarJsonSencillo("temporizadores",temporizadorArreglado)
        }

        val cajaTemporizadoresCorriendoExtra = findViewById<LinearLayout>(R.id.LLCajaTemporizadoresExtraHorizontal)
        val scrollTemporizadoresExtra = findViewById<HorizontalScrollView>(R.id.HSTemporizadoresExtra)

        //boton iniciar temporizador
        btnIniciar.setOnClickListener{
            //cancela en caso de que sea 0
            if (horas.text == "00" && minutos.text == "00" && segundos.text == "00"){
                return@setOnClickListener
            }
            //obtiene el nombre de la etiqueta
            //💾 variable de salida nombreDeEtiqueta
            lateinit var nombreDeEtiqueta: String
            val etiquetaSeleccionada = contenedorEtiquetas.findViewWithTag<View>(true)
            when{
                etiquetaSeleccionada is TextView -> {
                    nombreDeEtiqueta = etiquetaSeleccionada.text.toString()
                }
                etiquetaSeleccionada is FrameLayout -> {
                    nombreDeEtiqueta = etiquetaSeleccionada.findViewById<TextView>(R.id.TVPonerTextoEtiquetaNueva).text.toString()
                }
                else -> {
                    nombreDeEtiqueta = "Sin etiqueta"
                }
            }
            //calcula el tiempo total en segundos
            //💾 variable de salida tiempoTotalSegundos
            val segundosSumar = segundos.text.toString().toInt()
            val minutosSumar = minutos.text.toString().toInt()
            val horasSumar = horas.text.toString().toInt()
            val tiempoTotalSegundos = segundosSumar+(minutosSumar*60)+(horasSumar*3600)

            var dommy = "0"

            val cantidadTemporizadores = TimerRepository.cantidadTemporizadores.value
            if (cantidadTemporizadores >= 3){
                if (scrollTemporizadoresExtra.visibility == View.GONE){
                    scrollTemporizadoresExtra.visibility = View.VISIBLE
                }
                //infla el nuevo contenedor
                val nuevoTemporizadorExtra = inflater.inflate(R.layout.bloque_temporizador_extra_horizontal, cajaTemporizadoresCorriendoExtra, false)
                nuevoTemporizadorExtra.id = View.generateViewId()
                cajaTemporizadoresCorriendoExtra.addView(nuevoTemporizadorExtra)
                dommy = nuevoTemporizadorExtra.id.toString()
            }
            //aqui ya se pone la funcion de ordenar
            ordenarTemporizadoresActivos(nombreDeEtiqueta, tiempoTotalSegundos, dommy)
        }

        //boton de cerrar tiempos
        btnCerrarTiempos.setOnClickListener{
            btnPrincipal.visibility = View.VISIBLE
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
        //endregion
    }
    private fun ordenarTemporizadoresActivos(nombreDeEtiqueta: String, segundos: Int, idExtra: String? = null){
        TimerRepository.actualizarCantidadTemporizadores()
        //obtiene los temporizadores actuales
        val temporizadores = TimerRepository.temporizadores.value.toMutableMap()
        //comprueba si ya hay etiquetas con ese nombre, de ser asi agrega un numero al final
        //variable de salida 💾 etiqueta
        lateinit var etiqueta: String
        val coincidencias = temporizadores
            .filterKeys { it.startsWith(nombreDeEtiqueta) }
        if (coincidencias.isNotEmpty()) {
            val indiceSinEtiqueta = coincidencias.size + 1
            etiqueta = "$nombreDeEtiqueta $indiceSinEtiqueta"
        }else{
            etiqueta = nombreDeEtiqueta
        }

        lateinit var idcontenedorTextoEtiqueta: String
        lateinit var idcontenedorTiempo: String
        if (temporizadores.size < 3){
            lateinit var temporizadorCorriendo: FrameLayout
            lateinit var contenedorTextoEtiqueta: TextView
            lateinit var contenedorTiempo: TextView
            when(temporizadores.size){
                0 -> {
                    temporizadorCorriendo = findViewById<FrameLayout>(R.id.LLTemporizadorActivo1)
                    contenedorTextoEtiqueta = temporizadorCorriendo.findViewById<TextView>(R.id.TVTextoTemporizadorActivo1)
                    contenedorTiempo = temporizadorCorriendo.findViewById<TextView>(R.id.TVTiempoTemporizadorActivo1)
                }
                1 -> {
                    temporizadorCorriendo = findViewById<FrameLayout>(R.id.LLTemporizadorActivo2)
                    contenedorTextoEtiqueta = temporizadorCorriendo.findViewById<TextView>(R.id.TVTextoTemporizadorActivo2)
                    contenedorTiempo = temporizadorCorriendo.findViewById<TextView>(R.id.TVTiempoTemporizadorActivo2)
                }
                2 -> {
                    temporizadorCorriendo = findViewById<FrameLayout>(R.id.LLTemporizadorActivo3)
                    contenedorTextoEtiqueta = temporizadorCorriendo.findViewById<TextView>(R.id.TVTextoTemporizadorActivo3)
                    contenedorTiempo = temporizadorCorriendo.findViewById<TextView>(R.id.TVTiempoTemporizadorActivo3)
                }
            }
            temporizadorCorriendo.visibility = View.VISIBLE
            idcontenedorTextoEtiqueta = contenedorTextoEtiqueta.id.toString()
            idcontenedorTiempo = contenedorTiempo.id.toString()
        }else{
            idcontenedorTextoEtiqueta = "extra"
            idcontenedorTiempo = idExtra.toString()
        }

        val btnMinimizar = findViewById<View>(R.id.VBotonMinimizar)
        val cajaTemporizadoresCorriendo = findViewById<LinearLayout>(R.id.LLCajaTemporizadoresActivos)
        //si no habia temporizadores funcionando pone visible el contenedor
        if (cajaTemporizadoresCorriendo.visibility == View.GONE){
            cajaTemporizadoresCorriendo.visibility = View.VISIBLE
        }
        //pone visible el boton de minimizar
        btnMinimizar.visibility = View.VISIBLE
        //agregar el id del contenedor para poner el tiempo y el de la etiqueta o bien extra a la lista para ordenar
        listaContenedoresTemporizadores = listaContenedoresTemporizadores + (idcontenedorTextoEtiqueta to idcontenedorTiempo)
        //agregar el id y segundos actualues a la lista de temporizadores
        temporizadores[etiqueta] = listOf("0", idcontenedorTextoEtiqueta, idcontenedorTiempo, segundos)
        // ordenar temporizadores por segundos
        if (temporizadores.size > 1){
            // 1️⃣ Ordenar según los segundos (índice 3, que es Int)
            val temporizadoresOrdenados: Map<String, List<Any>> = temporizadores
                .toList()
                .sortedBy { (_, lista) -> lista[3] as Int }
                .toMap()

            // 2️⃣ Reasignar contenedores según listaContenedoresTemporizadores
            val keysOrdenadas = temporizadoresOrdenados.keys.toList()
            val valoresOrdenados = temporizadoresOrdenados.values.toList()

            val nuevoMapa: Map<String, List<Any>> = valoresOrdenados
                .zip(listaContenedoresTemporizadores) // List<Pair<List<Any>, Pair<String,String>>>
                .mapIndexed { index, pair ->
                    val (valorAntiguo, contenedor) = pair
                    val key = keysOrdenadas[index]
                    key to listOf(valorAntiguo[0], contenedor.first, contenedor.second, valorAntiguo[3])
                }
                .toMap()
            TimerRepository.actualizarTemporizador(nuevoMapa)
        }else{
            TimerRepository.actualizarTemporizador(temporizadores)
        }

        val intent = Intent(this, TimerService::class.java)
        intent.putExtra("accion", "INICIAR")
        intent.putExtra("tiempo", segundos)
        intent.putExtra("id", etiqueta)
        startForegroundService(intent)

    }

    override fun onStart() {
        super.onStart()
        // lifecycleScope asegura que la coroutine se cancela automáticamente
        lifecycleScope.launch {
            TimerRepository.temporizadorModificado.collect {datos ->
                if (datos.isNotEmpty()){
                    if (datos[1].toString() != "extra"){
                        findViewById<TextView>(datos[1].toString().toInt()).text = datos[4].toString()
                    }
                    findViewById<TextView>(datos[2].toString().toInt()).text = datos[0].toString()
                }
            }
        }
    }
    fun cargarTemporizadoresActivos(){

    }
    //funcion que quita el focus al oprimir en otros lados de la pantalla
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
    //funcion para animar los botones de mojes, temporizadores y pedidos
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
    //funcion que actualiza las recetas en el contenedor de mojes
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
    //funcion que cambia las cantidades cuando se escrive la cantidad demojes
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
                .setMessage("Error al guardar : ${e.message}")
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
    private fun numerosTemporizador(vista: TextView){
        val horas = findViewById<TextView>(R.id.TVHoras)
        val minutos = findViewById<TextView>(R.id.TVMinutos)
        val segundos = findViewById<TextView>(R.id.TVSegundos)
        val numeroTexto = vista.text.toString()
        if(numeroTexto == "-"){
           var nuevoNumero = (horas.text.toString())+(minutos.text.toString())+(segundos.text.toString())
            if (nuevoNumero == "000000"){
                return
            }
            nuevoNumero = nuevoNumero.dropLast(1)
            nuevoNumero = "0$nuevoNumero"
            val hms = nuevoNumero.chunked(2)
            horas.text = hms[0]
            minutos.text = hms[1]
            segundos.text = hms[2]
            return
        }
        var nuevoNumero = (horas.text.toString())+(minutos.text.toString())+(segundos.text.toString())
        if(!nuevoNumero.startsWith("0")){
            return
        }
        var quitar = 1
        if(numeroTexto == "00"){
            if((horas.text.toString()) != "00"){
                return
            }
            quitar = 2
        }
        nuevoNumero = nuevoNumero.drop(quitar)
        nuevoNumero = nuevoNumero + numeroTexto
        val hms = nuevoNumero.chunked(2)
        horas.text = hms[0]
        minutos.text = hms[1]
        segundos.text = hms[2]
    }
    private fun convertirTiempo(horas: String, minutos: String, segundos: String): String{
        val sumaSegundos = segundos.toInt()/60
        var restaSegundos = "${segundos.toInt()-(sumaSegundos*60)}"
        val totalMinutos = minutos.toInt() + sumaSegundos
        val sumaMinutos = totalMinutos/60
        var restaMinutos = "${totalMinutos-(sumaMinutos*60)}"
        var totalHoras = "${horas.toInt() + sumaMinutos}"
        if(restaSegundos.length<2){restaSegundos = "0$restaSegundos"}
        if(restaMinutos.length<2){restaMinutos = "0$restaMinutos"}
        if(totalHoras.length<2){totalHoras = "0$totalHoras"}
        return "$totalHoras:$restaMinutos:$restaSegundos"
    }
    private fun ordenarTemporizadores (cajaTemporizadores: ConstraintLayout, btnAgregarTemporizador: View){
        val set = ConstraintSet()
        set.clone(cajaTemporizadores)
        lateinit var hijoAnterior: View
        lateinit var ultimoHijo: View
        val hijosOrdenados: List<View> = (0 until cajaTemporizadores.childCount)
            .map { cajaTemporizadores.getChildAt(it) } // obtenemos los hijos
            .mapNotNull {
                val tagInt = it.tag as? Int               // cast seguro
                tagInt?.let { tag -> it to tag }         // solo si tag es Int
            }
            .sortedBy { it.second }                       // ordenamos por el valor del tag
            .map { it.first }
        var cuentaHijos = 1
        for (hijo in hijosOrdenados) {
            if (cuentaHijos % 2 == 0) {
                if (cuentaHijos == 2){
                    set.connect(hijo.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
                    set.connect(hijo.id, ConstraintSet.START, R.id.GLCajaTemporizadoresGuardados, ConstraintSet.END)
                    set.connect(hijo.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
                }else{
                    set.connect(hijo.id, ConstraintSet.TOP, hijoAnterior.id, ConstraintSet.BOTTOM)
                    set.connect(hijo.id, ConstraintSet.START, R.id.GLCajaTemporizadoresGuardados, ConstraintSet.END)
                    set.connect(hijo.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
                }
                hijoAnterior = hijo
            }else{
                if (cuentaHijos == 1){
                    set.connect(hijo.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
                    set.connect(hijo.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
                    set.connect(hijo.id, ConstraintSet.END, R.id.GLCajaTemporizadoresGuardados, ConstraintSet.START)
                }else{
                    set.connect(hijo.id, ConstraintSet.TOP, hijoAnterior.id, ConstraintSet.BOTTOM)
                    set.connect(hijo.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
                    set.connect(hijo.id, ConstraintSet.END, R.id.GLCajaTemporizadoresGuardados, ConstraintSet.START)
                }
            }
            ultimoHijo = hijo
            cuentaHijos++
        }
        val margenDp = 10
        val margenPx = (margenDp * resources.displayMetrics.density).toInt()
        val totalTemporizadores = cajaTemporizadores.children.count()
        if(hijosOrdenados.isEmpty()){
            set.connect(btnAgregarTemporizador.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
            set.connect(btnAgregarTemporizador.id, ConstraintSet.START,ConstraintSet.PARENT_ID, ConstraintSet.START)
            set.connect(btnAgregarTemporizador.id, ConstraintSet.END,  R.id.GLCajaTemporizadoresGuardados, ConstraintSet.START)
            set.setMargin(btnAgregarTemporizador.id, ConstraintSet.TOP, margenPx)
        }else{
            if(totalTemporizadores % 2 ==0 ){
                set.connect(btnAgregarTemporizador.id, ConstraintSet.TOP, ultimoHijo.id, ConstraintSet.BOTTOM)
                set.connect(btnAgregarTemporizador.id, ConstraintSet.START,ConstraintSet.PARENT_ID, ConstraintSet.START)
                set.connect(btnAgregarTemporizador.id, ConstraintSet.END,  R.id.GLCajaTemporizadoresGuardados, ConstraintSet.START)
                set.setMargin(btnAgregarTemporizador.id, ConstraintSet.TOP, margenPx)
            }else{
                set.connect(btnAgregarTemporizador.id, ConstraintSet.TOP, ultimoHijo.id, ConstraintSet.TOP)
                set.connect(btnAgregarTemporizador.id, ConstraintSet.START, R.id.GLCajaTemporizadoresGuardados, ConstraintSet.START)
                set.connect(btnAgregarTemporizador.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
                set.setMargin(btnAgregarTemporizador.id, ConstraintSet.TOP, 0)
            }
        }
        set.applyTo(cajaTemporizadores)
    }
    fun guardarJsonSencillo(nombreArchivo: String, contenido: String) {
        val gson = GsonBuilder().setPrettyPrinting().create()
        val archivo = File(filesDir, "$nombreArchivo.json")
        val lista: MutableList<String> = try {
            if (archivo.exists() && archivo.readText().isNotBlank()) {
                val tipoLista = object : TypeToken<MutableList<String>>() {}.type
                gson.fromJson(archivo.readText(), tipoLista)
            } else mutableListOf()
        } catch (e: Exception) {
            mutableListOf()
        }

        lista.add(contenido)

        try {
            archivo.writeText(gson.toJson(lista))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun cargarTemporizadores() {
        val archivo = File(filesDir, "temporizadores.json")
        val temporizadores: MutableList<String> = if (archivo.exists() && archivo.readText().isNotBlank()) {
            val gson = Gson()
            val tipoLista = object : TypeToken<MutableList<String>>() {}.type
            gson.fromJson(archivo.readText(), tipoLista)
        } else mutableListOf()
        if (temporizadores.isEmpty()) return
        val cajaTemporizadores = findViewById<ConstraintLayout>(R.id.CLCajaTemporizadoresGuardados)
        val inflater = layoutInflater
        var horas = findViewById<TextView>(R.id.TVHoras)
        var minutos = findViewById<TextView>(R.id.TVMinutos)
        var segundos = findViewById<TextView>(R.id.TVSegundos)
        for (temporizador in temporizadores) {
            val temporizadorView = inflater.inflate(R.layout.bloque_temporizadores_guardados, cajaTemporizadores, false)
            temporizadorView.findViewById<TextView>(R.id.TVNuevoTemporizador).text = temporizador
            val subtag = temporizador.replace(":","")
            temporizadorView.id = View.generateViewId()
            val tag = subtag.toInt()
            temporizadorView.tag = tag
            temporizadorView.setOnClickListener{
                val trozos = subtag.chunked(2)
                horas.text = trozos[0]
                minutos.text = trozos[1]
                segundos.text = trozos[2]
            }
            val btnBorrado = temporizadorView.findViewById<View>(R.id.IVborrarTemporizador)
            val btnCancelar = temporizadorView.findViewById<View>(R.id.TVCancelarBorrado)
            val oscurecerTemporizador = temporizadorView.findViewById<View>(R.id.VOscurecerTemporizador)
            temporizadorView.setOnLongClickListener{
                btnBorrado.visibility = View.VISIBLE
                btnCancelar.visibility = View.VISIBLE
                oscurecerTemporizador.visibility = View.VISIBLE
                temporizadorView.isClickable = false
                false
            }
            btnCancelar.setOnClickListener{
                btnBorrado.visibility = View.GONE
                btnCancelar.visibility = View.GONE
                oscurecerTemporizador.visibility = View.GONE
                temporizadorView.isClickable = true
            }
            btnBorrado.setOnClickListener{
                cajaTemporizadores.removeView(temporizadorView)
                temporizadores.remove(temporizador)
                val gson = GsonBuilder().setPrettyPrinting().create()
                val archivo = File(filesDir, "temporizadores.json")
                archivo.writeText(gson.toJson(temporizadores))
                ordenarTemporizadores(cajaTemporizadores,findViewById(R.id.VBotonAgregarTemporizadores))
            }
            cajaTemporizadores.addView(temporizadorView)
        }
        ordenarTemporizadores(cajaTemporizadores,findViewById(R.id.VBotonAgregarTemporizadores))
    }
    private fun cargarEtiquetas() {
        val archivo = File(filesDir, "etiquetas.json")
        val etiquetas: MutableList<String> =
            if (archivo.exists() && archivo.readText().isNotBlank()) {
                val gson = Gson()
                val tipoLista = object : TypeToken<MutableList<String>>() {}.type
                gson.fromJson(archivo.readText(), tipoLista)
            } else mutableListOf()
        if (etiquetas.isEmpty()) return
        val inflater = layoutInflater
        val contenedorEtiquetas = findViewById<LinearLayout>(R.id.LLCajaEtiquetas)
        val index = contenedorEtiquetas.childCount-1
        val altoDP = 1
        val altoPX = (altoDP * resources.displayMetrics.density).toInt()
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            altoPX // alto en píxeles, por ejemplo
        )
        val typedValue = android.util.TypedValue()
        val typedValue2 = android.util.TypedValue()
        theme.resolveAttribute(android.R.attr.colorBackground, typedValue, true)
        theme.resolveAttribute(com.google.android.material.R.attr.colorOutline, typedValue2, true)

        for (etiqueta in etiquetas) {
            val nuevoView = View(this)
            nuevoView.layoutParams = params
            nuevoView.setBackgroundColor(Color.parseColor("#000000"))
            nuevoView.tag = "VNoColor"
            val nuevaEtiqueta = inflater.inflate(
                R.layout.bloque_etiquetas_temporizadores,
                contenedorEtiquetas,
                false
            )
            val etiquetaOscurecer = nuevaEtiqueta.findViewById<View>(R.id.VOscurecerTemporizador)
            val etiquetaCancelar = nuevaEtiqueta.findViewById<View>(R.id.TVCancelarBorrado)
            val etiquetaBorrar = nuevaEtiqueta.findViewById<View>(R.id.IVborrarTemporizador)
            val textview1 = nuevaEtiqueta.findViewById<TextView>(R.id.TVPonerTextoEtiquetaNueva)
            textview1.text = etiqueta
            textview1.setOnClickListener {
                contenedorEtiquetas.children.forEach { child ->
                    if (child.tag?.toString() == "agregarEtiqueta" || child.tag?.toString() == "VNoColor") return@forEach
                    child.setBackgroundColor(typedValue.data)
                    child.tag = false
                }
                nuevaEtiqueta.setBackgroundColor(typedValue2.data)
                nuevaEtiqueta.tag = true
            }
            textview1.setOnLongClickListener {
                etiquetaOscurecer.visibility = View.VISIBLE
                etiquetaCancelar.visibility = View.VISIBLE
                etiquetaBorrar.visibility = View.VISIBLE
                false
            }
            etiquetaCancelar.setOnClickListener {
                etiquetaOscurecer.visibility = View.GONE
                etiquetaCancelar.visibility = View.GONE
                etiquetaBorrar.visibility = View.GONE
            }
            etiquetaBorrar.setOnClickListener {
                contenedorEtiquetas.removeView(nuevaEtiqueta)
                val gson = GsonBuilder().setPrettyPrinting().create()
                val archivo = File(filesDir, "etiquetas.json")
                val etiquetas: MutableList<String> =
                    if (archivo.exists() && archivo.readText().isNotBlank()) {
                        val gson = Gson()
                        val tipoLista = object : TypeToken<MutableList<String>>() {}.type
                        gson.fromJson(archivo.readText(), tipoLista)
                    } else mutableListOf()
                etiquetas.remove(textview1.text.toString())
                archivo.writeText(gson.toJson(etiquetas))
            }
            contenedorEtiquetas.addView(nuevaEtiqueta, index)
            contenedorEtiquetas.addView(nuevoView, index + 1)
        }
    }
}
