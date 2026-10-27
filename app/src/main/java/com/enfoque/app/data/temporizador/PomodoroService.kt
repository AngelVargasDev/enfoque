package com.enfoque.app.data.temporizador

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.enfoque.app.EnfoqueApplication
import com.enfoque.app.domain.model.AjustesPomodoro
import com.enfoque.app.data.temporizador.PomodoroContrato.Fase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Sesión Pomodoro completa en un servicio en primer plano.
 *
 * Mantiene la cuenta regresiva, programa una alarma exacta por fase, avisa con
 * sonido al terminar cada bloque y encadena enfoque y descansos. El servicio
 * sigue vivo durante los descansos, por lo que la sesión no se pierde al apagar
 * la pantalla.
 */
class PomodoroService : Service() {

    private val alcance = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var trabajo: Job? = null

    private var sesionId = ""
    private var tareaId = 0L
    private var titulo = ""
    @Volatile
    private var fase = Fase.ENFOQUE
    private var bloque = 1
    private var ajustes = AjustesPomodoro()
    @Volatile
    private var finActual = 0L
    private var faseSaltada = false
    @Volatile
    private var esperaContinuar: CompletableDeferred<Unit>? = null

    @Volatile
    private var pausado = false

    @Volatile
    private var saltar = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        activo = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val accion = intent?.action ?: PomodoroContrato.ACCION_INICIAR

        if (accion == PomodoroContrato.ACCION_INICIAR) {
            if (intent == null) {
                stopSelf()
                return START_NOT_STICKY
            }
            iniciar(intent)
            return START_NOT_STICKY
        }

        if (accion == PomodoroContrato.ACCION_DETENER) {
            detener()
            return START_NOT_STICKY
        }

        // Si el proceso murió, una acción desde una notificación vieja no debe
        // crear un servicio fantasma.
        if (sesionId.isEmpty()) {
            stopSelf()
            return START_NOT_STICKY
        }

        val id = intent?.getStringExtra(PomodoroContrato.EXTRA_SESION_ID)
        if (id != null && id != sesionId) return START_NOT_STICKY

