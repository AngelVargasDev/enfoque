package com.enfoque.app.data.recordatorio

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.enfoque.app.data.alarma.Alarmas
import com.enfoque.app.domain.model.Tarea
import java.time.ZoneId

/**
 * Programa un recordatorio local para el día de vencimiento de la tarea.
 * El mismo requestCode (id de la tarea) identifica la alarma, de modo que
 * reprogramar o cancelar no deja alarmas huérfanas.
 */
class RecordatorioProgramador(private val context: Context) {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun programar(tarea: Tarea) {
        cancelar(tarea.id)
        if (tarea.estaCompletada) return

        val hora = tarea.horaRecordatorio ?: return
        val disparo = tarea.fechaVencimiento
            .atTime(hora)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        if (disparo <= System.currentTimeMillis()) return

        Alarmas.programar(
            context = context,
            disparoEn = disparo,
            pendingIntent = pendingIntent(tarea.id, tarea.titulo, tarea.descripcion),
        )
    }

    fun cancelar(id: Long) {
        alarmManager?.cancel(pendingIntent(id, null, null))
    }

    private fun pendingIntent(id: Long, titulo: String?, descripcion: String?): PendingIntent {
        val intent = Intent(context, RecordatorioReceiver::class.java).apply {
            action = RecordatorioContrato.ACCION
            putExtra(RecordatorioContrato.EXTRA_TAREA_ID, id)
            putExtra(RecordatorioContrato.EXTRA_TITULO, titulo)
            putExtra(RecordatorioContrato.EXTRA_DESCRIPCION, descripcion)
        }
        return PendingIntent.getBroadcast(
            context,
            id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
