package com.enfoque.app.data.repository

import com.enfoque.app.data.local.TareaDao
import com.enfoque.app.data.local.TareaRecurrenteDao
import com.enfoque.app.data.local.aDominio
import com.enfoque.app.data.local.aEntidad
import com.enfoque.app.data.recordatorio.RecordatorioProgramador
import com.enfoque.app.domain.model.CategoriaTarea
import com.enfoque.app.domain.model.EstadoTarea
import com.enfoque.app.domain.model.Tarea
import com.enfoque.app.domain.model.TareaRecurrente
import com.enfoque.app.domain.repository.TareaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.LocalDate

/**
 * Implementación local con Room. Además de persistir, mantiene sincronizadas las
 * alarmas de recordatorio: la UI nunca manipula AlarmManager directamente.
 */
class TareaRepositoryImpl(
    private val dao: TareaDao,
    private val recurrentesDao: TareaRecurrenteDao,
    private val recordatorios: RecordatorioProgramador,
    private val reloj: Clock = Clock.systemDefaultZone(),
) : TareaRepository {

    override fun observarTareas(): Flow<List<Tarea>> =
        dao.observarTodas().map { tareas -> tareas.map { it.aDominio() } }

    override fun observarRecurrentes(): Flow<List<TareaRecurrente>> =
        recurrentesDao.observarTodas().map { rutinas -> rutinas.map { it.aDominio() } }

    override suspend fun crear(tarea: Tarea): Long {
        val id = dao.insertar(tarea.aEntidad())
        recordatorios.programar(tarea.copy(id = id))
        return id
    }

    override suspend fun actualizar(tarea: Tarea) {
        dao.actualizar(tarea.aEntidad())
        recordatorios.programar(tarea)
    }

    override suspend fun moverA(id: Long, categoria: CategoriaTarea) {
        val actual = dao.obtenerPorId(id)?.aDominio() ?: return
        val hoy = LocalDate.now(reloj)
        val nuevaFecha = when (categoria) {
            CategoriaTarea.HOY -> hoy
            CategoriaTarea.MANANA -> hoy.plusDays(1)
            CategoriaTarea.BACKLOG -> actual.fechaVencimiento
        }
        dao.actualizarCategoria(id, categoria, nuevaFecha)
        recordatorios.programar(actual.copy(categoria = categoria, fechaVencimiento = nuevaFecha))
    }

    override suspend fun cambiarEstado(id: Long, estado: EstadoTarea) {
        val actual = dao.obtenerPorId(id)?.aDominio() ?: return
        dao.actualizarEstado(id, estado)
        if (estado == EstadoTarea.COMPLETADA) {
            recordatorios.cancelar(id)
        } else {
            recordatorios.programar(actual.copy(estado = estado))
        }
    }

    override suspend fun reiniciarSiEnProgreso(id: Long) {
        dao.reiniciarSiEnProgreso(id)
    }

    override suspend fun reiniciarTareasEnProgreso() {
        dao.reiniciarTodasEnProgreso()
    }

    override suspend fun eliminar(id: Long) {
        dao.eliminar(id)
        recordatorios.cancelar(id)
    }

    override suspend fun reprogramarRecordatorios() {
        dao.obtenerPendientes().forEach { recordatorios.programar(it.aDominio()) }
    }

    override suspend fun crearRecurrente(rutina: TareaRecurrente): Long =
        recurrentesDao.insertar(rutina.aEntidad())

    override suspend fun eliminarRecurrente(id: Long) {
        recurrentesDao.eliminar(id)
        // Las tareas ya generadas se conservan; solo dejan de estar vinculadas.
        dao.desvincularRecurrente(id)
    }

    override suspend fun alternarRecurrenteEn(
        rutina: TareaRecurrente,
        categoria: CategoriaTarea,
    ): Boolean {
        if (categoria == CategoriaTarea.BACKLOG) return false

        val hoy = LocalDate.now(reloj)
        val fecha = when (categoria) {
            CategoriaTarea.HOY -> hoy
            CategoriaTarea.MANANA -> hoy.plusDays(1)
            CategoriaTarea.BACKLOG -> return false
        }

        val existente = dao.obtenerPorRecurrenteYFecha(rutina.id, fecha)
        if (existente != null) {
            dao.eliminar(existente.id)
            recordatorios.cancelar(existente.id)
            return false
        }

        val tarea = Tarea(
            titulo = rutina.titulo,
            descripcion = rutina.descripcion,
            fechaVencimiento = fecha,
            categoria = categoria,
            duracionMinutos = rutina.duracionMinutos,
            horaRecordatorio = rutina.horaRecordatorio,
            usarPomodoro = rutina.usarPomodoro,
            recurrenteId = rutina.id,
        )
        val id = dao.insertar(tarea.aEntidad())
        recordatorios.programar(tarea.copy(id = id))
        return true
    }

    override suspend fun normalizarFechas() {
        dao.promoverMananaVencida(LocalDate.now(reloj))
    }
}
