package com.enfoque.app.data.temporizador

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.enfoque.app.domain.model.AjustesPomodoro
import com.enfoque.app.domain.model.Tarea
import com.enfoque.app.domain.temporizador.Temporizador
import java.util.UUID

/** Implementación que arranca los servicios de temporizador y Pomodoro. */
class TemporizadorAndroid(private val context: Context) : Temporizador {

    override fun iniciar(tarea: Tarea): Boolean {
        val minutos = tarea.duracionMinutos ?: return false
        if (minutos <= 0) return false

        detenerPomodoro()
        val finEn = System.currentTimeMillis() + minutos * 60_000L
        val sesionId = UUID.randomUUID().toString()

        return try {
            ContextCompat.startForegroundService(
                context,
                TemporizadorService.intent(
                    context = context,
                    sesionId = sesionId,
                    tareaId = tarea.id,
                    titulo = tarea.titulo,
                    finEn = finEn,
                ),
            )
            true
        } catch (_: Exception) {
            // Por ejemplo, ForegroundServiceStartNotAllowedException si el sistema
            // bloquea el arranque en segundo plano.
            false
        }
    }

    override fun iniciarPomodoro(tarea: Tarea, ajustes: AjustesPomodoro): Boolean =
        try {
            detenerTemporizadorSimple()
            val sesionId = UUID.randomUUID().toString()
            ContextCompat.startForegroundService(
                context,
                PomodoroService.intentIniciar(
                    context = context,
                    sesionId = sesionId,
                    tareaId = tarea.id,
                    titulo = tarea.titulo,
                    ajustes = ajustes.normalizado(),
                ),
            )
            true
        } catch (_: Exception) {
            false
        }

    override fun haySesionActiva(): Boolean =
        SesionesTemporizador.activa(context) != null || PomodoroSesiones.activa(context) != null

    override fun detenerPomodoroSiEs(tareaId: Long) {
        val sesion = PomodoroSesiones.activa(context) ?: return
        if (sesion.tareaId != tareaId) return
        detenerPomodoro()
    }

    override fun detenerPomodoro() {
        val sesion = PomodoroSesiones.activa(context) ?: return
        runCatching {
            context.startService(
                Intent(context, PomodoroService::class.java).apply {
                    action = PomodoroContrato.ACCION_DETENER
                    putExtra(PomodoroContrato.EXTRA_SESION_ID, sesion.id)
                },
            )
        }
    }

    override fun detenerTemporizadorSimple() {
        if (SesionesTemporizador.activa(context) == null) return
        runCatching {
            context.startService(
                Intent(context, TemporizadorService::class.java).apply {
                    action = TemporizadorService.ACCION_DETENER
                },
            )
        }
    }
}
