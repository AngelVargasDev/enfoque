package com.enfoque.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EsquemaClaro = lightColorScheme(
    primary = Tinta,
    onPrimary = Color.White,
    primaryContainer = SalviaSuave,
    onPrimaryContainer = Color(0xFF17281C),
    secondary = Salvia,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEDF1EC),
    onSecondaryContainer = Color(0xFF1F2E24),
    tertiary = Color(0xFF6B6252),
    onTertiary = Color.White,
    tertiaryContainer = Arena,
    onTertiaryContainer = Color(0xFF2E2A22),
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    background = Papel,
    onBackground = Tinta,
    surface = Superficie,
    onSurface = Tinta,
    surfaceVariant = Color(0xFFEEEEEA),
    onSurfaceVariant = TintaSuave,
    outline = Color(0xFFB9B9B2),
    outlineVariant = Linea,
    inverseSurface = Color(0xFF2C2C2A),
    inverseOnSurface = Color(0xFFF3F3EF),
    inversePrimary = SalviaOscura,
    surfaceTint = Tinta,
)

private val EsquemaOscuro = darkColorScheme(
    primary = Hueso,
    onPrimary = Color(0xFF1A1A18),
    primaryContainer = SalviaProfunda,
    onPrimaryContainer = Color(0xFFD7E5DA),
    secondary = SalviaOscura,
    onSecondary = Color(0xFF1F2E24),
    secondaryContainer = Color(0xFF2A362D),
    onSecondaryContainer = Color(0xFFD7E5DA),
    tertiary = Color(0xFFCFC5B2),
    onTertiary = Color(0xFF2E2A22),
    tertiaryContainer = Color(0xFF3A352C),
    onTertiaryContainer = Color(0xFFEDE5D6),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Noche,
    onBackground = Hueso,
    surface = NocheSuperficie,
    onSurface = Hueso,
    surfaceVariant = Color(0xFF252723),
    onSurfaceVariant = HuesoSuave,
    outline = Color(0xFF5C5F58),
    outlineVariant = LineaOscura,
    inverseSurface = Hueso,
    inverseOnSurface = Color(0xFF2C2C2A),
    inversePrimary = Salvia,
    surfaceTint = Hueso,
)

@Composable
fun EnfoqueTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) EsquemaOscuro else EsquemaClaro,
        typography = Tipografia,
        content = content,
    )
}
