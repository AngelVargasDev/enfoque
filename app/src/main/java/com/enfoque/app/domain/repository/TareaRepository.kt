package com.enfoque.app.domain.repository

import com.enfoque.app.domain.model.CategoriaTarea
import com.enfoque.app.domain.model.EstadoTarea
import com.enfoque.app.domain.model.Tarea
import com.enfoque.app.domain.model.TareaRecurrente
import kotlinx.coroutines.flow.Flow

/**
 * Contrato de acceso a tareas para la capa de presentación.
 *
 * La UI y los ViewModels solo conocen esta interfaz. Hoy la implementa Room
 * ([com.enfoque.app.data.repository.TareaRepositoryImpl]); mañana puede existir una
 * implementación que combine Room con Google Calendar sin tocar los paneles.
 */
interface TareaRepository {

    fun observarTareas(): Flow<List<Tarea>>

    fun observarRecurrentes(): Flow<List<TareaRecurrente>>

    suspend fun crear(tarea: Tarea): Long

    suspend fun actualizar(tarea: Tarea)

    /**
     * Mueve una tarea entre paneles y reancla su fecha de vencimiento:
     * [CategoriaTarea.HOY] -> hoy, [CategoriaTarea.MANANA] -> mañana,
     * [CategoriaTarea.BACKLOG] -> conserva la fecha actual.
     */
    suspend fun moverA(id: Long, categoria: CategoriaTarea)

    suspend fun cambiarEstado(id: Long, estado: EstadoTarea)

    /** Devuelve a PENDIENTE una tarea que quedó EN_PROGRESO al terminar su temporizador. */
    suspend fun reiniciarSiEnProgreso(id: Long)

    suspend fun reiniciarTareasEnProgreso()

    suspend fun eliminar(id: Long)

    /** Reagenda los recordatorios pendientes (se usa tras reiniciar el dispositivo). */
    suspend fun reprogramarRecordatorios()

    suspend fun crearRecurrente(rutina: TareaRecurrente): Long

    suspend fun eliminarRecurrente(id: Long)

    /**
     * Añade o quita la rutina de un panel. Devuelve `true` si se añadió una tarea
     * nueva y `false` si se retiró.
     */
    suspend fun alternarRecurrenteEn(rutina: TareaRecurrente, categoria: CategoriaTarea): Boolean

    /** Promueve a Hoy las tareas de Mañana cuya fecha ya llegó o pasó. */
    suspend fun normalizarFechas()
}
