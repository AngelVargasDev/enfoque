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
import com.enfoque.app.data.temporizador.PomodoroContrato.Fase
import com.enfoque.app.ui.MainActivity

/** Canales y construcción de todas las notificaciones locales de la app. */
object Notificaciones {

    const val CANAL_TEMPORIZADOR = "temporizador_en_curso"
    const val CANAL_FIN_TEMPORIZADOR = "fin_temporizador"
    const val CANAL_RECORDATORIOS = "recordatorios"
    const val CANAL_POMODORO = "pomodoro"

    const val ID_EN_CURSO = 1001
    private const val ID_FIN = 1002
    private const val ID_RECORDATORIO_BASE = 2000
    const val ID_POMODORO_EN_CURSO = 3001
    private const val ID_POMODORO_FIN = 3002

    fun crearCanales(context: Context) {
        val gerente = context.getSystemService(NotificationManager::class.java) ?: return
        val atributosAlarma = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val atributosAviso = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
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
            setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), atributosAviso)
            enableVibration(true)
        }

        val pomodoro = NotificationChannel(
            CANAL_POMODORO,
            "Pomodoro",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Avisa el final de cada bloque de enfoque y de cada descanso."
            setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), atributosAviso)
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 350, 150, 350)
        }

        gerente.createNotificationChannels(listOf(enCurso, fin, recordatorios, pomodoro))
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

    // ---------------------------------------------------------------------
    // Pomodoro
    // ---------------------------------------------------------------------

    fun pomodoroEnCurso(
        context: Context,
        sesionId: String,
        titulo: String,
        fase: Fase,
        finEn: Long,
        bloque: Int,
        ciclos: Int,
    ): Notification {
        val subtitulo = when (fase) {
            Fase.ENFOQUE -> "Bloque $bloque de $ciclos"
            Fase.DESCANSO_CORTO -> "Descanso corto"
            Fase.DESCANSO_LARGO -> "Descanso largo"
        }
        return NotificationCompat.Builder(context, CANAL_POMODORO)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle("${fase.etiqueta} · $titulo")
            .setContentText(subtitulo)
            .setContentIntent(abrirApp(context))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setWhen(finEn)
            .setUsesChronometer(true)
            .setChronometerCountDown(true)
            .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                android.R.drawable.ic_media_pause,
                "Pausar",
                accionServicio(context, PomodoroContrato.ACCION_PAUSA, sesionId),
            )
            .addAction(
                android.R.drawable.ic_media_next,
                "Saltar",
                accionServicio(context, PomodoroContrato.ACCION_SALTAR, sesionId),
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Detener",
                accionServicio(context, PomodoroContrato.ACCION_DETENER, sesionId),
            )
            .build()
    }

    fun pomodoroEnPausa(
        context: Context,
        sesionId: String,
        titulo: String,
        fase: Fase,
        finEn: Long,
    ): Notification {
        val restante = (finEn - System.currentTimeMillis()).coerceAtLeast(0L)
        return NotificationCompat.Builder(context, CANAL_POMODORO)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle("En pausa · ${fase.etiqueta}")
            .setContentText("Quedan ${formatearRestante(restante)} · $titulo")
            .setContentIntent(abrirApp(context))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                android.R.drawable.ic_media_play,
                "Reanudar",
                accionServicio(context, PomodoroContrato.ACCION_REANUDAR, sesionId),
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Detener",
                accionServicio(context, PomodoroContrato.ACCION_DETENER, sesionId),
            )
            .build()
    }

    fun pomodoroEnEspera(
        context: Context,
        sesionId: String,
        titulo: String,
        siguiente: Fase,
        minutos: Int,
    ): Notification =
        NotificationCompat.Builder(context, CANAL_POMODORO)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle("Listo para continuar")
            .setContentText("Siguiente: ${siguiente.etiqueta} · $minutos min · $titulo")
            .setContentIntent(abrirApp(context))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                android.R.drawable.ic_media_play,
                "Continuar",
                accionServicio(context, PomodoroContrato.ACCION_CONTINUAR, sesionId),
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Detener",
                accionServicio(context, PomodoroContrato.ACCION_DETENER, sesionId),
            )
            .build()

    fun notificarFinFase(
        context: Context,
        sesionId: String,
        fase: Fase,
        titulo: String,
        token: String,
    ) {
        if (!PomodoroSesiones.reclamarFin(context, token)) return
        cancelarPomodoroEnCurso(context)

        val (encabezado, texto) = when (fase) {
            Fase.ENFOQUE -> "Enfoque completado" to
                "Buen trabajo en «$titulo». Es momento de descansar."
            Fase.DESCANSO_CORTO -> "Descanso corto terminado" to
                "Vuelve al enfoque cuando estés listo."
            Fase.DESCANSO_LARGO -> "Descanso largo terminado" to
                "Retoma «$titulo» con calma."
        }

        val notificacion = NotificationCompat.Builder(context, CANAL_POMODORO)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle(encabezado)
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setContentIntent(abrirApp(context))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .build()

        publicar(context, ID_POMODORO_FIN, notificacion)
    }

    fun cancelarPomodoroEnCurso(context: Context) {
        NotificationManagerCompat.from(context).cancel(ID_POMODORO_EN_CURSO)
    }

    /** Actualiza la notificación persistente sin exigir permiso en el llamador. */
    fun actualizarPomodoroEnCurso(context: Context, notificacion: Notification) {
        publicar(context, ID_POMODORO_EN_CURSO, notificacion)
    }

    fun cancelarEnCurso(context: Context) {
        NotificationManagerCompat.from(context).cancel(ID_EN_CURSO)
    }

    @SuppressLint("MissingPermission")
    private fun publicar(context: Context, id: Int, notificacion: Notification) {
        if (!puedeNotificar(context)) return
        NotificationManagerCompat.from(context).notify(id, notificacion)
    }

    private fun accionServicio(context: Context, accion: String, sesionId: String): PendingIntent {
        val intent = Intent(context, PomodoroService::class.java).apply {
            this.action = accion
            putExtra(PomodoroContrato.EXTRA_SESION_ID, sesionId)
        }
        return PendingIntent.getService(
            context,
            "${accion}_$sesionId".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun formatearRestante(millis: Long): String {
        val totalSegundos = millis / 1000
        val minutos = totalSegundos / 60
        val segundos = totalSegundos % 60
        return "%d:%02d".format(minutos, segundos)
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
