package com.enfoque.app.ui.panel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.enfoque.app.di.ContenedorApp
import com.enfoque.app.domain.model.CategoriaTarea
import com.enfoque.app.domain.model.EstadoTarea
import com.enfoque.app.domain.model.Tarea
import com.enfoque.app.domain.model.TareaRecurrente
import com.enfoque.app.domain.repository.TareaRepository
import com.enfoque.app.domain.temporizador.Temporizador
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * ViewModel del panel principal. Solo habla con contratos de dominio, por lo que
 * Room, AlarmManager y una futura sincronización con Google Calendar quedan
 * aislados de la UI.
 */
class PanelViewModel(
    private val tareas: TareaRepository,
    private val temporizador: Temporizador,
) : ViewModel() {

    private val _eventos = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val eventos: SharedFlow<String> = _eventos.asSharedFlow()

    private val diaActual = MutableStateFlow(LocalDate.now())

    val estado: StateFlow<PanelUiState> = combine(
        tareas.observarTareas(),
        tareas.observarRecurrentes(),
        diaActual,
    ) { lista, rutinas, dia ->
        construirEstado(lista, rutinas, dia)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = PanelUiState(),
    )

    init {
        // Mantiene el tablero al día si la app queda abierta pasada la medianoche
        // y promueve a Hoy las tareas preparadas para el día anterior.
        viewModelScope.launch {
            while (isActive) {
                val ahora = LocalDateTime.now()
                diaActual.value = ahora.toLocalDate()
                tareas.normalizarFechas()

                val siguienteMedianoche = ahora.toLocalDate().plusDays(1).atStartOfDay()
                val espera = Duration.between(ahora, siguienteMedianoche).toMillis()
                delay(espera.coerceAtLeast(1_000L) + 500L)
            }
        }
    }

    /** Se llama al volver a la app para recuperar el día real tras un segundo plano largo. */
    fun refrescarDia() {
        diaActual.value = LocalDate.now()
        viewModelScope.launch { tareas.normalizarFechas() }
    }

    /** Reaplica los recordatorios; útil al conceder el permiso de alarmas exactas. */
    fun reprogramarRecordatorios() {
        viewModelScope.launch { tareas.reprogramarRecordatorios() }
    }

    fun agregarTarea(tarea: Tarea) {
        viewModelScope.launch {
            tareas.crear(tarea)
            _eventos.tryEmit("Tarea agregada")
        }
    }

    fun moverTarea(tarea: Tarea, categoria: CategoriaTarea) {
        viewModelScope.launch {
            tareas.moverA(tarea.id, categoria)
            val mensaje = when (categoria) {
                CategoriaTarea.HOY -> "Movida al enfoque de hoy"
                CategoriaTarea.MANANA -> "Programada para mañana"
                CategoriaTarea.BACKLOG -> "Enviada al backlog"
            }
            _eventos.tryEmit(mensaje)
        }
    }

    fun alternarCompletada(tarea: Tarea) {
        val nuevoEstado = if (tarea.estaCompletada) EstadoTarea.PENDIENTE else EstadoTarea.COMPLETADA
        viewModelScope.launch {
            tareas.cambiarEstado(tarea.id, nuevoEstado)
            _eventos.tryEmit(if (nuevoEstado == EstadoTarea.COMPLETADA) "Tarea completada" else "Tarea reabierta")
        }
    }

    fun eliminarTarea(tarea: Tarea) {
        viewModelScope.launch {
            tareas.eliminar(tarea.id)
            _eventos.tryEmit("Tarea eliminada")
        }
    }

    fun agregarRutina(rutina: TareaRecurrente) {
        viewModelScope.launch {
            tareas.crearRecurrente(rutina)
            _eventos.tryEmit("Rutina agregada")
        }
    }

    fun eliminarRutina(rutina: TareaRecurrente) {
        viewModelScope.launch {
            tareas.eliminarRecurrente(rutina.id)
            _eventos.tryEmit("Rutina eliminada")
        }
    }

    fun alternarRutina(rutina: TareaRecurrente, categoria: CategoriaTarea) {
        viewModelScope.launch {
            val anadida = tareas.alternarRecurrenteEn(rutina, categoria)
            val cuando = if (categoria == CategoriaTarea.MANANA) "mañana" else "hoy"
            _eventos.tryEmit(
                if (anadida) {
                    "«${rutina.titulo}» añadida a $cuando"
                } else {
                    "«${rutina.titulo}» retirada de $cuando"
                },
            )
        }
    }

    fun iniciarTemporizador(tarea: Tarea) {
        if (!temporizador.iniciar(tarea)) {
            _eventos.tryEmit("Asigna una duración válida para iniciar el temporizador")
            return
        }
        if (!tarea.estaCompletada) {
            viewModelScope.launch {
                // Solo hay un temporizador activo: cualquier tarea que hubiera
                // quedado EN_PROGRESO vuelve a PENDIENTE antes de marcar la nueva.
                tareas.reiniciarTareasEnProgreso()
                tareas.cambiarEstado(tarea.id, EstadoTarea.EN_PROGRESO)
            }
        }
        _eventos.tryEmit("Temporizador iniciado: ${tarea.duracionMinutos} min")
    }

    private fun construirEstado(
        lista: List<Tarea>,
        rutinas: List<TareaRecurrente>,
        dia: LocalDate,
    ): PanelUiState {
        val manana = dia.plusDays(1)

        // Una tarea de "Mañana" cuya fecha ya llegó cuenta como de hoy aunque la
        // migración de la base todavía no se haya ejecutado.
        fun correspondeAHoy(tarea: Tarea): Boolean =
            tarea.categoria == CategoriaTarea.HOY ||
                (tarea.categoria == CategoriaTarea.MANANA && !tarea.fechaVencimiento.isAfter(dia))

        val deHoy = lista.filter(::correspondeAHoy)

        return PanelUiState(
            cargando = false,
            hoy = dia,
            enfoqueDiario = deHoy.filterNot { it.estaCompletada },
            completadasHoy = deHoy.filter { it.estaCompletada },
            manana = lista.filter {
                it.categoria == CategoriaTarea.MANANA && it.fechaVencimiento.isAfter(dia)
            },
            backlog = lista.filter { it.categoria == CategoriaTarea.BACKLOG },
            rutinas = rutinas.map { rutina ->
                RutinaUi(
                    rutina = rutina,
                    enHoy = lista.any {
                        it.recurrenteId == rutina.id && it.fechaVencimiento == dia
                    },
                    enManana = lista.any {
                        it.recurrenteId == rutina.id && it.fechaVencimiento == manana
                    },
                )
            },
            recordatoriosActivos = lista.count { !it.estaCompletada && it.horaRecordatorio != null },
        )
    }

    companion object {
        fun fabrica(contenedor: ContenedorApp) = viewModelFactory {
            initializer {
                PanelViewModel(
                    tareas = contenedor.tareas,
                    temporizador = contenedor.temporizador,
                )
            }
        }
    }
}
