package com.enfoque.app.data.temporizador

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.enfoque.app.EnfoqueApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Respaldo del temporizador cuando el proceso del servicio ya no está vivo. */
class AlarmaReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != AlarmaTemporizador.ACCION) return

        val sesionId = intent.getStringExtra(AlarmaTemporizador.EXTRA_SESION_ID) ?: return
        val titulo = intent.getStringExtra(AlarmaTemporizador.EXTRA_TITULO) ?: return
        val tareaId = intent.getLongExtra(AlarmaTemporizador.EXTRA_TAREA_ID, 0L)

        val resultado = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Notificaciones.notificarFin(context, sesionId, titulo)
                SesionesTemporizador.limpiarActiva(context)
                if (tareaId > 0L) {
                    runCatching {
                        (context.applicationContext as EnfoqueApplication)
                            .contenedor
                            .tareas
                            .reiniciarSiEnProgreso(tareaId)
                    }
                }
                context.stopService(Intent(context, TemporizadorService::class.java))
            } finally {
                resultado.finish()
            }
        }
    }
}
