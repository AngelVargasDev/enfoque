package com.enfoque.app.ui.panel

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.enfoque.app.data.alarma.Alarmas
import com.enfoque.app.domain.model.CategoriaTarea
import com.enfoque.app.domain.model.Racha
import com.enfoque.app.domain.model.Tarea
import com.enfoque.app.domain.model.TareaRecurrente
import com.enfoque.app.ui.panel.componentes.TareaCard
import com.enfoque.app.ui.util.fechaLarga
import com.enfoque.app.ui.util.horaCorta
import kotlinx.coroutines.flow.collect

/** Pantalla única con los paneles de Enfoque Diario y Planeación. */
@Composable
fun PanelScreen(
    viewModel: PanelViewModel,
    onAbrirConfiguracion: () -> Unit,
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var mostrarDialogoTarea by rememberSaveable { mutableStateOf(false) }
    var mostrarDialogoRutina by rememberSaveable { mutableStateOf(false) }
    var tareaAEliminar by remember { mutableStateOf<Tarea?>(null) }
    var rutinaAEliminar by remember { mutableStateOf<TareaRecurrente?>(null) }

    val contexto = LocalContext.current
    val ciclo = LocalLifecycleOwner.current.lifecycle
    var alarmasExactas by remember { mutableStateOf(Alarmas.puedeProgramarExactas(contexto)) }

    DisposableEffect(ciclo, contexto) {
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) {
                val exactasAhora = Alarmas.puedeProgramarExactas(contexto)
                if (exactasAhora && !alarmasExactas) {
                    // Se acaba de conceder el permiso: reemplaza las alarmas
                    // inexactas por alarmas exactas.
                    viewModel.reprogramarRecordatorios()
                }
                alarmasExactas = exactasAhora
                viewModel.refrescarDia()
            }
        }
        ciclo.addObserver(observador)
        onDispose { ciclo.removeObserver(observador) }
    }

    LaunchedEffect(viewModel) {
        viewModel.eventos.collect { mensaje -> snackbar.showSnackbar(mensaje) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { mostrarDialogoTarea = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp)) },
                text = { Text("Nueva tarea", style = MaterialTheme.typography.labelLarge) },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                start = 24.dp,
                end = 24.dp,
                top = 24.dp,
                bottom = 112.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            item(key = "cabecera") {
                CabeceraPrincipal(
                    fecha = estado.hoy,
                    completadas = estado.completadasHoy.size,
                    total = estado.totalHoy,
                    racha = estado.racha,
                    onAbrirConfiguracion = onAbrirConfiguracion,
                )
            }

            if (!alarmasExactas) {
                item(key = "aviso-alarmas") {
                    AvisoAlarmas(onActivar = { abrirAjustesAlarmas(contexto) })
                }
            }

            item(key = "seccion-hoy") {
                SeccionHoy(
                    estado = estado,
                    onCompletar = viewModel::alternarCompletada,
                    onIniciar = viewModel::iniciarTemporizador,
                    onIniciarPomodoro = viewModel::iniciarPomodoro,
                    onMover = viewModel::moverTarea,
                    onEliminar = { tareaAEliminar = it },
                )
            }

            item(key = "seccion-rutinas") {
                SeccionRutinas(
                    rutinas = estado.rutinas,
                    onAgregar = { mostrarDialogoRutina = true },
                    onAlternar = viewModel::alternarRutina,
                    onEliminar = { rutinaAEliminar = it },
                )
            }

            item(key = "seccion-manana") {
                SeccionLista(
                    titulo = "Mañana",
                    tareas = estado.manana,
                    mensajeVacio = "Nada programado para mañana.",
                    onCompletar = viewModel::alternarCompletada,
                    onIniciar = viewModel::iniciarTemporizador,
                    onIniciarPomodoro = viewModel::iniciarPomodoro,
                    onMover = viewModel::moverTarea,
                    onEliminar = { tareaAEliminar = it },
                )
            }

            item(key = "seccion-backlog") {
                SeccionLista(
                    titulo = "Backlog",
                    tareas = estado.backlog,
                    mensajeVacio = "Sin ideas pendientes. Captura la siguiente con «Nueva tarea».",
                    onCompletar = viewModel::alternarCompletada,
                    onIniciar = viewModel::iniciarTemporizador,
                    onIniciarPomodoro = viewModel::iniciarPomodoro,
                    onMover = viewModel::moverTarea,
                    onEliminar = { tareaAEliminar = it },
                )
            }
        }
    }

    if (mostrarDialogoTarea) {
        NuevaTareaDialog(
            onCerrar = { mostrarDialogoTarea = false },
            onGuardar = { tarea ->
                viewModel.agregarTarea(tarea)
                mostrarDialogoTarea = false
            },
        )
    }

    if (mostrarDialogoRutina) {
        NuevaRutinaDialog(
            onCerrar = { mostrarDialogoRutina = false },
            onGuardar = { rutina ->
                viewModel.agregarRutina(rutina)
                mostrarDialogoRutina = false
            },
        )
    }

    tareaAEliminar?.let { tarea ->
        AlertDialog(
            onDismissRequest = { tareaAEliminar = null },
            title = { Text("Eliminar tarea") },
            text = { Text("«${tarea.titulo}» se eliminará de forma permanente.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.eliminarTarea(tarea)
                        tareaAEliminar = null
                    },
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { tareaAEliminar = null }) { Text("Cancelar") }
            },
        )
    }

    rutinaAEliminar?.let { rutina ->
        AlertDialog(
            onDismissRequest = { rutinaAEliminar = null },
            title = { Text("Eliminar rutina") },
            text = {
                Text(
                    "«${rutina.titulo}» dejará de aparecer en Rutinas. " +
                        "Las tareas que ya añadiste se conservan.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.eliminarRutina(rutina)
                        rutinaAEliminar = null
                    },
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { rutinaAEliminar = null }) { Text("Cancelar") }
            },
        )
    }
}

