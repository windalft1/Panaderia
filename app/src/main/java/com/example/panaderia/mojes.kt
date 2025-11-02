package com.example.panaderia

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import androidx.lifecycle.ViewModelProvider
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import androidx.core.view.children
import kotlin.sequences.forEach
import android.content.res.ColorStateList
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ScrollView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.startForegroundService
import com.google.android.material.appbar.MaterialToolbar
import kotlin.math.log


class mojes : Fragment() {
    lateinit var viewModel: MojeViewModel
    private var container: LinearLayout? = null
    private var containerSub: LinearLayout? = null
    private var inflater: LayoutInflater? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.inflater = inflater
        return inflater.inflate(R.layout.fragment_mojes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.supportActionBar?.title = "Mojes y tiempos"

        viewModel = ViewModelProvider(requireActivity()).get(MojeViewModel::class.java)
        container = view.findViewById<LinearLayout>(R.id.cargaIngredientes)
        containerSub = view.findViewById<LinearLayout>(R.id.barraMojes)
        actualizarListaDeMojes()
    }

    fun actualizarListaDeMojes() {
        val localContainer = container ?: return
        val localContainerSub = containerSub ?: return
        val localInflater = inflater ?: return

        val typedValue = android.util.TypedValue()
        val typedValue2 = android.util.TypedValue()
        requireContext().theme.resolveAttribute(android.R.attr.colorBackground, typedValue, true)
        requireContext().theme.resolveAttribute(com.google.android.material.R.attr.colorSurfaceVariant, typedValue2, true)

        localContainerSub.removeAllViews()
        // Limpia los ingredientes del moje previamente seleccionado
        localContainer.children
            .filter { it.id == R.id.borrarBloque }
            .toList()
            .forEach { localContainer.removeView(it) }
        val contenedorAbajo = localContainer.findViewById<LinearLayout>(R.id.ponerAntes)
        contenedorAbajo?.removeAllViews()
        val textoCentrado = view?.findViewById<TextView>(R.id.TVTextoNoHayMojesNiOTemporizadores)
        textoCentrado?.visibility = View.GONE

        val pestanaTemporizadores = view?.findViewById<TextView>(R.id.TVTemporizadoresFrangmentMojes)
        val contenedorListaTemporizadores = view?.findViewById<ScrollView>(R.id.SVTemporizadores)
        val scrollViewMojes = view?.findViewById<ScrollView>(R.id.SVMojes)
        val botonTerminarMoje = view?.findViewById<TextView>(R.id.terminarMoje)
        val linearTemporizadores = view?.findViewById<LinearLayout>(R.id.LLListaTemporizadores)
        linearTemporizadores?.removeAllViews()

        //region  temporizadores
        //cargar temporizadores
        pestanaTemporizadores?.visibility = View.VISIBLE
        val temporizadores = TimerRepository.temporizadores.value.toMutableMap()
        if (temporizadores.isNotEmpty()){
            for ((key, value) in temporizadores) {
                val bloqueTemporizadores = localInflater.inflate(R.layout.bloque_temporizadores_pestana_temporizadores, linearTemporizadores, false)
                bloqueTemporizadores.id = View.generateViewId()
                val textViewEtiqueta = bloqueTemporizadores.findViewById<TextView>(R.id.TVEtiquetaTemporizador)
                textViewEtiqueta.text = key
                val textViewTiempo = bloqueTemporizadores.findViewById<TextView>(R.id.TVTiempoTemporizador)
                textViewTiempo.id = View.generateViewId()
                textViewTiempo.text = value[0].toString()
                val btnBorrarTemporizador = bloqueTemporizadores.findViewById<View>(R.id.VBorrarTemporizador)
                btnBorrarTemporizador.setOnClickListener {
                    (requireActivity() as MainActivity).borrarTemporizador(key)
                    if (TimerRepository.cantidadTemporizadores.value == 0){
                        contenedorListaTemporizadores?.visibility = View.GONE
                        pestanaTemporizadores?.backgroundTintList = ColorStateList.valueOf(typedValue2.data)
                        TimerRepository.cambiarEspera("flotando")
                        (requireActivity() as MainActivity).cargarTemporizadoresActivosMojes("quitar")
                        val intent = Intent(requireContext(), TimerService::class.java)
                        intent.putExtra("accion", "DETENER")
                        intent.putExtra("id", key)
                        requireContext().startForegroundService(intent)
                        //actualizarListaDeMojes()
                    }
                    actualizarListaDeMojes()
                }
                val color = bloqueTemporizadores.findViewById<LinearLayout>(R.id.LLTemporizadorPaginaTemporizadores)
                linearTemporizadores?.addView(bloqueTemporizadores)
                temporizadores[key] = listOf(value[0],value[1],value[2],value[3],textViewTiempo.id.toString(),value[5],color.id.toString())
            }
            TimerRepository.actualizarTemporizador(temporizadores)
        }
        //boton pestana temporizadores
        pestanaTemporizadores?.setOnClickListener {
            localContainerSub.children.forEach { child ->
                child.backgroundTintList = ColorStateList.valueOf(typedValue2.data)
            }
            pestanaTemporizadores.backgroundTintList = ColorStateList.valueOf(typedValue.data)
            scrollViewMojes?.visibility = View.GONE
            botonTerminarMoje?.visibility = View.GONE
            if (TimerRepository.cantidadTemporizadores.value == 0){
                textoCentrado?.visibility = View.VISIBLE
                textoCentrado?.text = "No hay temporizadores corriendo"
            }else{
                textoCentrado?.visibility = View.GONE
                contenedorListaTemporizadores?.visibility = View.VISIBLE
            }

            TimerRepository.cambiarEspera("pestana")
            (requireActivity() as MainActivity).cargarTemporizadoresActivosMojes("quitar")
        }
        //endregion
        var primero = true
        if (contenedorListaTemporizadores?.visibility == View.VISIBLE) {
            primero = false
        }

        if (viewModel.moje.isEmpty()) {
            /*if (contenedorListaTemporizadores?.visibility != View.VISIBLE) {
                primero = false
            }*/
            textoCentrado?.visibility = View.VISIBLE
            scrollViewMojes?.visibility = View.GONE
            botonTerminarMoje?.visibility = View.GONE
            if (TimerRepository.cantidadTemporizadores.value == 0){
                pestanaTemporizadores?.visibility = View.GONE
                textoCentrado?.text = "No hay mojes en preparacion ni temporizadores activos"
            }else{
                textoCentrado?.text = "No hay mojes en preparacion"
                val bloqueBarraMoje = localInflater.inflate(R.layout.bloque_barra_mojes, localContainerSub, false)
                localContainerSub.addView(bloqueBarraMoje)
                if (contenedorListaTemporizadores?.visibility == View.VISIBLE) {
                    textoCentrado?.visibility = View.GONE
                }else{
                    bloqueBarraMoje.backgroundTintList = ColorStateList.valueOf(typedValue.data)
                }
                bloqueBarraMoje.setOnClickListener{
                    bloqueBarraMoje.backgroundTintList = ColorStateList.valueOf(typedValue.data)
                    pestanaTemporizadores?.backgroundTintList = ColorStateList.valueOf(typedValue2.data)
                    textoCentrado?.visibility = View.VISIBLE
                    contenedorListaTemporizadores?.visibility = View.GONE
                    textoCentrado?.text = "No hay mojes en preparacion"
                    TimerRepository.cambiarEspera("flotando")
                    (requireActivity() as MainActivity).cargarTemporizadoresActivosMojes()
                }
            }
        }
        viewModel.moje.forEach { moje ->
            val bloqueBarraMoje = localInflater.inflate(R.layout.bloque_barra_mojes, localContainerSub, false)
            bloqueBarraMoje.findViewById<TextView>(R.id.textoMoje1).text = moje.nombre + " " + moje.moje
            bloqueBarraMoje.apply {
                isClickable = true
                isFocusable = true
            }
            val typedValue2 = android.util.TypedValue()
            requireContext().theme.resolveAttribute(com.google.android.material.R.attr.colorSurfaceVariant, typedValue2, true)
            bloqueBarraMoje.backgroundTintList = ColorStateList.valueOf(typedValue2.data)
            bloqueBarraMoje.setOnClickListener{
                cargarIngredientes(localContainer,localContainerSub,bloqueBarraMoje,moje.nombre,moje)
                contenedorListaTemporizadores?.visibility = View.GONE
                pestanaTemporizadores?.backgroundTintList = ColorStateList.valueOf(typedValue2.data)
                scrollViewMojes?.visibility = View.VISIBLE
                botonTerminarMoje?.visibility = View.VISIBLE
                textoCentrado?.visibility = View.GONE
                TimerRepository.cambiarEspera("flotando")
                (requireActivity() as MainActivity).cargarTemporizadoresActivosMojes()
            }
            localContainerSub.tag = moje.nombre
            localContainerSub.addView(bloqueBarraMoje)
            if (contenedorListaTemporizadores?.visibility == View.VISIBLE) {
                primero = false
            }
            if (primero) {
                bloqueBarraMoje.post { // Usa post para asegurar que la vista está lista
                    cargarIngredientes(localContainer,localContainerSub,bloqueBarraMoje,moje.nombre,moje)
                    contenedorListaTemporizadores?.visibility = View.GONE
                    pestanaTemporizadores?.backgroundTintList = ColorStateList.valueOf(typedValue2.data)
                    scrollViewMojes?.visibility = View.VISIBLE
                    botonTerminarMoje?.visibility = View.VISIBLE
                    textoCentrado?.visibility = View.GONE
                    TimerRepository.cambiarEspera("flotando")
                    (requireActivity() as MainActivity).cargarTemporizadoresActivosMojes()
                }
                primero = false
            }
        }
    }
    fun cargarIngredientes (principal: LinearLayout, barra: LinearLayout, subBarra: View, nombre: String, moje: Moje ){
        val typedValue = android.util.TypedValue()
        val typedValue2 = android.util.TypedValue()
        val inflater = LayoutInflater.from(requireContext())
        val terminar = view?.findViewById<TextView>(R.id.terminarMoje)

        requireContext().theme.resolveAttribute(android.R.attr.colorBackground, typedValue, true)
        requireContext().theme.resolveAttribute(com.google.android.material.R.attr.colorSurfaceVariant, typedValue2, true)

        barra.children.forEach { child ->
            child.backgroundTintList = ColorStateList.valueOf(typedValue2.data)
        }
        subBarra.backgroundTintList = ColorStateList.valueOf(typedValue.data)

        principal.children
            .filter { it.id == R.id.borrarBloque }
            .toList() // evita modificación durante iteración
            .forEach { principal.removeView(it) }
        val contenedorAbajo = principal.findViewById<LinearLayout>(R.id.ponerAntes)
        contenedorAbajo.removeAllViews()

        moje.ingredientes.forEach { ingrediente ->
            val bloqueIngrediente = inflater.inflate(R.layout.bloque_mojes_tachable, principal, false)
            bloqueIngrediente.findViewById<TextView>(R.id.tachable_ingrediente).text = ingrediente.ingrediente
            bloqueIngrediente.findViewById<TextView>(R.id.tachable_cantidad).text = ingrediente.cantidad
            val tachar = bloqueIngrediente.findViewById<LinearLayout>(R.id.tachar)
            if (ingrediente.tachado){
                tachar.visibility = View.VISIBLE
            }

            bloqueIngrediente.setOnClickListener{ view ->
                ingrediente.tachado = !ingrediente.tachado
                tachar.visibility = if (ingrediente.tachado) View.VISIBLE else View.GONE
                val parent = view.parent as ViewGroup
                val destino = if (parent == principal) contenedorAbajo else principal
                val direccion = if (destino == contenedorAbajo) 1 else -1

                view.animate()
                    .translationY(100f * direccion)
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction {
                        parent.removeView(view)
                        if (destino == principal) {
                            val currentIndex = principal.indexOfChild(contenedorAbajo)
                            destino.addView(view, currentIndex)
                        }else{
                            destino.addView(view)
                        }

                        view.translationY = -100f * direccion
                        view.animate()
                            .translationY(0f)
                            .alpha(1f)
                            .setDuration(200)
                            .start()
                    }
                    .start()

                val wrappedMojes = viewModel.moje.map { MojeWrapper(it) }
                val gson = Gson()
                val json = gson.toJson(wrappedMojes)
                requireContext().openFileOutput("mojes.json", Context.MODE_PRIVATE).use {
                    it.write(json.toByteArray())
                }
            }
            if (ingrediente.tachado){
                contenedorAbajo.addView(bloqueIngrediente)
            }else{
                val index = principal.indexOfChild(contenedorAbajo)
                principal.addView(bloqueIngrediente,index)
            }
        }

        terminar?.setOnClickListener {
            val actionEliminar = {
                viewModel.moje.remove(moje)

                val wrappedMojes = viewModel.moje.map { MojeWrapper(it) }
                val gson = Gson()
                val json = gson.toJson(wrappedMojes)
                requireContext().openFileOutput("mojes.json", Context.MODE_PRIVATE).use { file ->
                    file.write(json.toByteArray())
                }

                actualizarListaDeMojes()
            }

            val haySinTachar = moje.ingredientes.any { !it.tachado }

            if (haySinTachar) {
                AlertDialog.Builder(requireContext())
                    .setMessage("Todavía hay ingredientes sin tachar. ¿Estás seguro de que quieres terminar y borrar el moje?")
                    .setPositiveButton("Sí") { dialog, _ ->
                        dialog.dismiss()
                        actionEliminar()
                    }
                    .setNegativeButton("No", null)
                    .show()
            } else {
                actionEliminar()
            }
        }
    }
    override fun onPause() {
        super.onPause()
        // Cuando el fragment deja de estar visible
        TimerRepository.cambiarEspera("flotando")
    }

}