package com.enfoque.app.data.temporizador

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.enfoque.app.data.alarma.Alarmas
import com.enfoque.app.data.temporizador.PomodoroContrato.Fase

/** Alarmas exactas de respaldo para el final de cada fase Pomodoro. */
object PomodoroAlarmas {

    fun programar(
        context: Context,
        sesionId: String,
        fase: Fase,
        finEn: Long,
        token: String,
        titulo: String,
        tareaId: Long,
    ) {
        Alarmas.programar(
            context = context,
            disparoEn = finEn,
            pendingIntent = pendingIntent(
                context = context,
                sesionId = sesionId,
                fase = fase,
                token = token,
                titulo = titulo,
                tareaId = tareaId,
            ),
        )
    }

    fun cancelar(context: Context, sesionId: String, fase: Fase) {
        context.getSystemService(AlarmManager::class.java)
            ?.cancel(pendingIntent(context, sesionId, fase, null, null, 0L))
    }

    private fun pendingIntent(
        context: Context,
        sesionId: String,
        fase: Fase,
        token: String?,
        titulo: String?,
        tareaId: Long,
    ): PendingIntent {
        val intent = Intent(context, PomodoroAlarmaReceiver::class.java).apply {
            action = PomodoroContrato.ACCION_FIN
            putExtra(PomodoroContrato.EXTRA_SESION_ID, sesionId)
            putExtra(PomodoroContrato.EXTRA_FASE, fase.name)
            putExtra(PomodoroContrato.EXTRA_TOKEN, token)
            putExtra(PomodoroContrato.EXTRA_TITULO, titulo)
            putExtra(PomodoroContrato.EXTRA_TAREA_ID, tareaId)
        }
        return PendingIntent.getBroadcast(
            context,
            sesionId.hashCode() * 31 + fase.ordinal,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
