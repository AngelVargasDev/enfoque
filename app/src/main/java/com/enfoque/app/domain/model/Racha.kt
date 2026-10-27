package com.enfoque.app.domain.model

/** Estado de la racha de días con al menos una tarea completada. */
data class Racha(
    val actual: Int = 0,
    val mejor: Int = 0,
    val hoyCompletado: Boolean = false,
)