        when (accion) {
            PomodoroContrato.ACCION_PAUSA -> {
                pausado = true
                cancelarAlarmaActual()
                mostrarPausa()
            }

            PomodoroContrato.ACCION_REANUDAR -> pausado = false
            PomodoroContrato.ACCION_SALTAR -> {
                saltar = true
                esperaContinuar?.complete(Unit)
            }

            PomodoroContrato.ACCION_CONTINUAR -> esperaContinuar?.complete(Unit)
        }
        return START_NOT_STICKY
    }

    private fun iniciar(intent: Intent) {
        val nuevoId = intent.getStringExtra(PomodoroContrato.EXTRA_SESION_ID)
        val nuevoTitulo = intent.getStringExtra(PomodoroContrato.EXTRA_TITULO)
        if (nuevoId.isNullOrBlank() || nuevoTitulo.isNullOrBlank()) {
            stopSelf()
            return
        }

        // Reemplaza cualquier sesión anterior.
        trabajo?.cancel()
        trabajo = null
        if (sesionId.isNotEmpty()) {
            cancelarAlarmaActual()
            PomodoroSesiones.limpiar(this)
        }

        sesionId = nuevoId
        titulo = nuevoTitulo
        tareaId = intent.getLongExtra(PomodoroContrato.EXTRA_TAREA_ID, 0L)
        ajustes = AjustesPomodoro(
            minutosEnfoque = intent.getIntExtra(PomodoroContrato.EXTRA_ENFOQUE, 25),
            minutosDescansoCorto = intent.getIntExtra(PomodoroContrato.EXTRA_CORTO, 5),
            minutosDescansoLargo = intent.getIntExtra(PomodoroContrato.EXTRA_LARGO, 15),
            ciclosAntesDescansoLargo = intent.getIntExtra(PomodoroContrato.EXTRA_CICLOS, 4),
            autoIniciarDescansos = intent.getBooleanExtra(PomodoroContrato.EXTRA_AUTO_DESCANSOS, true),
            autoIniciarEnfoque = intent.getBooleanExtra(PomodoroContrato.EXTRA_AUTO_ENFOQUE, false),
        ).normalizado()

        fase = Fase.ENFOQUE
        bloque = 1
        pausado = false
        saltar = false
        faseSaltada = false
        finActual = System.currentTimeMillis() + duracionFaseMs()

        startForeground(
            Notificaciones.ID_POMODORO_EN_CURSO,
            Notificaciones.pomodoroEnCurso(
                context = this,
                sesionId = sesionId,
                titulo = titulo,
                fase = fase,
                finEn = finActual,
                bloque = bloque,
                ciclos = ajustes.ciclosAntesDescansoLargo,
            ),
        )
        guardarSesion(finActual)

        trabajo = alcance.launch { bucle() }
    }

    private suspend fun bucle() {
        while (currentCoroutineContext().isActive) {
            val fin = ejecutarFase()
            if (fin <= 0L) {
                if (currentCoroutineContext().isActive) detener()
                return
            }

            val saltada = faseSaltada
            faseSaltada = false
            if (!saltada) {
                Notificaciones.notificarFinFase(
                    context = this,
                    sesionId = sesionId,
                    fase = fase,
                    titulo = titulo,
                    token = token(fase, fin),
                )
            }

            val siguiente = calcularSiguiente()
            val auto = if (fase == Fase.ENFOQUE) {
                ajustes.autoIniciarDescansos
            } else {
                ajustes.autoIniciarEnfoque
            }

            if (!auto) {
                esperaContinuar = CompletableDeferred()
                mostrarEspera(siguiente)
                esperaContinuar?.await()
                esperaContinuar = null
                if (!currentCoroutineContext().isActive) return
            }

            avanzarA(siguiente)
            pausado = false
            saltar = false
        }
    }

    /**
     * Ejecuta una fase y devuelve el instante en que terminó. Si se detuvo,
     * devuelve 0. La pausa conserva el tiempo restante y reprograma la alarma.
     */
    private suspend fun ejecutarFase(): Long {
        var restante = duracionFaseMs()

        while (restante > 0L && currentCoroutineContext().isActive) {
            if (pausado) {
                delay(200)
                continue
            }

            val fin = System.currentTimeMillis() + restante
            finActual = fin
            programarAlarmaActual(fin)
            actualizarNotificacionEnCurso(fin)
            guardarSesion(fin)

            while (currentCoroutineContext().isActive &&
                !pausado &&
                !saltar &&
                System.currentTimeMillis() < fin
            ) {
                delay(200)
            }

            cancelarAlarmaActual()

            if (saltar) {
                faseSaltada = true
                return System.currentTimeMillis()
            }

            if (pausado) {
                restante = (fin - System.currentTimeMillis()).coerceAtLeast(0L)
                continue
            }

            return fin
        }
        return 0L
    }

    private fun calcularSiguiente(): Fase = if (fase == Fase.ENFOQUE) {
        if (bloque >= ajustes.ciclosAntesDescansoLargo) {
            Fase.DESCANSO_LARGO
        } else {
            Fase.DESCANSO_CORTO
        }
    } else {
        Fase.ENFOQUE
    }

    private fun avanzarA(siguiente: Fase) {
        when {
            fase == Fase.ENFOQUE -> fase = siguiente
            fase == Fase.DESCANSO_LARGO -> {
                fase = Fase.ENFOQUE
                bloque = 1
            }
            else -> {
                fase = Fase.ENFOQUE
                bloque += 1
            }
        }
    }

    private fun duracionFaseMs(): Long = when (fase) {
        Fase.ENFOQUE -> ajustes.minutosEnfoque
        Fase.DESCANSO_CORTO -> ajustes.minutosDescansoCorto
        Fase.DESCANSO_LARGO -> ajustes.minutosDescansoLargo
    } * 60_000L

    private fun programarAlarmaActual(fin: Long) {
        PomodoroAlarmas.programar(
            context = this,
            sesionId = sesionId,
            fase = fase,
            finEn = fin,
            token = token(fase, fin),
            titulo = titulo,
            tareaId = tareaId,
        )
    }

    private fun cancelarAlarmaActual() {
        if (sesionId.isNotEmpty()) {
            PomodoroAlarmas.cancelar(this, sesionId, fase)
        }
    }

    private fun guardarSesion(fin: Long) {
        PomodoroSesiones.guardarActiva(
            this,
            PomodoroSesiones.Sesion(
                id = sesionId,
                titulo = titulo,
                tareaId = tareaId,
                fase = fase,
                finEn = fin,
                bloque = bloque,
            ),
        )
    }

    private fun actualizarNotificacionEnCurso(fin: Long) {
        startForeground(
            Notificaciones.ID_POMODORO_EN_CURSO,
            Notificaciones.pomodoroEnCurso(
                context = this,
                sesionId = sesionId,
                titulo = titulo,
                fase = fase,
                finEn = fin,
                bloque = bloque,
                ciclos = ajustes.ciclosAntesDescansoLargo,
            ),
        )
    }

    private fun mostrarPausa() {
        Notificaciones.actualizarPomodoroEnCurso(
            this,
            Notificaciones.pomodoroEnPausa(
                context = this,
                sesionId = sesionId,
                titulo = titulo,
                fase = fase,
                finEn = finActual,
            ),
        )
    }

    private fun mostrarEspera(siguiente: Fase) {
        val minutos = when (siguiente) {
            Fase.ENFOQUE -> ajustes.minutosEnfoque
            Fase.DESCANSO_CORTO -> ajustes.minutosDescansoCorto
            Fase.DESCANSO_LARGO -> ajustes.minutosDescansoLargo
        }
        startForeground(
            Notificaciones.ID_POMODORO_EN_CURSO,
            Notificaciones.pomodoroEnEspera(
                context = this,
                sesionId = sesionId,
                titulo = titulo,
                siguiente = siguiente,
                minutos = minutos,
            ),
        )
    }

    private fun detener() {
        pausado = false
        saltar = false
        esperaContinuar?.complete(Unit)
        esperaContinuar = null
        trabajo?.cancel()
        trabajo = null

        // Si el servicio se recreó solo para atender "Detener", los campos
        // pueden estar vacíos; se recupera la sesión persistida.
        val sesionGuardada = PomodoroSesiones.activa(this)
        val id = sesionId.ifEmpty { sesionGuardada?.id.orEmpty() }
        val faseParaCancelar = if (sesionId.isEmpty() && sesionGuardada != null) {
            sesionGuardada.fase
        } else {
            fase
        }
        val tarea = if (tareaId > 0L) tareaId else sesionGuardada?.tareaId ?: 0L

        if (id.isNotEmpty()) {
            PomodoroAlarmas.cancelar(this, id, faseParaCancelar)
            PomodoroSesiones.limpiar(this)
        }

        if (tarea > 0L) {
            CoroutineScope(Dispatchers.IO).launch {
                runCatching {
                    (application as? EnfoqueApplication)
                        ?.contenedor
                        ?.tareas
                        ?.reiniciarSiEnProgreso(tarea)
                }
            }
        }

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        activo = false
        alcance.cancel()
        super.onDestroy()
    }

    private fun token(fase: Fase, fin: Long): String =
        "$sesionId:${fase.name}:$fin"

    companion object {
        @Volatile
        var activo: Boolean = false
            private set

        fun intentIniciar(
            context: Context,
            sesionId: String,
            tareaId: Long,
            titulo: String,
            ajustes: AjustesPomodoro,
        ): Intent = Intent(context, PomodoroService::class.java).apply {
            action = PomodoroContrato.ACCION_INICIAR
            putExtra(PomodoroContrato.EXTRA_SESION_ID, sesionId)
            putExtra(PomodoroContrato.EXTRA_TAREA_ID, tareaId)
            putExtra(PomodoroContrato.EXTRA_TITULO, titulo)
            putExtra(PomodoroContrato.EXTRA_ENFOQUE, ajustes.minutosEnfoque)
            putExtra(PomodoroContrato.EXTRA_CORTO, ajustes.minutosDescansoCorto)
            putExtra(PomodoroContrato.EXTRA_LARGO, ajustes.minutosDescansoLargo)
            putExtra(PomodoroContrato.EXTRA_CICLOS, ajustes.ciclosAntesDescansoLargo)
            putExtra(PomodoroContrato.EXTRA_AUTO_DESCANSOS, ajustes.autoIniciarDescansos)
            putExtra(PomodoroContrato.EXTRA_AUTO_ENFOQUE, ajustes.autoIniciarEnfoque)
        }
    }
}
