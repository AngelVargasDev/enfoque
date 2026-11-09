package com.enfoque.app.ui.peso

import android.app.DatePickerDialog
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.enfoque.app.domain.model.Peso
import com.enfoque.app.ui.util.fechaLarga
import com.enfoque.app.ui.util.filtrarEntradaPeso
import com.enfoque.app.ui.util.formatoPeso
import com.enfoque.app.ui.util.parsearPeso
import java.time.LocalDate
import kotlin.math.roundToInt

/** Alta o edición de un peso diario. */
@Composable
fun PesoDialog(
    fechaInicial: LocalDate,
    pesosPorFecha: Map<LocalDate, Peso>,
    onGuardar: (Peso) -> Unit,
    onEliminar: (LocalDate) -> Unit,
    onCerrar: () -> Unit,
) {
    val inicial = pesosPorFecha[fechaInicial]
    var fechaEpoch by rememberSaveable(fechaInicial) {
        mutableLongStateOf(fechaInicial.toEpochDay())
    }
    var pesoTexto by rememberSaveable(fechaInicial) {
        mutableStateOf(inicial?.let { formatoPeso(it.pesoKg) } ?: "")
    }
    var nota by rememberSaveable(fechaInicial) {
        mutableStateOf(inicial?.nota ?: "")
    }
    var confirmarEliminar by rememberSaveable { mutableStateOf(false) }

    val contexto = LocalContext.current
    val fecha = LocalDate.ofEpochDay(fechaEpoch)
    val existente = pesosPorFecha[fecha]
    val peso = parsearPeso(pesoTexto)
    val valido = peso != null && peso in 20.0..300.0

    val abrirSelectorFecha = {
        DatePickerDialog(
            contexto,
            { _, anio, mes, dia ->
                val nueva = LocalDate.of(anio, mes + 1, dia)
                fechaEpoch = nueva.toEpochDay()
                val registro = pesosPorFecha[nueva]
                pesoTexto = registro?.let { formatoPeso(it.pesoKg) } ?: ""
                nota = registro?.nota ?: ""
            },
            fecha.year,
            fecha.monthValue - 1,
            fecha.dayOfMonth,
        ).apply {
            datePicker.maxDate = System.currentTimeMillis()
        }.show()
    }

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text(if (existente != null) "Editar peso" else "Registrar peso") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    "Un registro por día. Si eliges una fecha que ya existe, se actualiza.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedButton(onClick = abrirSelectorFecha, modifier = Modifier.fillMaxWidth()) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(fechaLarga(fecha))
                }

                OutlinedTextField(
                    value = pesoTexto,
                    onValueChange = { pesoTexto = filtrarEntradaPeso(it) },
                    label = { Text("Peso") },
                    suffix = { Text("kg") },
                    isError = pesoTexto.isNotBlank() && !valido,
                    supportingText = {
                        Text(
                            if (valido || pesoTexto.isBlank()) {
                                "En kilogramos, por ejemplo 72.4"
                            } else {
                                "Usa un peso entre 20 y 300 kg."
                            },
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = nota,
                    onValueChange = { nota = it },
                    label = { Text("Nota (opcional)") },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (peso != null) {
                        onGuardar(
                            Peso(
                                fecha = fecha,
                                pesoKg = (peso * 10).roundToInt() / 10.0,
                                nota = nota.trim(),
                            ),
                        )
                    }
                },
                enabled = valido,
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (existente != null) {
                    TextButton(onClick = { confirmarEliminar = true }) {
                        Text("Eliminar", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onCerrar) { Text("Cancelar") }
            }
        },
    )

    if (confirmarEliminar) {
        AlertDialog(
            onDismissRequest = { confirmarEliminar = false },
            title = { Text("Eliminar registro") },
            text = { Text("Se borrará el peso del ${fechaLarga(existente?.fecha ?: fecha)}.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmarEliminar = false
                        onEliminar(fecha)
                    },
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarEliminar = false }) { Text("Cancelar") }
            },
        )
    }
}
