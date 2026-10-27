package com.enfoque.app.data.temporizador

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.enfoque.app.EnfoqueApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Servicio en primer plano del bloque de enfoque.
 *
 * Responsabilidades:
 * 1. Mostrar una notificación persistente con la cuenta regresiva.
 * 2. Programar una alarma exacta de respaldo con [AlarmaTemporizador].
 * 3. Al cumplirse el tiempo, devolver la tarea a PENDIENTE, notificar con sonido
 *    y detenerse.
 *
 * Solo se admite un temporizador activo: iniciar otro reemplaza al anterior y
 * cancela su alarma.
 */
class TemporizadorService : Service() {

    private val alcance = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var trabajo: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACCION_DETENER) {
            trabajo?.cancel()
            trabajo = null
            SesionesTemporizador.activa(this)?.let { anterior ->
                AlarmaTemporizador.cancelar(this, anterior.id)
            }
            SesionesTemporizador.limpiarActiva(this)
            Notificaciones.cancelarEnCurso(this)
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        val sesionId = intent?.getStringExtra(EXTRA_SESION_ID)
        val titulo = intent?.getStringExtra(EXTRA_TITULO)
        val finEn = intent?.getLongExtra(EXTRA_FIN_EN, 0L) ?: 0L
        val tareaId = intent?.getLongExtra(EXTRA_TAREA_ID, 0L) ?: 0L

        if (sesionId == null || titulo.isNullOrBlank() || finEn <= System.currentTimeMillis()) {
            stopSelf()
            return START_NOT_STICKY
        }

        // Cancela la alarma de la sesión anterior, si existía.
        SesionesTemporizador.activa(this)?.let { anterior ->
            AlarmaTemporizador.cancelar(this, anterior.id)
        }

        SesionesTemporizador.guardarActiva(this, sesionId, titulo, finEn)

        startForeground(
            Notificaciones.ID_EN_CURSO,
            Notificaciones.enCurso(this, titulo, finEn),
        )
        AlarmaTemporizador.programar(this, sesionId, tareaId, titulo, finEn)

        trabajo?.cancel()
        trabajo = alcance.launch {
            // Recalcula el tiempo restante: si el sistema despierta antes de lo
            // previsto, vuelve a esperar en lugar de notificar de inmediato.
            while (true) {
                val restante = finEn - System.currentTimeMillis()
                if (restante <= 0L) break
                delay(restante)
            }
            finalizar(sesionId, tareaId, titulo)
        }

        return START_NOT_STICKY
    }

    private suspend fun finalizar(sesionId: String, tareaId: Long, titulo: String) {
        // Primero se libera la tarea; si el usuario ya la completó, el UPDATE no
        // hace nada. Después se notifica y se cancela la alarma de respaldo.
        reiniciarEstado(tareaId)
        Notificaciones.notificarFin(this, sesionId, titulo)
        AlarmaTemporizador.cancelar(this, sesionId)
        SesionesTemporizador.limpiarActiva(this)
        stopForeground(Service.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private suspend fun reiniciarEstado(tareaId: Long) {
        if (tareaId <= 0L) return
        runCatching {
            (application as? EnfoqueApplication)?.contenedor?.tareas?.reiniciarSiEnProgreso(tareaId)
        }
    }

    override fun onDestroy() {
        alcance.cancel()
        super.onDestroy()
    }

    companion object {
        const val ACCION_DETENER = "com.enfoque.app.action.TEMPORIZADOR_DETENER"
        const val EXTRA_SESION_ID = "sesion_id"
        const val EXTRA_TAREA_ID = "tarea_id"
        const val EXTRA_TITULO = "titulo"
        const val EXTRA_FIN_EN = "fin_en"

        fun intent(
            context: Context,
            sesionId: String,
            tareaId: Long,
            titulo: String,
            finEn: Long,
        ): Intent = Intent(context, TemporizadorService::class.java).apply {
            putExtra(EXTRA_SESION_ID, sesionId)
            putExtra(EXTRA_TAREA_ID, tareaId)
            putExtra(EXTRA_TITULO, titulo)
            putExtra(EXTRA_FIN_EN, finEn)
        }
    }
}
