package com.enfoque.app.data.temporizador

import android.content.Context
import android.content.SharedPreferences
import com.enfoque.app.data.temporizador.PomodoroContrato.Fase

/**
 * Estado persistente de la sesión Pomodoro. Lo consulta la alarma de respaldo
 * cuando el servicio ya no está vivo.
 */
object PomodoroSesiones {

    private const val PREFS = "pomodoro_sesiones"
    private const val CLAVE_ID = "sesion_id"
    private const val CLAVE_TITULO = "titulo"
    private const val CLAVE_TAREA = "tarea_id"
    private const val CLAVE_FASE = "fase"
    private const val CLAVE_FIN = "fin_en"
    private const val CLAVE_BLOQUE = "bloque"
    private const val CLAVE_ULTIMO_FIN = "ultimo_fin"

    data class Sesion(
        val id: String,
        val titulo: String,
        val tareaId: Long,
        val fase: Fase,
        val finEn: Long,
        val bloque: Int,
    )

    fun activa(context: Context): Sesion? {
        val prefs = prefs(context)
        val id = prefs.getString(CLAVE_ID, null) ?: return null
        val titulo = prefs.getString(CLAVE_TITULO, null) ?: return null
        val fase = prefs.getString(CLAVE_FASE, null)
            ?.let { runCatching { Fase.valueOf(it) }.getOrNull() }
            ?: return null
        return Sesion(
            id = id,
            titulo = titulo,
            tareaId = prefs.getLong(CLAVE_TAREA, 0L),
            fase = fase,
            finEn = prefs.getLong(CLAVE_FIN, 0L),
            bloque = prefs.getInt(CLAVE_BLOQUE, 1),
        )
    }

    fun guardarActiva(context: Context, sesion: Sesion) {
        prefs(context).edit()
            .putString(CLAVE_ID, sesion.id)
            .putString(CLAVE_TITULO, sesion.titulo)
            .putLong(CLAVE_TAREA, sesion.tareaId)
            .putString(CLAVE_FASE, sesion.fase.name)
            .putLong(CLAVE_FIN, sesion.finEn)
            .putInt(CLAVE_BLOQUE, sesion.bloque)
            .apply()
    }

    fun limpiar(context: Context) {
        prefs(context).edit()
            .remove(CLAVE_ID)
            .remove(CLAVE_TITULO)
            .remove(CLAVE_TAREA)
            .remove(CLAVE_FASE)
            .remove(CLAVE_FIN)
            .remove(CLAVE_BLOQUE)
            .apply()
    }

    /** True solo la primera vez que se reclama el fin de una fase concreta. */
    @Synchronized
    fun reclamarFin(context: Context, token: String): Boolean {
        val prefs = prefs(context)
        if (prefs.getString(CLAVE_ULTIMO_FIN, null) == token) return false
        prefs.edit().putString(CLAVE_ULTIMO_FIN, token).apply()
        return true
    }

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
