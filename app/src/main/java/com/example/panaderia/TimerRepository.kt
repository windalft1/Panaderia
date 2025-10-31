package com.example.panaderia
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object TimerRepository {
    private val _temporizadorModificado = MutableStateFlow<List<Any>>(emptyList())
    private val _temporizadores = MutableStateFlow<Map<String, List<Any>>>(emptyMap())
    val temporizadores = _temporizadores.asStateFlow()

    fun registrarTemporizador(id: String, segundos: Int, contenedorEtiqueta: String, contenedorTiempo: Int,tipo: String) {
        if (tipo == "principal"){
            _temporizadoresPrincipales.value = _temporizadoresPrincipales.value.toMutableMap().apply {

            }
        }else{
            _temporizadoresExtra.value = _temporizadoresExtra.value.toMutableMap().apply {
                put(id, listOf("00:00:00",contenedorEtiqueta,contenedorTiempo,segundos))
                actualizarSegundos(id, segundos)
            }
        }
    }

    fun actualizarSegundos(id: String, segundos: Int) {
        lateinit var ponerTiempo : String

        var horas = (segundos / 3600).toString()
        var minutos = ((segundos % 3600) / 60).toString()
        var segundosRestantes = (segundos % 60).toString()

        if (horas.toInt() < 10){
            horas = "0$horas"
        }
        if (minutos.toInt() < 10){
            minutos = "0$minutos"
        }
        if (segundosRestantes.toInt() < 10){
            segundosRestantes = "0$segundosRestantes"
        }

        if (segundos < 60){
            ponerTiempo = segundosRestantes
        }else if (segundos < 3600){
            ponerTiempo = "$minutos:$segundosRestantes"
        }else{
            ponerTiempo = "$horas:$minutos:$segundosRestantes"
        }

        _temporizadores.value = _temporizadores.value.toMutableMap().apply {
            val listaActual = this[id]?.toMutableList() // convierte la lista inmutable en mutable
            if (listaActual != null) {
                listaActual[0] = ponerTiempo
                listaActual[3] = segundos
                put(id, listaActual) // actualizar el map con la lista modificada
                _temporizadorModificado.value = listaActual + id
            }
        }
    }
    fun actualizarTemporizador(id: String, contenedorEtiqueta: String, contenedorTiempo: Int) {
        _temporizadores.value = _temporizadores.value.toMutableMap().apply {
            val listaActual = this[id]?.toMutableList() // convierte la lista inmutable en mutable
            if (listaActual != null) {
                listaActual[1] = contenedorEtiqueta
                listaActual[2] = contenedorTiempo
                put(id, listaActual) // actualizar el map con la lista modificada
            }
        }
    }
    fun quitarTemporizadorTerminadoEliminado(id: String){
        _temporizadores.value = _temporizadores.value.toMutableMap().apply {
            remove(id)
        }
    }
}