package com.enfoque.app.domain.temporizador

import com.enfoque.app.domain.model.AjustesPomodoro
import com.enfoque.app.domain.model.Tarea

/** Abstracción de la ejecución de un bloque de enfoque con duración. */
interface Temporizador {

    /**
     * Arranca el servicio en segundo plano que programa la alarma y notifica al terminar.
     * @return false si la tarea no tiene una duración válida o el sistema rechazó el inicio.
     */
    fun iniciar(tarea: Tarea): Boolean

    /**
     * Arranca una sesión Pomodoro vinculada a la tarea. La duración de la tarea
     * no se usa: mandan los tiempos configurados en [AjustesPomodoro].
     */
    fun iniciarPomodoro(tarea: Tarea, ajustes: AjustesPomodoro): Boolean

    /** True si hay un temporizador simple o una sesión Pomodoro en curso. */
    fun haySesionActiva(): Boolean

    /** Detiene el Pomodoro solo si pertenece a la tarea indicada. */
    fun detenerPomodoroSiEs(tareaId: Long)

    /** Detiene cualquier sesión Pomodoro activa. */
    fun detenerPomodoro()

    /** Detiene el temporizador simple activo, si existe. */
    fun detenerTemporizadorSimple()
}
