package com.enfoque.app.ui.panel.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.enfoque.app.domain.model.CategoriaTarea
import com.enfoque.app.domain.model.EstadoTarea
import com.enfoque.app.domain.model.Tarea
import com.enfoque.app.ui.theme.EnfoqueTheme
import com.enfoque.app.ui.util.etiquetaFecha
import com.enfoque.app.ui.util.horaCorta
import java.time.LocalDate
import java.time.LocalTime

/**
 * Fila de tarea compartida por todos los paneles. Sin elevación ni bloques de
 * color: la jerarquía la marcan el espacio y una línea fina.
 */
@Composable
fun TareaCard(
    tarea: Tarea,
    onCompletarCambio: () -> Unit,
    onIniciar: () -> Unit,
    onMover: (CategoriaTarea) -> Unit,
    onEliminar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuAbierto by remember { mutableStateOf(false) }
    val completada = tarea.estaCompletada
    val accionRapida = when (tarea.categoria) {
        CategoriaTarea.BACKLOG -> CategoriaTarea.MANANA
        CategoriaTarea.MANANA -> CategoriaTarea.HOY
        CategoriaTarea.HOY -> null
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(start = 4.dp, end = 4.dp, top = 4.dp, bottom = 6.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Checkbox(
                    checked = completada,
                    onCheckedChange = { onCompletarCambio() },
                    modifier = Modifier.padding(top = 2.dp),
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 10.dp, bottom = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = tarea.titulo,
                        style = MaterialTheme.typography.titleMedium,
                        textDecoration = if (completada) TextDecoration.LineThrough else null,
                        color = if (completada) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )
                    if (tarea.descripcion.isNotBlank()) {
                        Text(
                            text = tarea.descripcion,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (tarea.estado == EstadoTarea.EN_PROGRESO) {
                            Text(
                                "En curso",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                            )
                        }
                        Text(
                            text = etiquetaFecha(tarea.fechaVencimiento),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (tarea.categoria == CategoriaTarea.HOY) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                        tarea.duracionMinutos?.let { minutos ->
                            Text(
                                "$minutos min",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        tarea.horaRecordatorio?.let { hora ->
                            Text(
                                horaCorta(hora),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                Box {
                    IconButton(onClick = { menuAbierto = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Más opciones",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    DropdownMenu(
                        expanded = menuAbierto,
                        onDismissRequest = { menuAbierto = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Mover a hoy") },
                            onClick = {
                                menuAbierto = false
                                onMover(CategoriaTarea.HOY)
                            },
                            enabled = tarea.categoria != CategoriaTarea.HOY,
                        )
                        DropdownMenuItem(
                            text = { Text("Mover a mañana") },
                            onClick = {
                                menuAbierto = false
                                onMover(CategoriaTarea.MANANA)
                            },
                            enabled = tarea.categoria != CategoriaTarea.MANANA,
                        )
                        DropdownMenuItem(
                            text = { Text("Enviar al backlog") },
                            onClick = {
                                menuAbierto = false
                                onMover(CategoriaTarea.BACKLOG)
                            },
                            enabled = tarea.categoria != CategoriaTarea.BACKLOG,
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                )
                            },
                            onClick = {
                                menuAbierto = false
                                onEliminar()
                            },
                        )
                    }
                }
            }

            if (!completada && (accionRapida != null || tarea.tieneTemporizador)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 40.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (accionRapida != null) {
                        TextButton(
                            onClick = { onMover(accionRapida) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (accionRapida == CategoriaTarea.MANANA) "Mañana" else "Hoy",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    if (tarea.tieneTemporizador) {
                        TextButton(
                            onClick = onIniciar,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Iniciar",
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TareaCardPreview() {
    EnfoqueTheme {
        TareaCard(
            tarea = Tarea(
                id = 1,
                titulo = "Preparar propuesta del cliente",
                descripcion = "Revisar alcance, costos y enviar el documento final.",
                fechaVencimiento = LocalDate.now(),
                categoria = CategoriaTarea.HOY,
                duracionMinutos = 25,
                horaRecordatorio = LocalTime.of(9, 0),
            ),
            onCompletarCambio = {},
            onIniciar = {},
            onMover = {},
            onEliminar = {},
        )
    }
}
