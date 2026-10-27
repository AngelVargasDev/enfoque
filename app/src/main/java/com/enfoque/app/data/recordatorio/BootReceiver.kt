package com.enfoque.app.data.recordatorio

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.enfoque.app.EnfoqueApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Las alarmas se pierden al reiniciar el dispositivo. Este receptor vuelve a
 * programar todos los recordatorios pendientes usando Room como fuente de verdad.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val accionesValidas = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
        )
        val accion = intent.action ?: return
        if (accion !in accionesValidas) return

        val resultado = goAsync()
        val contenedor = (context.applicationContext as EnfoqueApplication).contenedor

        CoroutineScope(Dispatchers.IO).launch {
            try {
                contenedor.tareas.reprogramarRecordatorios()
            } finally {
                resultado.finish()
            }
        }
    }
}
