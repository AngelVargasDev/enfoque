package com.enfoque.app.ui.peso

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.enfoque.app.data.preferencias.PreferenciasApp
import com.enfoque.app.di.ContenedorApp
import com.enfoque.app.domain.model.Peso
import com.enfoque.app.domain.repository.PesoRepository
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

class PesoViewModel(
    private val pesos: PesoRepository,
    private val preferencias: PreferenciasApp,
) : ViewModel() {

    private val _eventos = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val eventos: SharedFlow<String> = _eventos.asSharedFlow()

    private val diaActual = MutableStateFlow(LocalDate.now())

    val estado: StateFlow<PesoUiState> = combine(
        pesos.observarPesos(),
        preferencias.ajustes,
        diaActual,
    ) { lista, ajustes, dia ->
        PesoUiState(
            cargando = false,
            pesos = lista,
            metaKg = ajustes.metaPesoKg,
            hoy = dia,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = PesoUiState(),
    )

    init {
        viewModelScope.launch {
            while (isActive) {
                val ahora = LocalDateTime.now()
                diaActual.value = ahora.toLocalDate()
                val siguienteMedianoche = ahora.toLocalDate().plusDays(1).atStartOfDay()
                val espera = Duration.between(ahora, siguienteMedianoche).toMillis()
                delay(espera.coerceAtLeast(1_000L) + 500L)
            }
        }
    }

    fun refrescarDia() {
        diaActual.value = LocalDate.now()
    }

    fun guardar(peso: Peso) {
        viewModelScope.launch {
            pesos.guardar(peso)
            _eventos.tryEmit("Peso guardado")
        }
    }

    fun eliminar(fecha: LocalDate) {
        viewModelScope.launch {
            pesos.eliminar(fecha)
            _eventos.tryEmit("Registro eliminado")
        }
    }

    fun definirMeta(metaKg: Double?) {
        preferencias.definirMetaPeso(metaKg)
        _eventos.tryEmit(if (metaKg == null) "Meta eliminada" else "Meta actualizada")
    }

    companion object {
        fun fabrica(contenedor: ContenedorApp) = viewModelFactory {
            initializer {
                PesoViewModel(
                    pesos = contenedor.pesos,
                    preferencias = contenedor.preferencias,
                )
            }
        }
    }
}
