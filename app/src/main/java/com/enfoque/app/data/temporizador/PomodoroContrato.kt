package com.enfoque.app.data.temporizador

/** Acciones, extras y fases compartidos por el servicio Pomodoro y sus alarmas. */
object PomodoroContrato {

    const val ACCION_INICIAR = "com.enfoque.app.action.POMODORO_INICIAR"
    const val ACCION_PAUSA = "com.enfoque.app.action.POMODORO_PAUSA"
    const val ACCION_REANUDAR = "com.enfoque.app.action.POMODORO_REANUDAR"
    const val ACCION_SALTAR = "com.enfoque.app.action.POMODORO_SALTAR"
    const val ACCION_DETENER = "com.enfoque.app.action.POMODORO_DETENER"
    const val ACCION_CONTINUAR = "com.enfoque.app.action.POMODORO_CONTINUAR"
    const val ACCION_FIN = "com.enfoque.app.action.POMODORO_FIN"

    const val EXTRA_SESION_ID = "sesion_id"
    const val EXTRA_TAREA_ID = "tarea_id"
    const val EXTRA_TITULO = "titulo"
    const val EXTRA_FASE = "fase"
    const val EXTRA_BLOQUE = "bloque"
    const val EXTRA_TOKEN = "token"
    const val EXTRA_FIN_EN = "fin_en"
    const val EXTRA_ENFOQUE = "minutos_enfoque"
    const val EXTRA_CORTO = "minutos_corto"
    const val EXTRA_LARGO = "minutos_largo"
    const val EXTRA_CICLOS = "ciclos"
    const val EXTRA_AUTO_DESCANSOS = "auto_descansos"
    const val EXTRA_AUTO_ENFOQUE = "auto_enfoque"

    enum class Fase(val etiqueta: String) {
        ENFOQUE("Enfoque"),
        DESCANSO_CORTO("Descanso corto"),
        DESCANSO_LARGO("Descanso largo"),
    }
}
