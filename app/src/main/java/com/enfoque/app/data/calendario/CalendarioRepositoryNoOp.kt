package com.enfoque.app.data.calendario

import com.enfoque.app.domain.calendario.CalendarioRepository
import com.enfoque.app.domain.calendario.EventoCalendario
import com.enfoque.app.domain.model.Tarea
import java.time.LocalDate

/**
 * Implementación vacía hasta que se conecte la API de Google Calendar.
 * Mantener el contrato desde la fase 1 evita reescribir la UI en la fase 2.
 */
class CalendarioRepositoryNoOp : CalendarioRepository {

    override suspend fun importarEventos(desde: LocalDate, hasta: LocalDate): List<EventoCalendario> =
        emptyList()

    override suspend fun publicarTarea(tarea: Tarea): String? = null

    override suspend fun eliminarEvento(idEventoExterno: String) = Unit
}
