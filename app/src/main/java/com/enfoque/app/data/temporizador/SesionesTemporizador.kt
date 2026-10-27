package com.enfoque.app.data.temporizador

import android.content.Context
import android.content.SharedPreferences

/**
 * Estado persistente de los temporizadores. Existe porque el servicio en primer
 * plano y la alarma de respaldo (BroadcastReceiver) pueden terminar el mismo
 * bloque: así se garantiza que la notificación final se emita una sola vez.
 */
object SesionesTemporizador {

    private const val PREFS = "temporizador_sesiones"
    private const val CLAVE_ACTIVA_ID = "activa_id"
    private const val CLAVE_ACTIVA_TITULO = "activa_titulo"
    private const val CLAVE_ACTIVA_FIN = "activa_fin"
    private const val CLAVE_ULTIMO_FIN = "ultimo_fin"

    data class Sesion(val id: String, val titulo: String, val finEn: Long)

    fun activa(context: Context): Sesion? {
        val prefs = prefs(context)
        val id = prefs.getString(CLAVE_ACTIVA_ID, null) ?: return null
        val titulo = prefs.getString(CLAVE_ACTIVA_TITULO, null) ?: return null
        val finEn = prefs.getLong(CLAVE_ACTIVA_FIN, 0L)
        if (finEn <= 0L) return null
        return Sesion(id, titulo, finEn)
    }

    fun guardarActiva(context: Context, id: String, titulo: String, finEn: Long) {
        prefs(context).edit()
            .putString(CLAVE_ACTIVA_ID, id)
            .putString(CLAVE_ACTIVA_TITULO, titulo)
            .putLong(CLAVE_ACTIVA_FIN, finEn)
            .apply()
    }

    fun limpiarActiva(context: Context) {
        prefs(context).edit()
            .remove(CLAVE_ACTIVA_ID)
            .remove(CLAVE_ACTIVA_TITULO)
            .remove(CLAVE_ACTIVA_FIN)
            .apply()
    }

    /** Devuelve true solo la primera vez que se reclama el fin de una sesión. */
    @Synchronized
    fun reclamarFin(context: Context, sesionId: String): Boolean {
        val prefs = prefs(context)
        if (prefs.getString(CLAVE_ULTIMO_FIN, null) == sesionId) return false
        prefs.edit().putString(CLAVE_ULTIMO_FIN, sesionId).apply()
        return true
    }

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
