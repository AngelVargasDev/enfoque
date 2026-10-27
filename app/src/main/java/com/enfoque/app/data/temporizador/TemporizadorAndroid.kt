package com.enfoque.app.data.temporizador

import android.content.Context
import androidx.core.content.ContextCompat
import com.enfoque.app.domain.model.Tarea
import com.enfoque.app.domain.temporizador.Temporizador
import java.util.UUID

/** Implementación que arranca [TemporizadorService] con la duración de la tarea. */
class TemporizadorAndroid(private val context: Context) : Temporizador {

    override fun iniciar(tarea: Tarea): Boolean {
        val minutos = tarea.duracionMinutos ?: return false
        if (minutos <= 0) return false

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
}
