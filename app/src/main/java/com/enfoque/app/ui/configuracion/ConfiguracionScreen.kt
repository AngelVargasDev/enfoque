package com.enfoque.app.ui.configuracion

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.enfoque.app.data.preferencias.PreferenciasApp
import com.enfoque.app.domain.model.AjustesPomodoro
import com.enfoque.app.domain.model.ModoDiseno
import com.enfoque.app.domain.model.TemaApp
import com.enfoque.app.ui.theme.paletaDiseno

/**
 * Configuración de la app: tema, paleta y tiempos del Pomodoro. Todos los
 * cambios se guardan al instante en [PreferenciasApp].
 */
@Composable
fun ConfiguracionScreen(
    preferencias: PreferenciasApp,
    onCerrar: () -> Unit,
) {
    val ajustes by preferencias.ajustes.collectAsStateWithLifecycle()
    val oscuro = when (ajustes.tema) {
        TemaApp.SISTEMA -> isSystemInDarkTheme()
        TemaApp.CLARO -> false
        TemaApp.OSCURO -> true
    }

    BackHandler(onBack = onCerrar)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(Modifier.fillMaxSize().systemBarsPadding()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onCerrar) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                    )
                }
                Text(
                    "Configuración",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(26.dp),
            ) {
                item(key = "tema") {
                    Seccion("Tema") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TemaApp.entries.forEach { tema ->
                                FilterChip(
                                    selected = ajustes.tema == tema,
                                    onClick = { preferencias.definirTema(tema) },
                                    label = { Text(tema.etiqueta) },
                                )
                            }
                        }
                    }
                }

                item(key = "diseno") {
                    Seccion(
                        titulo = "Diseño",
                        descripcion = "Elige la paleta que prefieras. Se aplica al instante.",
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ModoDiseno.entries.forEach { modo ->
                                FilaDiseno(
                                    modo = modo,
                                    seleccionado = ajustes.modoDiseno == modo,
                                    oscuro = oscuro,
                                    onSeleccionar = { preferencias.definirModoDiseno(modo) },
                                )
                            }
                        }
                    }
                }

                item(key = "pomodoro") {
                    Seccion(
                        titulo = "Pomodoro",
                        descripcion = "El descanso largo llega después de varios bloques de enfoque.",
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                PresetChip(
                                    texto = "Clásico",
                                    seleccionado = ajustes.pomodoro.esPreset(25, 5, 15, 4),
                                    onClick = {
                                        preferencias.actualizarPomodoro {
                                            it.copy(
                                                minutosEnfoque = 25,
                                                minutosDescansoCorto = 5,
                                                minutosDescansoLargo = 15,
                                                ciclosAntesDescansoLargo = 4,
                                            )
                                        }
                                    },
                                )
                                PresetChip(
                                    texto = "Profundo",
                                    seleccionado = ajustes.pomodoro.esPreset(50, 10, 20, 4),
                                    onClick = {
                                        preferencias.actualizarPomodoro {
                                            it.copy(
                                                minutosEnfoque = 50,
                                                minutosDescansoCorto = 10,
                                                minutosDescansoLargo = 20,
                                                ciclosAntesDescansoLargo = 4,
                                            )
                                        }
                                    },
                                )
                                PresetChip(
                                    texto = "Suave",
                                    seleccionado = ajustes.pomodoro.esPreset(15, 3, 10, 3),
                                    onClick = {
                                        preferencias.actualizarPomodoro {
                                            it.copy(
                                                minutosEnfoque = 15,
                                                minutosDescansoCorto = 3,
                                                minutosDescansoLargo = 10,
                                                ciclosAntesDescansoLargo = 3,
                                            )
                                        }
                                    },
                                )
                            }

                            FilaNumero(
                                etiqueta = "Enfoque",
                                detalle = "minutos por bloque",
                                valor = ajustes.pomodoro.minutosEnfoque,
                                onCambiar = { valor ->
                                    preferencias.actualizarPomodoro { it.copy(minutosEnfoque = valor) }
                                },
                            )
                            FilaNumero(
                                etiqueta = "Descanso corto",
                                detalle = "minutos entre bloques",
                                valor = ajustes.pomodoro.minutosDescansoCorto,
                                onCambiar = { valor ->
                                    preferencias.actualizarPomodoro { it.copy(minutosDescansoCorto = valor) }
                                },
                            )
                            FilaNumero(
                                etiqueta = "Descanso largo",
                                detalle = "minutos al cerrar un ciclo",
                                valor = ajustes.pomodoro.minutosDescansoLargo,
                                onCambiar = { valor ->
                                    preferencias.actualizarPomodoro { it.copy(minutosDescansoLargo = valor) }
                                },
                            )
                            FilaNumero(
                                etiqueta = "Bloques por ciclo",
                                detalle = "enfoques antes del descanso largo",
                                valor = ajustes.pomodoro.ciclosAntesDescansoLargo,
                                onCambiar = { valor ->
                                    preferencias.actualizarPomodoro {
                                        it.copy(ciclosAntesDescansoLargo = valor)
                                    }
                                },
                            )

                            FilaSwitch(
                                etiqueta = "Iniciar descansos automáticamente",
                                detalle = "Al terminar un enfoque, empieza el descanso.",
                                activo = ajustes.pomodoro.autoIniciarDescansos,
                                onCambiar = { activo ->
                                    preferencias.actualizarPomodoro { it.copy(autoIniciarDescansos = activo) }
                                },
                            )
                            FilaSwitch(
                                etiqueta = "Iniciar el siguiente enfoque automáticamente",
                                detalle = "Si está desactivado, la app espera tu toque.",
                                activo = ajustes.pomodoro.autoIniciarEnfoque,
                                onCambiar = { activo ->
                                    preferencias.actualizarPomodoro { it.copy(autoIniciarEnfoque = activo) }
                                },
                            )
                        }
                    }
                }

                item(key = "pie") {
                    Text(
                        "Enfoque 0.3.0 · Los cambios se guardan al instante.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun Seccion(
    titulo: String,
    descripcion: String? = null,
    contenido: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleLarge)
            if (descripcion != null) {
                Text(
                    descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        contenido()
    }
}

@Composable
private fun FilaDiseno(
    modo: ModoDiseno,
    seleccionado: Boolean,
    oscuro: Boolean,
    onSeleccionar: () -> Unit,
) {
    val paleta = paletaDiseno(modo, oscuro)
    Surface(
        onClick = onSeleccionar,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = if (seleccionado) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (seleccionado) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            },
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Circulo(paleta.primario)
                Circulo(paleta.secundario)
                Circulo(paleta.fondo, borde = paleta.linea)
            }
            Spacer(Modifier.width(12.dp))
            Text(
                modo.etiqueta,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            if (seleccionado) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun Circulo(color: Color, borde: Color? = null) {
    Box(
        modifier = Modifier
            .size(18.dp)
            .clip(CircleShape)
            .background(color)
            .then(if (borde != null) Modifier.border(1.dp, borde, CircleShape) else Modifier),
    )
}

@Composable
private fun PresetChip(texto: String, seleccionado: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = seleccionado,
        onClick = onClick,
        label = { Text(texto) },
    )
}

@Composable
private fun FilaNumero(
    etiqueta: String,
    detalle: String,
    valor: Int,
    onCambiar: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(etiqueta, style = MaterialTheme.typography.bodyLarge)
            Text(
                detalle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        TextButton(onClick = { onCambiar(valor - 1) }) {
            Text("−", style = MaterialTheme.typography.titleMedium)
        }
        Text(
            valor.toString(),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(min = 28.dp),
        )
        TextButton(onClick = { onCambiar(valor + 1) }) {
            Text("+", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun FilaSwitch(
    etiqueta: String,
    detalle: String,
    activo: Boolean,
    onCambiar: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(etiqueta, style = MaterialTheme.typography.bodyLarge)
            Text(
                detalle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = activo, onCheckedChange = onCambiar)
    }
}

private fun AjustesPomodoro.esPreset(
    enfoque: Int,
    corto: Int,
    largo: Int,
    ciclos: Int,
): Boolean = minutosEnfoque == enfoque &&
    minutosDescansoCorto == corto &&
    minutosDescansoLargo == largo &&
    ciclosAntesDescansoLargo == ciclos
