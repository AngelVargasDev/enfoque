package com.enfoque.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.enfoque.app.domain.model.ModoDiseno
import com.enfoque.app.domain.model.TemaApp

@Composable
fun EnfoqueTheme(
    modoDiseno: ModoDiseno = ModoDiseno.MINIMALISTA,
    tema: TemaApp = TemaApp.SISTEMA,
    content: @Composable () -> Unit,
) {
    val oscuro = when (tema) {
        TemaApp.SISTEMA -> isSystemInDarkTheme()
        TemaApp.CLARO -> false
        TemaApp.OSCURO -> true
    }

    MaterialTheme(
        colorScheme = esquemaColor(modoDiseno, oscuro),
        typography = Tipografia,
        content = content,
    )
}
