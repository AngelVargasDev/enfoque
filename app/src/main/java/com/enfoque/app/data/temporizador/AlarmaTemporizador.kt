package com.enfoque.app.data.temporizador

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.enfoque.app.data.alarma.Alarmas

/**
 * Alarma exacta de respaldo para el fin del temporizador. Aunque el servicio en
 * primer plano sea destruido por el sistema, esta alarma despierta al dispositivo
 * y [AlarmaReceiver] publica la notificación con sonido.
 */
object AlarmaTemporizador {

    const val ACCION = "com.enfoque.app.action.TEMPORIZADOR_FIN"
    const val EXTRA_SESION_ID = "sesion_id"
    const val EXTRA_TAREA_ID = "tarea_id"
    const val EXTRA_TITULO = "titulo"

    fun programar(
        context: Context,
        sesionId: String,
        tareaId: Long,
        titulo: String,
        finEn: Long,
    ) {
        Alarmas.programarReloj(
            context = context,
            disparoEn = finEn,
            pendingIntent = pendingIntent(context, sesionId, tareaId, titulo),
        )
    }

    fun cancelar(context: Context, sesionId: String) {
        context.getSystemService(AlarmManager::class.java)
            ?.cancel(pendingIntent(context, sesionId, 0L, null))
    }

    private fun pendingIntent(
        context: Context,
        sesionId: String,
        tareaId: Long,
        titulo: String?,
    ): PendingIntent {
        val intent = Intent(context, AlarmaReceiver::class.java).apply {
            action = ACCION
            putExtra(EXTRA_SESION_ID, sesionId)
            putExtra(EXTRA_TAREA_ID, tareaId)
            putExtra(EXTRA_TITULO, titulo)
        }
        return PendingIntent.getBroadcast(
            context,
            sesionId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
