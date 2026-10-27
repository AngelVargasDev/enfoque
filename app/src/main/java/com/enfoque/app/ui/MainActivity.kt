package com.enfoque.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.enfoque.app.EnfoqueApplication
import com.enfoque.app.domain.model.TemaApp
import com.enfoque.app.ui.configuracion.ConfiguracionScreen
import com.enfoque.app.ui.panel.PanelScreen
import com.enfoque.app.ui.panel.PanelViewModel
import com.enfoque.app.ui.theme.EnfoqueTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val contenedor = (application as EnfoqueApplication).contenedor

        setContent {
            val ajustes by contenedor.preferencias.ajustes.collectAsStateWithLifecycle()
            var mostrarConfiguracion by rememberSaveable { mutableStateOf(false) }
            val oscuro = when (ajustes.tema) {
                TemaApp.SISTEMA -> isSystemInDarkTheme()
                TemaApp.CLARO -> false
                TemaApp.OSCURO -> true
            }

            SideEffect {
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !oscuro
                    isAppearanceLightNavigationBars = !oscuro
                }
            }

            EnfoqueTheme(
                modoDiseno = ajustes.modoDiseno,
                tema = ajustes.tema,
            ) {
                PedirPermisoNotificaciones()
                val viewModel: PanelViewModel = viewModel(
                    factory = PanelViewModel.fabrica(contenedor),
                )

                if (mostrarConfiguracion) {
                    BackHandler { mostrarConfiguracion = false }
                    ConfiguracionScreen(
                        preferencias = contenedor.preferencias,
                        onCerrar = { mostrarConfiguracion = false },
                    )
                } else {
                    PanelScreen(
                        viewModel = viewModel,
                        onAbrirConfiguracion = { mostrarConfiguracion = true },
                    )
                }
            }
        }
    }
}

/**
 * Android 13+ exige permiso explícito para mostrar recordatorios y el aviso de fin
 * de temporizador. Se solicita una sola vez al abrir la app.
 */
@Composable
private fun PedirPermisoNotificaciones() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val contexto = LocalContext.current
    val lanzador = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { }

    LaunchedEffect(Unit) {
        val concedido = ContextCompat.checkSelfPermission(
            contexto,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (!concedido) {
            lanzador.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
