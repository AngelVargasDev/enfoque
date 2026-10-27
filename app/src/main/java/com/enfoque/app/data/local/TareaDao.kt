package com.enfoque.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.enfoque.app.domain.model.CategoriaTarea
import com.enfoque.app.domain.model.EstadoTarea
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TareaDao {

    /** Las pendientes primero; dentro de cada grupo, por vencimiento y creación. */
    @Query(
        """
        SELECT * FROM tareas
        ORDER BY CASE WHEN estado = 'COMPLETADA' THEN 1 ELSE 0 END,
                 fechaVencimiento ASC,
                 creadaEn ASC
        """,
    )
    fun observarTodas(): Flow<List<TareaEntity>>

    @Query("SELECT * FROM tareas WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Long): TareaEntity?

    @Query("SELECT * FROM tareas WHERE estado != 'COMPLETADA'")
    suspend fun obtenerPendientes(): List<TareaEntity>

    /** La tarea generada por una rutina en una fecha concreta, si existe. */
    @Query(
        """
        SELECT * FROM tareas
        WHERE recurrenteId = :recurrenteId AND fechaVencimiento = :fecha
        LIMIT 1
        """,
    )
    suspend fun obtenerPorRecurrenteYFecha(recurrenteId: Long, fecha: LocalDate): TareaEntity?

    @Insert
    suspend fun insertar(tarea: TareaEntity): Long

    @Update
    suspend fun actualizar(tarea: TareaEntity)

    @Query("UPDATE tareas SET categoria = :categoria, fechaVencimiento = :fechaVencimiento WHERE id = :id")
    suspend fun actualizarCategoria(id: Long, categoria: CategoriaTarea, fechaVencimiento: LocalDate)

    @Query("UPDATE tareas SET estado = :estado WHERE id = :id")
    suspend fun actualizarEstado(id: Long, estado: EstadoTarea)

    /**
     * Al cruzar la medianoche, las tareas preparadas para "mañana" pasan a "hoy".
     * También recupera las que quedaron vencidas de días anteriores.
     */
    @Query("UPDATE tareas SET categoria = 'HOY' WHERE categoria = 'MANANA' AND fechaVencimiento <= :hoy")
    suspend fun promoverMananaVencida(hoy: LocalDate)

    /** Al terminar el temporizador, solo revierte si la tarea sigue en progreso. */
    @Query("UPDATE tareas SET estado = 'PENDIENTE' WHERE id = :id AND estado = 'EN_PROGRESO'")
    suspend fun reiniciarSiEnProgreso(id: Long)

    /** Limpia estados EN_PROGRESO huérfanos al iniciar un temporizador nuevo. */
    @Query("UPDATE tareas SET estado = 'PENDIENTE' WHERE estado = 'EN_PROGRESO'")
    suspend fun reiniciarTodasEnProgreso()

    @Query("DELETE FROM tareas WHERE id = :id")
    suspend fun eliminar(id: Long)

    /** Al borrar una rutina, sus tareas ya generadas se conservan como tareas normales. */
    @Query("UPDATE tareas SET recurrenteId = NULL WHERE recurrenteId = :recurrenteId")
    suspend fun desvincularRecurrente(recurrenteId: Long)
}