@Composable
private fun CabeceraPrincipal(
    fecha: java.time.LocalDate,
    completadas: Int,
    total: Int,
    racha: Racha,
    onAbrirConfiguracion: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text("Enfoque", style = MaterialTheme.typography.headlineMedium)
                Text(
                    fechaLarga(fecha),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onAbrirConfiguracion) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Configuración",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Text(
            text = textoRacha(racha),
            style = MaterialTheme.typography.labelMedium,
            color = if (racha.actual > 0 && !racha.hoyCompletado) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.secondary
            },
        )

        if (total > 0) {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        "$completadas de $total completadas",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        "${(completadas * 100 / total)}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
                LinearProgressIndicator(
                    progress = { completadas.toFloat() / total },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
        }
    }
}

private fun textoRacha(racha: Racha): String {
    val diasActual = if (racha.actual == 1) "día" else "días"
    val diasMejor = if (racha.mejor == 1) "día" else "días"
    return when {
        racha.actual > 0 && racha.hoyCompletado ->
            "Racha de ${racha.actual} $diasActual · mejor ${racha.mejor} $diasMejor"
        racha.actual > 0 ->
            "Racha de ${racha.actual} $diasActual · hoy pendiente"
        racha.mejor > 0 ->
            "Mejor racha: ${racha.mejor} $diasMejor · empieza otra"
        else ->
            "Completa una tarea para empezar tu racha"
    }
}

