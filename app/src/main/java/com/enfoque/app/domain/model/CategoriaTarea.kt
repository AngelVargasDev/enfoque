package com.enfoque.app.domain.model

/**
 * Clasificación temporal que usan los paneles de la pantalla principal.
 *
 * - [HOY]: forma parte del Enfoque Diario.
 * - [MANANA]: tareas preparadas para el día siguiente.
 * - [BACKLOG]: tareas generales sin una fecha inmediata.
 * - [PROGRAMADA]: tareas con una fecha futura (más allá de mañana).
 */
enum class CategoriaTarea {
    HOY,
    MANANA,
    BACKLOG,
    PROGRAMADA,
}
