package com.enfoque.app.ui.panel

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.enfoque.app.domain.model.CategoriaTarea
import com.enfoque.app.domain.model.Tarea
import com.enfoque.app.ui.util.fechaLarga
import com.enfoque.app.ui.util.horaCorta
import java.time.LocalDate
import java.time.LocalTime

/**
 * Formulario de captura. Al elegir Hoy o Mañana la fecha de vencimiento se ajusta
 * automáticamente, así el panel y la fecha siempre cuentan la misma historia.
 */
@Composable
fun NuevaTareaDialog(
    onCerrar: () -> Unit,
    onGuardar: (Tarea) -> Unit,
) {
    var titulo by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var categoria by rememberSaveable { mutableStateOf(CategoriaTarea.HOY) }
    var fechaEpoch by rememberSaveable { mutableLongStateOf(LocalDate.now().toEpochDay()) }
    var duracionTexto by rememberSaveable { mutableStateOf("") }
    var recordatorioActivo by rememberSaveable { mutableStateOf(true) }
    var horaMinutos by rememberSaveable { mutableIntStateOf(9 * 60) }
    var pomodoro by rememberSaveable { mutableStateOf(false) }

    val contexto = LocalContext.current
    val fecha = LocalDate.ofEpochDay(fechaEpoch)
    val duracion = duracionTexto.trim().toIntOrNull()
    val duracionValida = duracionTexto.isBlank() || (duracion != null && duracion in 1..720)
    val puedeGuardar = titulo.isNotBlank() && (pomodoro || duracionValida)

    val abrirSelectorFecha = {
        DatePickerDialog(
            contexto,
            { _, anio, mes, dia ->
                val elegida = LocalDate.of(anio, mes + 1, dia)
                fechaEpoch = elegida.toEpochDay()
                categoria = categoriaParaFecha(elegida)
            },
            fecha.year,
            fecha.monthValue - 1,
            fecha.dayOfMonth,
        ).show()
    }

    val abrirSelectorHora = {
        val hora = LocalTime.of(horaMinutos / 60, horaMinutos % 60)
        TimePickerDialog(
            contexto,
            { _, h, m -> horaMinutos = h * 60 + m },
            hora.hour,
            hora.minute,
            true,
        ).show()
    }

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Nueva tarea") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción (opcional)") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                )

                Text("Cuándo", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CategoriaTarea.entries.forEach { opcion ->
                        FilterChip(
                            selected = categoria == opcion,
                            onClick = {
                                categoria = opcion
                                if (opcion != CategoriaTarea.BACKLOG) {
                                    fechaEpoch = cuandoOcurre(opcion).toEpochDay()
                                }
                            },
                            label = { Text(opcion.etiqueta()) },
                        )
                    }
                }

                OutlinedButton(onClick = abrirSelectorFecha, modifier = Modifier.fillMaxWidth()) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(fechaLarga(fecha))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Recordatorio", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = recordatorioActivo,
                        onCheckedChange = { recordatorioActivo = it },
                    )
                }
                if (recordatorioActivo) {
                    OutlinedButton(onClick = abrirSelectorHora, modifier = Modifier.fillMaxWidth()) {
                        Text("Avisar a las ${horaCorta(LocalTime.of(horaMinutos / 60, horaMinutos % 60))}")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Modo Pomodoro", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "Enfoque y descansos con los tiempos de Configuración.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = pomodoro,
                        onCheckedChange = { pomodoro = it },
                    )
                }

                if (!pomodoro) {
                    OutlinedTextField(
                        value = duracionTexto,
                        onValueChange = { nuevo -> duracionTexto = nuevo.filter(Char::isDigit).take(3) },
                        label = { Text("Duración en minutos (opcional)") },
                        isError = !duracionValida,
                        supportingText = {
                            Text(
                                if (duracionValida) {
                                    "Con duración, la tarea muestra el botón Iniciar."
                                } else {
                                    "Usa un valor entre 1 y 720 minutos."
                                },
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(15, 25, 45).forEach { minutos ->
                            val valor = minutos.toString()
                            FilterChip(
                                selected = duracionTexto == valor,
                                onClick = {
                                    duracionTexto = if (duracionTexto == valor) "" else valor
                                },
                                label = { Text("$minutos min") },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onGuardar(
                        Tarea(
                            titulo = titulo.trim(),
                            descripcion = descripcion.trim(),
                            fechaVencimiento = fecha,
                            categoria = categoria,
                            duracionMinutos = if (pomodoro) null else duracion,
                            horaRecordatorio = if (recordatorioActivo) {
                                LocalTime.of(horaMinutos / 60, horaMinutos % 60)
                            } else {
                                null
                            },
                            usarPomodoro = pomodoro,
                        ),
                    )
                },
                enabled = puedeGuardar,
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) { Text("Cancelar") }
        },
    )
}

private fun cuandoOcurre(categoria: CategoriaTarea): LocalDate = when (categoria) {
    CategoriaTarea.HOY -> LocalDate.now()
    CategoriaTarea.MANANA -> LocalDate.now().plusDays(1)
    CategoriaTarea.BACKLOG -> LocalDate.now()
}

/** Mantiene la categoría coherente con la fecha elegida a mano. */
private fun categoriaParaFecha(fecha: LocalDate): CategoriaTarea {
    val hoy = LocalDate.now()
    return when {
        fecha == hoy -> CategoriaTarea.HOY
        fecha == hoy.plusDays(1) -> CategoriaTarea.MANANA
        else -> CategoriaTarea.BACKLOG
    }
}

private fun CategoriaTarea.etiqueta(): String = when (this) {
    CategoriaTarea.HOY -> "Hoy"
    CategoriaTarea.MANANA -> "Mañana"
    CategoriaTarea.BACKLOG -> "Backlog"
}
