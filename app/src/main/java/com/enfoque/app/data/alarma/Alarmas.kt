package com.enfoque.app.data.alarma

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build

/**
 * Envoltorio de AlarmManager. Usa alarmas exactas cuando el usuario lo permite y
 * degrada a una alarma permitida en reposo (menos precisa) si no hay permiso.
 */
object Alarmas {

    /** En Android 12+ el usuario debe autorizar las alarmas exactas. */
    fun puedeProgramarExactas(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        return context.getSystemService(AlarmManager::class.java)
            ?.canScheduleExactAlarms() == true
    }

    fun programar(context: Context, disparoEn: Long, pendingIntent: PendingIntent) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, disparoEn, pendingIntent)
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, disparoEn, pendingIntent)
            }
        } catch (_: SecurityException) {
            // El permiso se revocó entre la comprobación y la llamada.
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, disparoEn, pendingIntent)
        }
    }

    /**
     * Alarma de reloj para el fin de un temporizador. `setAlarmClock` es la forma
     * más precisa de despertar el dispositivo incluso en reposo profundo, por eso
     * se usa en lugar de una alarma exacta normal.
     */
    fun programarReloj(context: Context, disparoEn: Long, pendingIntent: PendingIntent) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, disparoEn, pendingIntent)
            } else {
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(disparoEn, null),
                    pendingIntent,
                )
            }
        } catch (_: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, disparoEn, pendingIntent)
        }
    }

    fun cancelar(context: Context, pendingIntent: PendingIntent) {
        context.getSystemService(AlarmManager::class.java)?.cancel(pendingIntent)
    }
}
