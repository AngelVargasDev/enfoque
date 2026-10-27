package com.enfoque.app.domain.model

import java.time.LocalTime

/**
 * Definición de una rutina: una tarea que se repite y que el usuario puede
 * añadir a Hoy o Mañana con un solo toque, sin volver a escribirla.
 *
 * La rutina no es una tarea en sí: es una plantilla. Al seleccionarla se crea
 * una [Tarea] normal en el panel correspondiente.
 */
data class TareaRecurrente(
    val id: Long = 0L,
    val titulo: String,
    val descripcion: String = "",
    /** Duración opcional del bloque de enfoque. */
    val duracionMinutos: Int? = null,
    /** Si las tareas generadas por esta rutina usan Pomodoro. */
    val usarPomodoro: Boolean = false,
    /** Hora del recordatorio. `null` significa que la rutina no avisa. */
    val horaRecordatorio: LocalTime? = LocalTime.of(9, 0),
    val creadaEn: Long = System.currentTimeMillis(),
) {
    val tieneTemporizador: Boolean
        get() = duracionMinutos != null && duracionMinutos > 0
}
