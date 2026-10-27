package com.enfoque.app.ui.panel

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.enfoque.app.domain.model.TareaRecurrente
import com.enfoque.app.ui.util.horaCorta
import java.time.LocalTime

/**
 * Plantilla de una rutina. No se programa sola: queda en la sección Rutinas y
 * desde ahí se añade a Hoy o Mañana cuando el usuario la necesita.
 */
@Composable
fun NuevaRutinaDialog(
    onCerrar: () -> Unit,
    onGuardar: (TareaRecurrente) -> Unit,
) {
    var titulo by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var duracionTexto by rememberSaveable { mutableStateOf("") }
    var recordatorioActivo by rememberSaveable { mutableStateOf(true) }
    var horaMinutos by rememberSaveable { mutableIntStateOf(9 * 60) }
    var pomodoro by rememberSaveable { mutableStateOf(false) }

    val contexto = LocalContext.current
    val duracion = duracionTexto.trim().toIntOrNull()
    val duracionValida = duracionTexto.isBlank() || (duracion != null && duracion in 1..720)
    val puedeGuardar = titulo.isNotBlank() && (pomodoro || duracionValida)
    val hora = LocalTime.of(horaMinutos / 60, horaMinutos % 60)

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Nueva rutina") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    "Estará siempre disponible para añadirla a Hoy o Mañana.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Nombre") },
                    placeholder = { Text("Estudiar inglés") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Nota (opcional)") },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )

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
                    OutlinedButton(
                        onClick = {
                            TimePickerDialog(
                                contexto,
                                { _, h, m -> horaMinutos = h * 60 + m },
                                hora.hour,
                                hora.minute,
                                true,
                            ).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Avisar a las ${horaCorta(hora)}")
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
                            "Las tareas de esta rutina usarán Pomodoro.",
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
                        TareaRecurrente(
                            titulo = titulo.trim(),
                            descripcion = descripcion.trim(),
                            duracionMinutos = if (pomodoro) null else duracion,
                            usarPomodoro = pomodoro,
                            horaRecordatorio = if (recordatorioActivo) hora else null,
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
