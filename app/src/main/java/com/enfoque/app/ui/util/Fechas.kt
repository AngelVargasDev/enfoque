package com.enfoque.app.ui.util

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val localeEspanol: Locale = Locale.forLanguageTag("es-ES")

private val formatoLargo = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", localeEspanol)
private val formatoCorto = DateTimeFormatter.ofPattern("d MMM", localeEspanol)
private val formatoHora = DateTimeFormatter.ofPattern("HH:mm", localeEspanol)

fun fechaLarga(fecha: LocalDate): String =
    fecha.format(formatoLargo).replaceFirstChar { caracter ->
        if (caracter.isLowerCase()) caracter.titlecase(localeEspanol) else caracter.toString()
    }

fun horaCorta(hora: LocalTime): String = hora.format(formatoHora)

/** Etiqueta humana para el chip de vencimiento. */
fun etiquetaFecha(fecha: LocalDate, hoy: LocalDate = LocalDate.now()): String = when (fecha) {
    hoy -> "Hoy"
    hoy.plusDays(1) -> "Mañana"
    hoy.minusDays(1) -> "Ayer"
    else -> fecha.format(formatoCorto)
}
