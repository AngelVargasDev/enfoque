package com.enfoque.app.data.recordatorio

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.enfoque.app.data.temporizador.Notificaciones

/** Recibe la alarma del recordatorio y publica la notificación local. */
class RecordatorioReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != RecordatorioContrato.ACCION) return

        val tareaId = intent.getLongExtra(RecordatorioContrato.EXTRA_TAREA_ID, 0L)
        val titulo = intent.getStringExtra(RecordatorioContrato.EXTRA_TITULO) ?: return
        val descripcion = intent.getStringExtra(RecordatorioContrato.EXTRA_DESCRIPCION).orEmpty()
        Notificaciones.notificarRecordatorio(context, tareaId, titulo, descripcion)
    }
}
