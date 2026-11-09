package com.enfoque.app.ui.peso

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.enfoque.app.domain.model.Peso
import com.enfoque.app.ui.navegacion.BarraNavegacion
import com.enfoque.app.ui.navegacion.SeccionPrincipal
import com.enfoque.app.ui.util.fechaLarga
import com.enfoque.app.ui.util.fechaHistorial
import com.enfoque.app.ui.util.formatoPeso
import com.enfoque.app.ui.util.textoCambio
import kotlinx.coroutines.flow.collect
import java.time.LocalDate
import kotlin.math.max

@Composable
fun PesoScreen(
    viewModel: PesoViewModel,
    onAbrirConfiguracion: () -> Unit,
    onNavegar: (SeccionPrincipal) -> Unit,
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var filtroDias by rememberSaveable { mutableIntStateOf(30) }
    var dialogoPesoAbierto by rememberSaveable { mutableStateOf(false) }
    var fechaEdicionEpoch by rememberSaveable { mutableStateOf<Long?>(null) }
    var dialogoMetaAbierto by rememberSaveable { mutableStateOf(false) }

    val ciclo = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(ciclo) {
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) viewModel.refrescarDia()
        }
        ciclo.addObserver(observador)
        onDispose { ciclo.removeObserver(observador) }
    }

    LaunchedEffect(viewModel) {
        viewModel.eventos.collect { mensaje -> snackbar.showSnackbar(mensaje) }
    }

    val abrirRegistro = { fecha: LocalDate ->
        fechaEdicionEpoch = fecha.toEpochDay()
        dialogoPesoAbierto = true
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            BarraNavegacion(
                actual = SeccionPrincipal.PESO,
                onSeleccionar = onNavegar,
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { abrirRegistro(estado.hoy) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp),
                icon = {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                },
                text = { Text("Registrar peso", style = MaterialTheme.typography.labelLarge) },
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
            item(key = "cabecera-peso") {
                CabeceraPeso(
                    fecha = estado.hoy,
                    onAbrirConfiguracion = onAbrirConfiguracion,
                )
            }

            item(key = "resumen-peso") {
                ResumenPeso(
                    estado = estado,
                    onRegistrar = { abrirRegistro(estado.hoy) },
                    onEditarMeta = { dialogoMetaAbierto = true },
                )
            }

            item(key = "evolucion-peso") {
                EvolucionPeso(
                    estado = estado,
                    filtroDias = filtroDias,
                    onCambiarFiltro = { filtroDias = it },
                )
            }

            item(key = "historial-cabecera") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        "Historial",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f),
                    )
                    if (estado.pesos.isNotEmpty()) {
                        Text(
                            estado.pesos.size.toString(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            if (estado.pesos.isEmpty()) {
                item(key = "historial-vacio") {
                    Text(
                        "Aún no hay registros. Anota tu peso de hoy para empezar a ver tu evolución.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                items(
                    items = estado.pesos.sortedByDescending { it.fecha },
                    key = { it.fecha.toEpochDay() },
                ) { peso ->
                    val anterior = estado.pesos.lastOrNull { it.fecha < peso.fecha }
                    FilaPeso(
                        peso = peso,
                        anterior = anterior,
                        hoy = estado.hoy,
                        onClick = { abrirRegistro(peso.fecha) },
                    )
                }
            }

            item(key = "consejo-peso") {
                Text(
                    "Consejo: pésate a la misma hora, preferiblemente al despertar y antes de desayunar.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }

    if (dialogoPesoAbierto) {
        val fecha = LocalDate.ofEpochDay(fechaEdicionEpoch ?: estado.hoy.toEpochDay())
        PesoDialog(
            fechaInicial = fecha,
            pesosPorFecha = estado.pesos.associateBy { it.fecha },
            onGuardar = { peso ->
                viewModel.guardar(peso)
                dialogoPesoAbierto = false
                fechaEdicionEpoch = null
            },
            onEliminar = { fechaEliminar ->
                viewModel.eliminar(fechaEliminar)
                dialogoPesoAbierto = false
                fechaEdicionEpoch = null
            },
            onCerrar = {
                dialogoPesoAbierto = false
                fechaEdicionEpoch = null
            },
        )
    }

    if (dialogoMetaAbierto) {
        MetaPesoDialog(
            metaActual = estado.metaKg,
            onGuardar = { meta ->
                viewModel.definirMeta(meta)
                dialogoMetaAbierto = false
            },
            onCerrar = { dialogoMetaAbierto = false },
        )
    }
}

@Composable
private fun CabeceraPeso(fecha: LocalDate, onAbrirConfiguracion: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text("Peso", style = MaterialTheme.typography.headlineMedium)
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
}

@Composable
private fun ResumenPeso(
    estado: PesoUiState,
    onRegistrar: () -> Unit,
    onEditarMeta: () -> Unit,
) {
    val actual = estado.actual
    if (actual == null) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                "Tu evolución empieza con el primer registro.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(
                onClick = onRegistrar,
                contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
            ) {
                Text("Registrar mi primer peso")
            }
        }
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                formatoPeso(actual.pesoKg),
                style = MaterialTheme.typography.displaySmall,
            )
            Text(
                "kg",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, bottom = 5.dp),
            )
            Spacer(Modifier.weight(1f))
            estado.cambioDesdeAnterior?.let { cambio ->
                CambioPeso(cambio = cambio)
            }
        }

        val anterior = estado.anterior
        if (anterior != null) {
            val referencia = if (anterior.fecha == estado.hoy.minusDays(1)) {
                "ayer"
            } else {
                fechaHistorial(anterior.fecha, estado.hoy).lowercase()
            }
            Text(
                "Último registro: ${fechaHistorial(actual.fecha, estado.hoy)} · " +
                    "${textoCambio(estado.cambioDesdeAnterior ?: 0.0)} desde $referencia",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Text(
                "Último registro: ${fechaHistorial(actual.fecha, estado.hoy)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Estadistica(
                etiqueta = "Total",
                valor = estado.cambioTotal?.let { textoCambio(it) } ?: "—",
                modifier = Modifier.weight(1f),
            )
            Estadistica(
                etiqueta = "7 días",
                valor = estado.cambioDesde(7)?.let { textoCambio(it) } ?: "—",
                modifier = Modifier.weight(1f),
            )
            Estadistica(
                etiqueta = "Prom. 7d",
                valor = estado.promedioSemana?.let { "${formatoPeso(it)} kg" } ?: "—",
                modifier = Modifier.weight(1f),
            )
        }

        if (estado.metaKg == null) {
            TextButton(
                onClick = onEditarMeta,
                contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
            ) {
                Text("Definir una meta de peso")
            }
        } else {
            val distancia = estado.distanciaMeta ?: 0.0
            val progreso = estado.progresoMeta ?: 0f
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        "Meta ${formatoPeso(estado.metaKg)} kg",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        if (distancia > 0.05) {
                            "Faltan ${formatoPeso(distancia)} kg"
                        } else {
                            "Meta alcanzada"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
                LinearProgressIndicator(
                    progress = { progreso },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                TextButton(
                    onClick = onEditarMeta,
                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
                ) {
                    Text(
                        "Editar meta",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun Estadistica(etiqueta: String, valor: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            etiqueta,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(valor, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun EvolucionPeso(
    estado: PesoUiState,
    filtroDias: Int,
    onCambiarFiltro: (Int) -> Unit,
) {
    val datos = estado.pesosEnRango(if (filtroDias == 0) null else filtroDias)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Evolución", style = MaterialTheme.typography.titleLarge)

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf(7, 30, 90, 0).forEach { dias ->
                FilterChip(
                    selected = filtroDias == dias,
                    onClick = { onCambiarFiltro(dias) },
                    label = { Text(if (dias == 0) "Todo" else "$dias días") },
                )
            }
        }

        if (datos.isEmpty()) {
            Text(
                "Sin registros en este periodo.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return@Column
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (estado.metaKg != null) {
                    Text(
                        "Meta ${formatoPeso(estado.metaKg)} kg",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
                GraficoPeso(
                    pesos = datos,
                    metaKg = estado.metaKg,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        fechaHistorial(datos.first().fecha, estado.hoy),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        fechaHistorial(datos.last().fecha, estado.hoy),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        val valores = datos.map { it.pesoKg }
        Text(
            "Mín ${formatoPeso(valores.min())} · Máx ${formatoPeso(valores.max())} · " +
                "Promedio ${formatoPeso(valores.average())} kg",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun GraficoPeso(
    pesos: List<Peso>,
    metaKg: Double?,
    modifier: Modifier = Modifier,
) {
    val colorLinea = MaterialTheme.colorScheme.primary
    val colorMeta = MaterialTheme.colorScheme.secondary
    val colorGrilla = MaterialTheme.colorScheme.outlineVariant
    val colorRelleno = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        if (pesos.isEmpty()) return@Canvas

        val valores = pesos.map { it.pesoKg }
        val margen = max(0.5, (valores.max() - valores.min()) * 0.15)
        val minimo = valores.min() - margen
        val maximo = valores.max() + margen
        val rango = (maximo - minimo).takeIf { it > 0.0 } ?: 1.0
        val ancho = size.width
        val alto = size.height

        fun yDe(peso: Double): Float =
            (alto - ((peso - minimo) / rango) * alto).toFloat()

        // Líneas de referencia.
        listOf(0.25f, 0.5f, 0.75f).forEach { fraccion ->
            val y = alto * fraccion
            drawLine(
                color = colorGrilla.copy(alpha = 0.5f),
                start = Offset(0f, y),
                end = Offset(ancho, y),
                strokeWidth = 1.dp.toPx(),
            )
        }

        // Línea de meta.
        if (metaKg != null && metaKg in minimo..maximo) {
            val y = yDe(metaKg)
            drawLine(
                color = colorMeta.copy(alpha = 0.7f),
                start = Offset(0f, y),
                end = Offset(ancho, y),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)),
            )
        }

        val primera = pesos.first().fecha.toEpochDay()
        val ultima = pesos.last().fecha.toEpochDay()
        val rangoDias = (ultima - primera).coerceAtLeast(1L)
        val puntos = pesos.map { peso ->
            val x = if (pesos.size == 1) {
                ancho / 2f
            } else {
                ((peso.fecha.toEpochDay() - primera).toFloat() / rangoDias) * ancho
            }
            Offset(x, yDe(peso.pesoKg))
        }

        if (puntos.size > 1) {
            val linea = Path().apply {
                moveTo(puntos.first().x, puntos.first().y)
                puntos.drop(1).forEach { punto -> lineTo(punto.x, punto.y) }
            }
            val relleno = Path().apply {
                addPath(linea)
                lineTo(puntos.last().x, alto)
                lineTo(puntos.first().x, alto)
                close()
            }
            drawPath(
                path = relleno,
                brush = Brush.verticalGradient(
                    colors = listOf(colorRelleno.copy(alpha = 0.16f), Color.Transparent),
                    startY = 0f,
                    endY = alto,
                ),
            )
            drawPath(
                path = linea,
                color = colorLinea,
                style = Stroke(
                    width = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                ),
            )
        }

        puntos.forEachIndexed { indice, punto ->
            drawCircle(
                color = colorLinea,
                radius = if (indice == puntos.lastIndex) 4.dp.toPx() else 2.5.dp.toPx(),
                center = punto,
            )
        }
    }
}

@Composable
private fun FilaPeso(
    peso: Peso,
    anterior: Peso?,
    hoy: LocalDate,
    onClick: () -> Unit,
) {
    val delta = anterior?.let { peso.pesoKg - it.pesoKg }
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    fechaHistorial(peso.fecha, hoy),
                    style = MaterialTheme.typography.titleMedium,
                )
                if (peso.nota.isNotBlank()) {
                    Text(
                        peso.nota,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${formatoPeso(peso.pesoKg)} kg",
                    style = MaterialTheme.typography.titleMedium,
                )
                if (delta != null) {
                    CambioPeso(cambio = delta, compacto = true)
                }
            }
        }
    }
}

@Composable
private fun CambioPeso(cambio: Double, compacto: Boolean = false) {
    val positivo = cambio > 0.05
    val negativo = cambio < -0.05
    val color = when {
        negativo -> MaterialTheme.colorScheme.secondary
        positivo -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val fondo = when {
        negativo -> MaterialTheme.colorScheme.secondaryContainer
        positivo -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Surface(
        shape = CircleShape,
        color = fondo,
        contentColor = color,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (compacto) 6.dp else 9.dp,
                vertical = if (compacto) 2.dp else 4.dp,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            if (positivo) {
                Icon(
                    Icons.Default.KeyboardArrowUp,
                    contentDescription = null,
                    modifier = Modifier.size(if (compacto) 13.dp else 16.dp),
                )
            } else if (negativo) {
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(if (compacto) 13.dp else 16.dp),
                )
            }
            Text(
                textoCambio(cambio),
                style = if (compacto) {
                    MaterialTheme.typography.labelSmall
                } else {
                    MaterialTheme.typography.labelMedium
                },
            )
        }
    }
}
