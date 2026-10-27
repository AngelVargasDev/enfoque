package com.enfoque.app.domain.model

/** Paletas de diseño disponibles en Configuración. */
enum class ModoDiseno(val etiqueta: String) {
    MINIMALISTA("Minimalista"),
    BOSQUE("Bosque"),
    OCEANO("Océano"),
    ATARDECER("Atardecer"),
    MONOCROMO("Monocromo"),
}

/** Preferencia de tema claro/oscuro/sistema. */
enum class TemaApp(val etiqueta: String) {
    SISTEMA("Sistema"),
    CLARO("Claro"),
    OSCURO("Oscuro"),
}

/** Tiempos y comportamiento de una sesión Pomodoro. */
data class AjustesPomodoro(
    val minutosEnfoque: Int = 25,
    val minutosDescansoCorto: Int = 5,
    val minutosDescansoLargo: Int = 15,
    val ciclosAntesDescansoLargo: Int = 4,
    val autoIniciarDescansos: Boolean = true,
    val autoIniciarEnfoque: Boolean = false,
) {
    /** Evita valores fuera de rango si se editan a mano. */
    fun normalizado(): AjustesPomodoro = copy(
        minutosEnfoque = minutosEnfoque.coerceIn(1, 120),
        minutosDescansoCorto = minutosDescansoCorto.coerceIn(1, 60),
        minutosDescansoLargo = minutosDescansoLargo.coerceIn(1, 90),
        ciclosAntesDescansoLargo = ciclosAntesDescansoLargo.coerceIn(2, 8),
    )
}

/** Preferencias globales de la app. */
data class AjustesApp(
    val modoDiseno: ModoDiseno = ModoDiseno.MINIMALISTA,
    val tema: TemaApp = TemaApp.SISTEMA,
    val pomodoro: AjustesPomodoro = AjustesPomodoro(),
)
