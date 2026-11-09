package com.enfoque.app.ui.peso

import com.enfoque.app.domain.model.Peso
import java.time.LocalDate

/** Estado de la sección de peso; la lista llega ordenada de antiguo a reciente. */
data class PesoUiState(
    val cargando: Boolean = true,
    val pesos: List<Peso> = emptyList(),
    val metaKg: Double? = null,
    val hoy: LocalDate = LocalDate.now(),
) {
    val actual: Peso?
        get() = pesos.lastOrNull()

    val anterior: Peso?
        get() = pesos.dropLast(1).lastOrNull()

    val primerPeso: Peso?
        get() = pesos.firstOrNull()

    val cambioDesdeAnterior: Double?
        get() {
            val actual = actual ?: return null
            val anterior = anterior ?: return null
            return actual.pesoKg - anterior.pesoKg
        }

    /** Cambio desde el primer registro hasta hoy. */
    val cambioTotal: Double?
        get() {
            val actual = actual ?: return null
            val primero = primerPeso ?: return null
            if (primero.fecha == actual.fecha) return null
            return actual.pesoKg - primero.pesoKg
        }

    /** Cambio frente al último registro de hace al menos [dias] días. */
    fun cambioDesde(dias: Int): Double? {
        val actual = actual ?: return null
        val referencia = pesos.lastOrNull {
            !it.fecha.isAfter(hoy.minusDays(dias.toLong()))
        } ?: return null
        if (referencia.fecha == actual.fecha) return null
        return actual.pesoKg - referencia.pesoKg
    }

    val promedioSemana: Double?
        get() {
            val desde = hoy.minusDays(6)
            val valores = pesos.filter { !it.fecha.isBefore(desde) }.map { it.pesoKg }
            return if (valores.isEmpty()) null else valores.average()
        }

    /** Progreso de 0 a 1 desde el primer registro hasta la meta. */
    val progresoMeta: Float?
        get() {
            val meta = metaKg ?: return null
            val inicio = primerPeso?.pesoKg ?: return null
            val actual = actual?.pesoKg ?: return null
            if (inicio <= meta) return if (actual <= meta) 1f else 0f
            val total = inicio - meta
            if (total <= 0.0) return 1f
            return ((inicio - actual) / total).toFloat().coerceIn(0f, 1f)
        }

    /** Positivo si todavía faltan kilos para la meta; negativo si ya se superó. */
    val distanciaMeta: Double?
        get() {
            val meta = metaKg ?: return null
            val actual = actual?.pesoKg ?: return null
            return actual - meta
        }

    fun pesosEnRango(dias: Int?): List<Peso> {
        if (dias == null) return pesos
        val desde = hoy.minusDays(dias.toLong() - 1)
        return pesos.filter { !it.fecha.isBefore(desde) }
    }
}
