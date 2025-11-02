package com.example.panaderia
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object TimerRepository {
    private val _temporizadorModificado = MutableStateFlow<List<Any>>(emptyList())
    private val _temporizadores = MutableStateFlow<Map<String, List<Any>>>(emptyMap())
    private val _cantidadTemporizadores = MutableStateFlow(0)
    private val _pestanaOFlotando = MutableStateFlow("flotandoGrande")
    val temporizadorModificado = _temporizadorModificado.asStateFlow()
    val temporizadores = _temporizadores.asStateFlow()
    val cantidadTemporizadores = _cantidadTemporizadores.asStateFlow()
    val pestanaOFlotando = _pestanaOFlotando.asStateFlow()
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
        if (segundosRestantes.toInt() < 10 && segundosRestantes.toInt() >= 0){
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
            val primerId = this.keys.firstOrNull()
            val tipo = _pestanaOFlotando.value
            val listaActual = this[id]?.toMutableList() // convierte la lista inmutable en mutable
            if (listaActual != null) {
                listaActual[0] = ponerTiempo
                listaActual[3] = segundos
                put(id, listaActual) // actualizar el map con la lista modificada
                if (tipo == "flotandoPequeno"){
                    if (primerId == id){
                        _temporizadorModificado.value = listaActual + id
                    }
                }else{
                    _temporizadorModificado.value = listaActual + id
                }
            }
        }
    }
    fun actualizarTemporizador(mapa:Map<String, List<Any>>) {
        _temporizadores.value = mapa
        _cantidadTemporizadores.value = mapa.size
    }
    fun reiniciarTipo(){
        _pestanaOFlotando.value = "flotandoGrande"
    }
    fun cambiarEspera(tipo: String){
        val valorActual = _pestanaOFlotando.value
        if ( tipo == "pestana"){
            if (valorActual == "flotandoGrande"){
                _pestanaOFlotando.value = "pestanaGrande"
            }
            if (valorActual == "flotandoPequeno"){
                _pestanaOFlotando.value = "pestanaPequeno"
            }
        }
        if(tipo == "flotando"){
            if (valorActual == "pestanaGrande"){
                _pestanaOFlotando.value = "flotandoGrande"
            }
            if (valorActual == "pestanaPequeno"){
                _pestanaOFlotando.value = "flotandoPequeno"
            }
        }
        if (tipo == "flotandoGrande"){
            _pestanaOFlotando.value = "flotandoPequeno"
        }
        if (tipo == "flotandoPequeno"){
            _pestanaOFlotando.value = "flotandoGrande"
        }
    }
}