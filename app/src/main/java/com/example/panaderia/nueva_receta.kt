package com.example.panaderia

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog   // Para el AlertDialog
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity // <-- CAMBIO 1 (AÑADIDO)
import android.widget.LinearLayout
import android.widget.Button
import androidx.compose.ui.semantics.text
import android.widget.EditText
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.input.key.type
import kotlin.concurrent.write
import kotlin.io.path.exists
import kotlin.io.path.readText
// Pega estas líneas junto a tus otras importaciones
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File
import android.content.Context
import android.widget.TextView
import com.google.android.material.appbar.MaterialToolbar


class nueva_receta : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_nueva_receta, container, false)
    }

    // CAMBIO 2 (AÑADIDO): NUEVO MÉTODO onViewCreated
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (tieneContenidoSinGuardar()) {
                (activity as? MainActivity)?.alerta(requireContext(), onConfirm = {
                    parentFragmentManager.popBackStack()
                })
            }else{
                parentFragmentManager.popBackStack()
            }
        }

        // Esta línea cambia el título de la Toolbar en MainActivity
        (activity as? AppCompatActivity)?.supportActionBar?.title = "Nueva Receta"

        val container = view.findViewById<LinearLayout>(R.id.containerBloques)
        val inflater = LayoutInflater.from(requireContext())

        //boton agregar ingredientes
        val boton = view.findViewById<View>(R.id.mas_ingredientes)
        boton.setOnClickListener {
            if (container.childCount >0){
                val ultimoHijo = container.getChildAt(container.childCount - 1)
                val nombreIngredienteEditText = ultimoHijo.findViewById<EditText>(R.id.ingrediente)
                val cantidadEditText = ultimoHijo.findViewById<EditText>(R.id.cantidad)
                // Obtiene el texto de cada EditText
                val nombreIngrediente = nombreIngredienteEditText.text.toString()
                val cantidad = cantidadEditText.text.toString()
                if(nombreIngrediente.isEmpty() && cantidad.isEmpty()){
                    return@setOnClickListener
                }
            }
            val nuevoBloque = inflater.inflate(R.layout.bloques_nueva_receta, container, false)
            val btnEliminar = nuevoBloque.findViewById<View>(R.id.quitar)
            btnEliminar.setOnClickListener {
                // Aquí puedes, por ejemplo, eliminar el bloque
                container.removeView(nuevoBloque)
            }
            container.addView(nuevoBloque)
        }

        //boton cancelar
        val botonCancelar = view.findViewById<View>(R.id.cancelar)
        botonCancelar.setOnClickListener {
            if (tieneContenidoSinGuardar()) {
                (activity as? MainActivity)?.alerta(requireContext(), onConfirm = {
                    parentFragmentManager.popBackStack()
                })
            }else{
                parentFragmentManager.popBackStack()
            }
        }

        //boton guardar
        val botonConfirmar = view.findViewById<View>(R.id.confirmar)
        botonConfirmar.setOnClickListener {
            val datosDeLaReceta = recopilarDatosReceta()
            if (datosDeLaReceta.isNotEmpty()) {
                val datosParaGuardar = mapOf("receta" to datosDeLaReceta)
                guardarRecetaEnArchivo(datosParaGuardar)
                Log.d("DATOS_RECETA", datosDeLaReceta.toString())
            }
        }
    }

    //solo se ejecuta cuando oprimo el boton cancelar o guardar
    fun tieneContenidoSinGuardar(): Boolean {
        // 'view' es la vista raíz del fragmento. Asegúrate de que no sea nula.
        val container = view?.findViewById<LinearLayout>(R.id.containerBloques)

        // Si el contenedor no se encuentra o no tiene hijos, devuelve false.
        // El operador elvis (?:) es perfecto para esto.
        return (container?.childCount ?: 0) > 0
    }

    private fun recopilarDatosReceta(): Map<String, Any> {
        val datosReceta = mutableMapOf<String, Any>()
        val listaIngredientes = mutableListOf<Map<String, String>>()

        // 1. Obtener la vista raíz de forma segura
        val vistaActual = view ?: return emptyMap() // Si la vista no existe, devuelve un mapa vacío

        // 2. Obtener datos de los EditText FIJOS
        val nombreRecetaEditText = vistaActual.findViewById<EditText>(R.id.nombre_receta)
        val nombreReceta = nombreRecetaEditText.text.toString()

        val cantidadRecetaEditText = vistaActual.findViewById<EditText>(R.id.cantidad_receta)
        val cantidadReceta = cantidadRecetaEditText.text.toString()

        val container = vistaActual.findViewById<LinearLayout>(R.id.containerBloques)

        var error1 = ""
        when{
            nombreReceta.isBlank() && cantidadReceta.isBlank() && container.childCount == 0 -> {error1="Nada que guardar"}
            nombreReceta.isBlank() && cantidadReceta.isBlank() -> {error1="Falta el nombre de la receta y la cantidad de porciones"}
            nombreReceta.isBlank() -> {error1="Falta el nombre de la receta"}
            cantidadReceta.isBlank() -> {error1="Falta la cantidad de porciones"}
            container.childCount == 0 -> {error1="Agregue ingredientes"}
        }
        if (error1 != "") {
            AlertDialog.Builder(requireContext())
                .setMessage(error1)
                .setNegativeButton("Ok") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
            return emptyMap()
        }
        datosReceta["nombre"] = nombreReceta
        datosReceta["porciones"] = cantidadReceta

        // 3. Recorrer el contenedor de bloques DINÁMICOS
        val bloquesParaEliminar = mutableListOf<View>()

        for (i in 0 until container.childCount) {
            val bloqueIngrediente = container.getChildAt(i) // Obtiene cada bloque (ConstraintLayout)


            // Encuentra los EditText dentro de este bloque específico
            val nombreIngredienteEditText = bloqueIngrediente.findViewById<EditText>(R.id.ingrediente)
            val cantidadEditText = bloqueIngrediente.findViewById<EditText>(R.id.cantidad)

            // Obtiene el texto de cada EditText
            val nombreIngrediente = nombreIngredienteEditText.text.toString()
            val cantidad = cantidadEditText.text.toString()

            // Solo añade el ingrediente si ambos campos tienen texto
            var error2 = ""
            when{
                nombreIngrediente.isBlank() && cantidad.isBlank() && container.childCount < 2-> {
                    error2 = "Agregue almenos 2 ingredientes"
                }
                nombreIngrediente.isBlank() && cantidad.isBlank() -> {
                    bloquesParaEliminar.add(bloqueIngrediente)
                }
                nombreIngrediente.isBlank() -> {
                    error2 = "Falta el nombre de un ingrediente"
                }
                cantidad.isBlank() -> {
                    error2 = "Falta la cantidad de un ingrediente"
                }
                container.childCount < 2->{
                    error2 = "Agregue almenos 2 ingredientes"
                }
                else -> {
                    val ingredienteMap = mapOf(
                        "ingrediente" to nombreIngrediente,
                        "cantidad" to cantidad
                    )
                    listaIngredientes.add(ingredienteMap)
                }
            }
            if (error2 != "") {
                AlertDialog.Builder(requireContext())
                    .setMessage(error2)
                    .setNegativeButton("Ok") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
                return emptyMap()
            }
        }

        for (bloque in bloquesParaEliminar) {
            container.removeView(bloque)
        }

        // 4. Añadir la lista de ingredientes al mapa principal
        datosReceta["ingredientes"] = listaIngredientes

        return datosReceta
    }

    private fun guardarRecetaEnArchivo(recetaEnvueltas: Map<String, Any>) {
        val nombreArchivo = "recetas.json"
        val gson = GsonBuilder().setPrettyPrinting().create() // Usamos el "bonito" desde el principio
        val listaDeRecetas: MutableList<Map<String, Any>>

        try {
            // 1. INTENTAR LEER EL ARCHIVO EXISTENTE
            val archivo = File(requireContext().filesDir, nombreArchivo)
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
            requireContext().openFileOutput(nombreArchivo, Context.MODE_PRIVATE).use { outputStream ->
                outputStream.write(jsonActualizado.toByteArray())
            }
            parentFragmentManager.popBackStack()

        } catch (e: Exception) {
            e.printStackTrace()
            AlertDialog.Builder(requireContext())
                .setMessage("Error al guardar la receta: ${e.message}")
                .setNegativeButton("Ok") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        (activity?.findViewById<LinearLayout>(R.id.opciones))?.visibility = View.GONE
        (activity?.findViewById<TextView>(R.id.opcion1))?.visibility = View.GONE
        (activity?.findViewById<TextView>(R.id.opcion2))?.visibility = View.GONE
        (activity?.findViewById<TextView>(R.id.opcion3))?.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        (activity?.findViewById<LinearLayout>(R.id.opciones))?.visibility = View.VISIBLE
    }

}