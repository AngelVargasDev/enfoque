package com.enfoque.app.domain.calendario

import com.enfoque.app.domain.model.Tarea
import java.time.LocalDate

/**
 * Punto de extensión para la integración futura con Google Calendar.
 *
 * En una siguiente iteración se crea una implementación basada en la Calendar API
 * (o en el Calendar Provider local) y se inyecta en [com.enfoque.app.di.ContenedorApp].
 * Los paneles no cambian: siguen observando [com.enfoque.app.domain.repository.TareaRepository],
 * y el repositorio compuesto decide si una tarea proviene de Room, del calendario o de ambos.
 */
interface CalendarioRepository {

    suspend fun importarEventos(desde: LocalDate, hasta: LocalDate): List<EventoCalendario>

    /** Devuelve el identificador externo del evento creado, si existe. */
    suspend fun publicarTarea(tarea: Tarea): String?

    suspend fun eliminarEvento(idEventoExterno: String)
}

data class EventoCalendario(
    val idExterno: String,
    val titulo: String,
    val descripcion: String = "",
    val fecha: LocalDate,
    val duracionMinutos: Int? = null,
)
