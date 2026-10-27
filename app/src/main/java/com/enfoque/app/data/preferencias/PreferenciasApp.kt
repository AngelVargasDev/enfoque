package com.enfoque.app.data.preferencias

import android.content.Context
import com.enfoque.app.domain.model.AjustesApp
import com.enfoque.app.domain.model.AjustesPomodoro
import com.enfoque.app.domain.model.ModoDiseno
import com.enfoque.app.domain.model.TemaApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Preferencias de diseño y Pomodoro. Se guardan en SharedPreferences y se
 * exponen como StateFlow para que el tema y la UI reaccionen al instante.
 */
class PreferenciasApp(context: Context) {

    private val prefs = context.getSharedPreferences(NOMBRE, Context.MODE_PRIVATE)
    private val _ajustes = MutableStateFlow(cargar())
    val ajustes: StateFlow<AjustesApp> = _ajustes.asStateFlow()

    @Synchronized
    fun actualizar(transform: (AjustesApp) -> AjustesApp) {
        val nuevos = transform(_ajustes.value)
        _ajustes.value = nuevos
        guardar(nuevos)
    }

    fun definirModoDiseno(modo: ModoDiseno) = actualizar { it.copy(modoDiseno = modo) }

    fun definirTema(tema: TemaApp) = actualizar { it.copy(tema = tema) }

    fun actualizarPomodoro(transform: (AjustesPomodoro) -> AjustesPomodoro) =
        actualizar { it.copy(pomodoro = transform(it.pomodoro).normalizado()) }

    private fun cargar(): AjustesApp = AjustesApp(
        modoDiseno = leerEnum(CLAVE_DISENO, ModoDiseno.MINIMALISTA),
        tema = leerEnum(CLAVE_TEMA, TemaApp.SISTEMA),
        pomodoro = AjustesPomodoro(
            minutosEnfoque = prefs.getInt(CLAVE_ENFOQUE, 25),
            minutosDescansoCorto = prefs.getInt(CLAVE_CORTO, 5),
            minutosDescansoLargo = prefs.getInt(CLAVE_LARGO, 15),
            ciclosAntesDescansoLargo = prefs.getInt(CLAVE_CICLOS, 4),
            autoIniciarDescansos = prefs.getBoolean(CLAVE_AUTO_DESCANSOS, true),
            autoIniciarEnfoque = prefs.getBoolean(CLAVE_AUTO_ENFOQUE, false),
        ).normalizado(),
    )

    private fun guardar(ajustes: AjustesApp) {
        prefs.edit()
            .putString(CLAVE_DISENO, ajustes.modoDiseno.name)
            .putString(CLAVE_TEMA, ajustes.tema.name)
            .putInt(CLAVE_ENFOQUE, ajustes.pomodoro.minutosEnfoque)
            .putInt(CLAVE_CORTO, ajustes.pomodoro.minutosDescansoCorto)
            .putInt(CLAVE_LARGO, ajustes.pomodoro.minutosDescansoLargo)
            .putInt(CLAVE_CICLOS, ajustes.pomodoro.ciclosAntesDescansoLargo)
            .putBoolean(CLAVE_AUTO_DESCANSOS, ajustes.pomodoro.autoIniciarDescansos)
            .putBoolean(CLAVE_AUTO_ENFOQUE, ajustes.pomodoro.autoIniciarEnfoque)
            .apply()
    }

    private inline fun <reified T : Enum<T>> leerEnum(clave: String, defecto: T): T =
        prefs.getString(clave, null)?.let { nombre ->
            runCatching { enumValueOf<T>(nombre) }.getOrNull()
        } ?: defecto

    private companion object {
        const val NOMBRE = "enfoque_ajustes"
        const val CLAVE_DISENO = "modo_diseno"
        const val CLAVE_TEMA = "tema"
        const val CLAVE_ENFOQUE = "pomodoro_enfoque"
        const val CLAVE_CORTO = "pomodoro_corto"
        const val CLAVE_LARGO = "pomodoro_largo"
        const val CLAVE_CICLOS = "pomodoro_ciclos"
        const val CLAVE_AUTO_DESCANSOS = "pomodoro_auto_descansos"
        const val CLAVE_AUTO_ENFOQUE = "pomodoro_auto_enfoque"
    }
}
