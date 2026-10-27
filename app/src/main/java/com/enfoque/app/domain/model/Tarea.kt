package com.enfoque.app.domain.model

import java.time.LocalDate
import java.time.LocalTime

/**
 * Modelo de dominio de una tarea. Es independiente de Room y de la UI, de modo que
 * una futura fuente de datos remota (por ejemplo, Google Calendar) pueda producir
 * exactamente el mismo tipo.
 */
data class Tarea(
    val id: Long = 0L,
    val titulo: String,
    val descripcion: String = "",
    val fechaVencimiento: LocalDate,
    val estado: EstadoTarea = EstadoTarea.PENDIENTE,
    val categoria: CategoriaTarea = CategoriaTarea.HOY,
    /** Duración opcional del bloque de enfoque. Si es null, la tarea no tiene temporizador. */
    val duracionMinutos: Int? = null,
    /** Hora del recordatorio local. `null` desactiva el aviso de esta tarea. */
    val horaRecordatorio: LocalTime? = LocalTime.of(9, 0),
    /** Si la tarea nació de una rutina, guarda su identificador. */
    val recurrenteId: Long? = null,
    val creadaEn: Long = System.currentTimeMillis(),
) {
    val estaCompletada: Boolean
        get() = estado == EstadoTarea.COMPLETADA

    /** Una duración válida habilita el botón "Iniciar" y el servicio de temporizador. */
    val tieneTemporizador: Boolean
        get() = duracionMinutos != null && duracionMinutos > 0
}
