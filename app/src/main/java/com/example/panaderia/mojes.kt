package com.example.panaderia

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import androidx.lifecycle.ViewModelProvider
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import android.widget.FrameLayout
import android.widget.TextView
import android.animation.ValueAnimator
import android.animation.AnimatorListenerAdapter
import android.animation.Animator
import android.R.attr.theme
import android.util.TypedValue
import android.content.res.Resources
import android.util.Log
import androidx.core.view.children
import kotlin.sequences.forEach
import android.content.res.ColorStateList
import com.google.gson.reflect.TypeToken
import android.content.Context
import androidx.appcompat.app.AlertDialog


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
        val lista = viewModel.moje ?: emptyList()
        if (lista.isEmpty()) {
            view.findViewById<LinearLayout>(R.id.cargaIngredientes).visibility = View.GONE
            view.findViewById<TextView>(R.id.terminarMoje).visibility = View.GONE
        } else {
            container = view.findViewById<LinearLayout>(R.id.cargaIngredientes)
            containerSub = view.findViewById<LinearLayout>(R.id.barraMojes)

            actualizarListaDeMojes()
        }
    }

    fun actualizarListaDeMojes() {
        val localContainer = container ?: return
        val localContainerSub = containerSub ?: return
        val localInflater = inflater ?: return

        localContainerSub.removeAllViews()
        // Limpia los ingredientes del moje previamente seleccionado
        localContainer.children
            .filter { it.id == R.id.borrarBloque }
            .toList()
            .forEach { localContainer.removeView(it) }
        val contenedorAbajo = localContainer.findViewById<LinearLayout>(R.id.ponerAntes)
        contenedorAbajo?.removeAllViews()


        var primero = true
        if (viewModel.moje.isEmpty()) {
            view?.findViewById<LinearLayout>(R.id.cargaIngredientes)?.visibility = View.GONE
            view?.findViewById<TextView>(R.id.terminarMoje)?.visibility = View.GONE
        }
        viewModel.moje?.forEach { moje ->
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
            }
            localContainerSub.tag = moje.nombre
            localContainerSub.addView(bloqueBarraMoje)
            if (primero) {
                bloqueBarraMoje.post { // Usa post para asegurar que la vista está lista
                    cargarIngredientes(localContainer,localContainerSub,bloqueBarraMoje,moje.nombre,moje)
                }
                primero = false
            }
        }
    }
    fun cargarIngredientes (principal: LinearLayout, barra: LinearLayout, subBarra: View, nombre: String, moje: Moje ){
        val typedValue = android.util.TypedValue()
        val typedValue2 = android.util.TypedValue()
        val inflater = LayoutInflater.from(requireContext())
        val tiempos = view?.findViewById<TextView>(R.id.tiempos)
        if (tiempos != null) {
            tiempos.visibility = View.VISIBLE
        }
        val terminar = view?.findViewById<TextView>(R.id.terminarMoje)
        if (terminar != null) {
            terminar.visibility = View.VISIBLE
        }

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

}