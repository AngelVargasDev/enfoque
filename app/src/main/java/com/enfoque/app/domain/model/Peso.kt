package com.enfoque.app.domain.model

import java.time.LocalDate

/** Registro diario de peso corporal. */
data class Peso(
    val fecha: LocalDate,
    /** Peso en kilogramos. */
    val pesoKg: Double,
    val nota: String = "",
    val creadoEn: Long = System.currentTimeMillis(),
)
