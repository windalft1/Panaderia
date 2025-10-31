package com.example.panaderia
import android.app.Service
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

class TimerService : Service() {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val temporizadores = mutableMapOf<String, Job>() // id → job
    private val tiemposRestantes = mutableMapOf<String, Int>() // id → segundos

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val accion = intent?.getStringExtra("accion")
        val id = intent?.getStringExtra("id") ?: return START_NOT_STICKY
        val contenedorEtiqueta = intent.getStringExtra("contenedorEtiqueta") ?: ""
        val contenedorTiempo = intent.getIntExtra("contenedorTiempoTemporizador",0)

        when (accion) {
            "DETENER" -> {
                temporizadores[id]?.cancel()
                temporizadores.remove(id)
                tiemposRestantes.remove(id)
                if (temporizadores.isEmpty()) stopSelf()
                return START_STICKY
            }
            "INICIAR" -> {
                val tiempo = intent.getIntExtra("tiempo", -1)
                if (tiempo <= 0) return START_NOT_STICKY
                // si ya hay uno con ese ID, lo reinicia
                temporizadores[id]?.cancel()
                // registrar tiempo inicial
                tiemposRestantes[id] = tiempo

                val job = scope.launch {
                    var tipo = "principal"
                    if (contenedorEtiqueta == "extra") tipo="extra"
                    for (i in tiempo downTo -10) {
                        tiemposRestantes[id] = i
                        if (i == tiempo){
                            scope.launch {
                                TimerRepository.registrarTemporizador(id, i,contenedorEtiqueta,contenedorTiempo,tipo)
                            }
                        }
                        delay(1000)
                        scope.launch {
                            TimerRepository.actualizarSegundos(id, i,tipo)
                        }
                    }
                    temporizadores.remove(id)
                    tiemposRestantes.remove(id)
                    TimerRepository.quitarTemporizadorTerminadoEliminado(id)

                    if (temporizadores.isEmpty()) {
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        stopSelf()
                    }
                }
                temporizadores[id] = job
            }
            "ORDENAR" -> {
                TimerRepository.actualizarTemporizador(id,contenedorEtiqueta,contenedorTiempo)
            }
        }
        startForeground(1, createNotification("Temporizadores activos"))
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
    private fun createNotification(text: String): Notification {
        val channelId = "timer_channel"
        val channel = NotificationChannel(
            channelId,
            "Temporizador",
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Temporizador activo")
            .setContentText(text)
            .setSmallIcon(R.drawable.mas_blanco)
            .setOngoing(true)
            .build()
    }
}