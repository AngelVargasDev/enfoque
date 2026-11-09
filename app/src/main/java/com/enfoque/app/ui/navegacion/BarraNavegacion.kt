package com.enfoque.app.ui.navegacion

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

enum class SeccionPrincipal {
    TAREAS,
    PESO,
}

/** Barra inferior compartida por Tareas y Peso. */
@Composable
fun BarraNavegacion(
    actual: SeccionPrincipal,
    onSeleccionar: (SeccionPrincipal) -> Unit,
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
    ) {
        NavigationBarItem(
            selected = actual == SeccionPrincipal.TAREAS,
            onClick = { onSeleccionar(SeccionPrincipal.TAREAS) },
            icon = {
                Icon(Icons.Default.List, contentDescription = null)
            },
            label = { Text("Tareas") },
        )
        NavigationBarItem(
            selected = actual == SeccionPrincipal.PESO,
            onClick = { onSeleccionar(SeccionPrincipal.PESO) },
            icon = {
                Icon(Icons.Default.FavoriteBorder, contentDescription = null)
            },
            label = { Text("Peso") },
        )
    }
}
