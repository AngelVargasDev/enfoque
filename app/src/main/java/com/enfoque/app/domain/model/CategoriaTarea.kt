package com.enfoque.app.domain.model

/**
 * Clasificación temporal que usan los dos paneles de la pantalla principal.
 *
 * - [HOY]: forma parte del Enfoque Diario.
 * - [MANANA]: tareas preparadas para el día siguiente.
 * - [BACKLOG]: tareas generales sin una fecha inmediata.
 */
enum class CategoriaTarea {
    HOY,
    MANANA,
    BACKLOG,
}
