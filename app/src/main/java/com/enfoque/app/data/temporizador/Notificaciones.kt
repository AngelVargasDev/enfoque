package com.enfoque.app.data.temporizador

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.enfoque.app.R
import com.enfoque.app.ui.MainActivity

/** Canales y construcción de todas las notificaciones locales de la app. */
object Notificaciones {

    const val CANAL_TEMPORIZADOR = "temporizador_en_curso"
    const val CANAL_FIN_TEMPORIZADOR = "fin_temporizador"
    const val CANAL_RECORDATORIOS = "recordatorios"

    const val ID_EN_CURSO = 1001
    private const val ID_FIN = 1002
    private const val ID_RECORDATORIO_BASE = 2000

    fun crearCanales(context: Context) {
        val gerente = context.getSystemService(NotificationManager::class.java) ?: return
        val atributosAlarma = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val sonidoAlarma = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val enCurso = NotificationChannel(
            CANAL_TEMPORIZADOR,
            "Temporizador en curso",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Muestra el tiempo restante del bloque de enfoque activo."
            setSound(null, null)
            enableVibration(false)
            setShowBadge(false)
        }

        val fin = NotificationChannel(
            CANAL_FIN_TEMPORIZADOR,
            "Fin de temporizador",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Avisa con sonido cuando termina un bloque de enfoque."
            setSound(sonidoAlarma, atributosAlarma)
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 250, 500)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }

        val recordatorios = NotificationChannel(
            CANAL_RECORDATORIOS,
            "Recordatorios de tareas",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Avisa el día de vencimiento de tus tareas."
            setSound(
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
            enableVibration(true)
        }

        gerente.createNotificationChannels(listOf(enCurso, fin, recordatorios))
    }

    fun enCurso(context: Context, titulo: String, finEn: Long): Notification =
        NotificationCompat.Builder(context, CANAL_TEMPORIZADOR)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle(titulo)
            .setContentText("Temporizador en curso")
            .setContentIntent(abrirApp(context))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setWhen(finEn)
            .setUsesChronometer(true)
            .setChronometerCountDown(true)
            .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

    fun notificarFin(context: Context, sesionId: String, titulo: String) {
        if (!SesionesTemporizador.reclamarFin(context, sesionId)) return
        cancelarEnCurso(context)

        val texto = "Terminó el temporizador de «$titulo». Marca la tarea como completada o inicia otro bloque de enfoque."
        val notificacion = NotificationCompat.Builder(context, CANAL_FIN_TEMPORIZADOR)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle("¡Tiempo cumplido!")
            .setContentText("Terminó el temporizador de «$titulo».")
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setContentIntent(abrirApp(context))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .build()

        publicar(context, ID_FIN, notificacion)
    }

    fun notificarRecordatorio(context: Context, tareaId: Long, titulo: String, descripcion: String) {
        val texto = descripcion.ifBlank { "Es el día de vencimiento de esta tarea." }
        val notificacion = NotificationCompat.Builder(context, CANAL_RECORDATORIOS)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle("Recordatorio: $titulo")
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setContentIntent(abrirApp(context))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        publicar(context, ID_RECORDATORIO_BASE + tareaId.toInt(), notificacion)
    }

    fun cancelarEnCurso(context: Context) {
        NotificationManagerCompat.from(context).cancel(ID_EN_CURSO)
    }

    @SuppressLint("MissingPermission")
    private fun publicar(context: Context, id: Int, notificacion: Notification) {
        if (!puedeNotificar(context)) return
        NotificationManagerCompat.from(context).notify(id, notificacion)
    }

    private fun puedeNotificar(context: Context): Boolean {
        val permiso = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        return permiso && NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    private fun abrirApp(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
