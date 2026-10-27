package com.enfoque.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.enfoque.app.EnfoqueApplication
import com.enfoque.app.ui.panel.PanelScreen
import com.enfoque.app.ui.panel.PanelViewModel
import com.enfoque.app.ui.theme.EnfoqueTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val contenedor = (application as EnfoqueApplication).contenedor

        setContent {
            EnfoqueTheme {
                PedirPermisoNotificaciones()
                val viewModel: PanelViewModel = viewModel(
                    factory = PanelViewModel.fabrica(contenedor),
                )
                PanelScreen(viewModel = viewModel)
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
