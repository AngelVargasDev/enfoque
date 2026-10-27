package com.enfoque.app.data.temporizador

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.enfoque.app.data.temporizador.PomodoroContrato.Fase

/**
 * Alarma de respaldo al terminar una fase. Si el servicio sigue vivo, la
 * notificación se deduplica; si el proceso murió, avisa y limpia la sesión.
 */
class PomodoroAlarmaReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != PomodoroContrato.ACCION_FIN) return

        val sesionId = intent.getStringExtra(PomodoroContrato.EXTRA_SESION_ID) ?: return
        val fase = intent.getStringExtra(PomodoroContrato.EXTRA_FASE)
            ?.let { runCatching { Fase.valueOf(it) }.getOrNull() }
            ?: return
        val token = intent.getStringExtra(PomodoroContrato.EXTRA_TOKEN) ?: return
        val sesion = PomodoroSesiones.activa(context)
        val titulo = sesion?.titulo
            ?: intent.getStringExtra(PomodoroContrato.EXTRA_TITULO)
            ?: "Pomodoro"

        Notificaciones.notificarFinFase(context, sesionId, fase, titulo, token)

        if (!PomodoroService.activo) {
            PomodoroSesiones.limpiar(context)
            context.stopService(Intent(context, PomodoroService::class.java))
        }
    }
}
