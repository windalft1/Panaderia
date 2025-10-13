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
import androidx.appcompat.app.AppCompatActivity // <-- CAMBIO 1 (AÑADIDO)
import com.google.android.material.appbar.MaterialToolbar
import android.widget.FrameLayout
import android.widget.TextView
import android.animation.ValueAnimator
import android.animation.AnimatorListenerAdapter
import android.animation.Animator

class recetas : Fragment() {
    lateinit var viewModel: RecetasViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recetas, container, false)
    }

    // CAMBIO 2 (AÑADIDO): NUEVO MÉTODO onViewCreated
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtenemos el mismo ViewModel que la Activity
        viewModel = ViewModelProvider(requireActivity()).get(RecetasViewModel::class.java)

        // Esta línea cambia el título de la Toolbar en MainActivity
        (activity as? AppCompatActivity)?.supportActionBar?.title = "Recetas"

        val boton = view.findViewById<View>(R.id.icon_mas_recetas)
        boton.setOnClickListener {
            replaceFragment(nueva_receta())
        }

        val container = view.findViewById<LinearLayout>(R.id.containerRecetas)
        val inflater = LayoutInflater.from(requireContext())

        viewModel.receta?.forEach { receta ->
            // esto solo se ejecuta si recetas no es null
            val bloqueReceta = inflater.inflate(R.layout.bloque_receta, container, false)

            val colapsable = bloqueReceta.findViewById<LinearLayout>(R.id.subReceta)

            val btnExpandir = bloqueReceta.findViewById<View>(R.id.botonIngredientes)
            val btnEditar = bloqueReceta.findViewById<View>(R.id.editar)

            btnExpandir.setOnClickListener {
                if (colapsable.visibility == View.VISIBLE) {
                    collapse(colapsable)
                } else {
                    expand(colapsable)
                }
            }

            bloqueReceta.findViewById<TextView>(R.id.recetaS).text = receta.nombre
            bloqueReceta.findViewById<TextView>(R.id.porcionesS).text = "${receta.porciones}"

            val contenedorIngredientes = bloqueReceta.findViewById<LinearLayout>(R.id.meterIngredientes)

            for (ing in receta.ingredientes) {
                val bloqueIngrediente = inflater.inflate(R.layout.bloque_ingredientes, contenedorIngredientes, false)
                bloqueIngrediente.findViewById<TextView>(R.id.ingrediente1).text = ing.ingrediente
                bloqueIngrediente.findViewById<TextView>(R.id.cantidad1).text = ing.cantidad
                contenedorIngredientes.addView(bloqueIngrediente)
            }

            container.addView(bloqueReceta)
        }
    }

    override fun onResume() {
        super.onResume()
        // Cuando el fragment se vuelve visible
        (activity?.findViewById<MaterialToolbar>(R.id.topAppBar3))?.visibility = View.VISIBLE
    }

    override fun onPause() {
        super.onPause()
        // Cuando el fragment deja de estar visible
        (activity?.findViewById<MaterialToolbar>(R.id.topAppBar3))?.visibility = View.GONE
    }
    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, fragment) // R.id.fragment_container es el ID de tu FragmentContainerView
            addToBackStack(null)
            commit()
        }
    }
    fun expand(view: View) {
        // Medimos la altura natural
        view.measure(
            View.MeasureSpec.makeMeasureSpec((view.parent as View).width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        val targetHeight = view.measuredHeight

        // Empezamos desde height = 0 y visible
        view.layoutParams.height = 0
        view.visibility = View.VISIBLE

        val animator = ValueAnimator.ofInt(0, targetHeight)
        animator.addUpdateListener { animation ->
            view.layoutParams.height = animation.animatedValue as Int
            view.requestLayout()
        }
        animator.duration = 300
        animator.start()
    }
    fun collapse(view: View) {
        val initialHeight = view.measuredHeight

        val animator = ValueAnimator.ofInt(initialHeight, 0)
        animator.addUpdateListener { animation ->
            view.layoutParams.height = animation.animatedValue as Int
            view.requestLayout()
        }
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                view.visibility = View.GONE
            }
        })
        animator.duration = 300
        animator.start()
    }

}

