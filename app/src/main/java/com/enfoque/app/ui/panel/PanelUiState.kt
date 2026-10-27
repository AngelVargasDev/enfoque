package com.enfoque.app.ui.panel

import com.enfoque.app.domain.model.Racha
import com.enfoque.app.domain.model.Tarea
import com.enfoque.app.domain.model.TareaRecurrente
import java.time.LocalDate

/** Una rutina con el estado de selección para hoy y mañana. */
data class RutinaUi(
    val rutina: TareaRecurrente,
    val enHoy: Boolean,
    val enManana: Boolean,
)

/** Estado inmutable que consume el panel principal. */
data class PanelUiState(
    val cargando: Boolean = true,
    val hoy: LocalDate = LocalDate.now(),
    val enfoqueDiario: List<Tarea> = emptyList(),
    val completadasHoy: List<Tarea> = emptyList(),
    val manana: List<Tarea> = emptyList(),
    val backlog: List<Tarea> = emptyList(),
    val rutinas: List<RutinaUi> = emptyList(),
    /** Tareas pendientes que tienen un recordatorio configurado. */
    val recordatoriosActivos: Int = 0,
    val racha: Racha = Racha(),
) {
    val totalHoy: Int
        get() = enfoqueDiario.size + completadasHoy.size

    val progresoHoy: Float
        get() = if (totalHoy == 0) 0f else completadasHoy.size.toFloat() / totalHoy
}
