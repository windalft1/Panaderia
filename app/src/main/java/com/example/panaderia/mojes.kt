package com.example.panaderia

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity // <-- CAMBIO 1 (AÑADIDO)

class mojes : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mojes, container, false)
    }

    // CAMBIO 2 (AÑADIDO): NUEVO MÉTODO onViewCreated
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Esta línea cambia el título de la Toolbar en MainActivity
        (activity as? AppCompatActivity)?.supportActionBar?.title = "Mojes y tiempos"


    }
}