@Composable
private fun SeccionHoy(
    estado: PanelUiState,
    onCompletar: (Tarea) -> Unit,
    onIniciar: (Tarea) -> Unit,
    onIniciarPomodoro: (Tarea) -> Unit,
    onMover: (Tarea, CategoriaTarea) -> Unit,
    onEliminar: (Tarea) -> Unit,
) {
    var mostrarCompletadas by rememberSaveable { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        CabeceraSeccion(titulo = "Hoy", cantidad = estado.totalHoy)

        if (estado.totalHoy == 0) {
            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.secondary,
                )
                Text(
                    "Día despejado. Elige una rutina o crea una tarea.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            estado.enfoqueDiario.forEach { tarea ->
                TareaCard(
                    tarea = tarea,
                    onCompletarCambio = { onCompletar(tarea) },
                    onIniciar = { onIniciar(tarea) },
                    onIniciarPomodoro = { onIniciarPomodoro(tarea) },
                    onMover = { categoria -> onMover(tarea, categoria) },
                    onEliminar = { onEliminar(tarea) },
                )
            }
        }

        if (estado.completadasHoy.isNotEmpty()) {
            TextButton(
                onClick = { mostrarCompletadas = !mostrarCompletadas },
                contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
            ) {
                Text(
                    if (mostrarCompletadas) {
                        "Ocultar completadas (${estado.completadasHoy.size})"
                    } else {
                        "Ver completadas (${estado.completadasHoy.size})"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (mostrarCompletadas) {
            estado.completadasHoy.forEach { tarea ->
                TareaCard(
                    tarea = tarea,
                    onCompletarCambio = { onCompletar(tarea) },
                    onIniciar = { onIniciar(tarea) },
                    onIniciarPomodoro = { onIniciarPomodoro(tarea) },
                    onMover = { categoria -> onMover(tarea, categoria) },
                    onEliminar = { onEliminar(tarea) },
                )
            }
        }
    }
}

@Composable
private fun SeccionRutinas(
    rutinas: List<RutinaUi>,
    onAgregar: () -> Unit,
    onAlternar: (TareaRecurrente, CategoriaTarea) -> Unit,
    onEliminar: (TareaRecurrente) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        CabeceraSeccion(
            titulo = "Rutinas",
            cantidad = rutinas.size,
            accion = {
                TextButton(
                    onClick = onAgregar,
                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.size(4.dp))
                    Text("Añadir", style = MaterialTheme.typography.labelLarge)
                }
            },
        )

        Text(
            "Selecciona las que harás hoy o mañana.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (rutinas.isEmpty()) {
            Text(
                "Aún no tienes rutinas. Crea una para no repetir la misma tarea cada día.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 2.dp),
            )
        }

        rutinas.forEach { elemento ->
            RutinaRow(
                elemento = elemento,
                onAlternar = onAlternar,
                onEliminar = onEliminar,
            )
        }
    }
}

@Composable
private fun RutinaRow(
    elemento: RutinaUi,
    onAlternar: (TareaRecurrente, CategoriaTarea) -> Unit,
    onEliminar: (TareaRecurrente) -> Unit,
) {
    val rutina = elemento.rutina

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(start = 14.dp, end = 6.dp, top = 8.dp, bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(rutina.titulo, style = MaterialTheme.typography.titleMedium)
                    val detalles = buildList {
                        if (rutina.usarPomodoro) {
                            add("Pomodoro")
                        } else {
                            rutina.duracionMinutos?.let { add("$it min") }
                        }
                        rutina.horaRecordatorio?.let { add(horaCorta(it)) }
                        if (rutina.descripcion.isNotBlank()) add(rutina.descripcion)
                    }
                    if (detalles.isNotEmpty()) {
                        Text(
                            detalles.joinToString(" · "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                }
                IconButton(onClick = { onEliminar(rutina) }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar rutina",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChipSeleccion(
                    texto = "Hoy",
                    seleccionado = elemento.enHoy,
                    onClick = { onAlternar(rutina, CategoriaTarea.HOY) },
                )
                ChipSeleccion(
                    texto = "Mañana",
                    seleccionado = elemento.enManana,
                    onClick = { onAlternar(rutina, CategoriaTarea.MANANA) },
                )
            }
        }
    }
}

@Composable
private fun ChipSeleccion(texto: String, seleccionado: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (seleccionado) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surface
        },
        contentColor = if (seleccionado) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        border = if (seleccionado) null else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            if (seleccionado) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                )
            }
            Text(texto, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun SeccionLista(
    titulo: String,
    tareas: List<Tarea>,
    mensajeVacio: String,
    onCompletar: (Tarea) -> Unit,
    onIniciar: (Tarea) -> Unit,
    onIniciarPomodoro: (Tarea) -> Unit,
    onMover: (Tarea, CategoriaTarea) -> Unit,
    onEliminar: (Tarea) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        CabeceraSeccion(titulo = titulo, cantidad = tareas.size)

        if (tareas.isEmpty()) {
            Text(
                mensajeVacio,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 2.dp),
            )
        } else {
            tareas.forEach { tarea ->
                TareaCard(
                    tarea = tarea,
                    onCompletarCambio = { onCompletar(tarea) },
                    onIniciar = { onIniciar(tarea) },
                    onIniciarPomodoro = { onIniciarPomodoro(tarea) },
                    onMover = { categoria -> onMover(tarea, categoria) },
                    onEliminar = { onEliminar(tarea) },
                )
            }
        }
    }
}

@Composable
private fun CabeceraSeccion(
    titulo: String,
    cantidad: Int? = null,
    accion: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleLarge,
            letterSpacing = (-0.2).sp,
            modifier = Modifier.weight(1f),
        )
        if (cantidad != null && cantidad > 0) {
            Text(
                cantidad.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        accion?.invoke()
    }
}

@Composable
private fun AvisoAlarmas(onActivar: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 6.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    "Alarmas exactas desactivadas",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    "Los avisos podrían sonar con minutos de retraso.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            TextButton(onClick = onActivar) {
                Text("Activar", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

private fun abrirAjustesAlarmas(contexto: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
    val intent = Intent(
        Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
        Uri.parse("package:${contexto.packageName}"),
    )
    try {
        contexto.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        contexto.startActivity(
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:${contexto.packageName}"),
            ),
        )
    }
}
