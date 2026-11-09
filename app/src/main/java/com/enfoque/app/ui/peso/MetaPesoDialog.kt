package com.enfoque.app.ui.peso

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.enfoque.app.ui.util.filtrarEntradaPeso
import com.enfoque.app.ui.util.formatoPeso
import com.enfoque.app.ui.util.parsearPeso
import kotlin.math.roundToInt

@Composable
fun MetaPesoDialog(
    metaActual: Double?,
    onGuardar: (Double?) -> Unit,
    onCerrar: () -> Unit,
) {
    var texto by rememberSaveable(metaActual) {
        mutableStateOf(metaActual?.let { formatoPeso(it) } ?: "")
    }
    val meta = parsearPeso(texto)
    val valido = meta != null && meta in 20.0..300.0

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Meta de peso") },
        text = {
            Column {
                Text(
                    "El progreso se calcula desde tu primer registro.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = texto,
                    onValueChange = { texto = filtrarEntradaPeso(it) },
                    label = { Text("Meta") },
                    suffix = { Text("kg") },
                    isError = texto.isNotBlank() && !valido,
                    supportingText = {
                        Text(
                            if (valido || texto.isBlank()) {
                                "En kilogramos, por ejemplo 68.0"
                            } else {
                                "Usa una meta entre 20 y 300 kg."
                            },
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (meta != null) onGuardar((meta * 10).roundToInt() / 10.0) },
                enabled = valido,
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            Column {
                if (metaActual != null) {
                    TextButton(onClick = { onGuardar(null) }) {
                        Text("Quitar meta", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onCerrar) { Text("Cancelar") }
            }
        },
    )
}
