package com.enfoque.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.enfoque.app.domain.model.ModoDiseno

/** Colores base de una paleta; el resto del esquema se deriva de aquí. */
data class PaletaDiseno(
    val primario: Color,
    val sobrePrimario: Color,
    val primarioContenedor: Color,
    val sobrePrimarioContenedor: Color,
    val secundario: Color,
    val sobreSecundario: Color,
    val secundarioContenedor: Color,
    val sobreSecundarioContenedor: Color,
    val fondo: Color,
    val sobreFondo: Color,
    val superficie: Color,
    val sobreSuperficie: Color,
    val superficieVariante: Color,
    val sobreSuperficieVariante: Color,
    val linea: Color,
    val lineaSuave: Color,
)

private val minimalistaClaro = PaletaDiseno(
    primario = Color(0xFF17171A),
    sobrePrimario = Color.White,
    primarioContenedor = Color(0xFFE8EFE9),
    sobrePrimarioContenedor = Color(0xFF17281C),
    secundario = Color(0xFF46644F),
    sobreSecundario = Color.White,
    secundarioContenedor = Color(0xFFEDF1EC),
    sobreSecundarioContenedor = Color(0xFF1F2E24),
    fondo = Color(0xFFF7F7F4),
    sobreFondo = Color(0xFF17171A),
    superficie = Color.White,
    sobreSuperficie = Color(0xFF17171A),
    superficieVariante = Color(0xFFEEEEEA),
    sobreSuperficieVariante = Color(0xFF5C5C61),
    linea = Color(0xFFB9B9B2),
    lineaSuave = Color(0xFFE4E4DE),
)

private val minimalistaOscuro = PaletaDiseno(
    primario = Color(0xFFE9E9E4),
    sobrePrimario = Color(0xFF1A1A18),
    primarioContenedor = Color(0xFF2B382F),
    sobrePrimarioContenedor = Color(0xFFD7E5DA),
    secundario = Color(0xFFA9BFAE),
    sobreSecundario = Color(0xFF1F2E24),
    secundarioContenedor = Color(0xFF2A362D),
    sobreSecundarioContenedor = Color(0xFFD7E5DA),
    fondo = Color(0xFF101110),
    sobreFondo = Color(0xFFE9E9E4),
    superficie = Color(0xFF171917),
    sobreSuperficie = Color(0xFFE9E9E4),
    superficieVariante = Color(0xFF252723),
    sobreSuperficieVariante = Color(0xFFB4B4AE),
    linea = Color(0xFF5C5F58),
    lineaSuave = Color(0xFF33352F),
)

private val bosqueClaro = PaletaDiseno(
    primario = Color(0xFF1F5A44),
    sobrePrimario = Color.White,
    primarioContenedor = Color(0xFFB8EBD2),
    sobrePrimarioContenedor = Color(0xFF002114),
    secundario = Color(0xFF4C6358),
    sobreSecundario = Color.White,
    secundarioContenedor = Color(0xFFCEE9DA),
    sobreSecundarioContenedor = Color(0xFF092016),
    fondo = Color(0xFFF6FBF6),
    sobreFondo = Color(0xFF171D19),
    superficie = Color.White,
    sobreSuperficie = Color(0xFF171D19),
    superficieVariante = Color(0xFFDBE5DD),
    sobreSuperficieVariante = Color(0xFF404943),
    linea = Color(0xFF707973),
    lineaSuave = Color(0xFFBFC9C1),
)

private val bosqueOscuro = PaletaDiseno(
    primario = Color(0xFF9CD1B9),
    sobrePrimario = Color(0xFF003825),
    primarioContenedor = Color(0xFF00513A),
    sobrePrimarioContenedor = Color(0xFFB8EBD2),
    secundario = Color(0xFFB3CCBE),
    sobreSecundario = Color(0xFF1F352B),
    secundarioContenedor = Color(0xFF354B40),
    sobreSecundarioContenedor = Color(0xFFCEE9DA),
    fondo = Color(0xFF0F1512),
    sobreFondo = Color(0xFFDEE4DE),
    superficie = Color(0xFF141B17),
    sobreSuperficie = Color(0xFFDEE4DE),
    superficieVariante = Color(0xFF404943),
    sobreSuperficieVariante = Color(0xFFBFC9C1),
    linea = Color(0xFF89938C),
    lineaSuave = Color(0xFF404943),
)

private val oceanoClaro = PaletaDiseno(
    primario = Color(0xFF1D4E5F),
    sobrePrimario = Color.White,
    primarioContenedor = Color(0xFFC1E8FB),
    sobrePrimarioContenedor = Color(0xFF001F29),
    secundario = Color(0xFF3D6374),
    sobreSecundario = Color.White,
    secundarioContenedor = Color(0xFFD6E7EE),
    sobreSecundarioContenedor = Color(0xFF0A2530),
    fondo = Color(0xFFF4F8FA),
    sobreFondo = Color(0xFF111A1D),
    superficie = Color.White,
    sobreSuperficie = Color(0xFF111A1D),
    superficieVariante = Color(0xFFDDE8EC),
    sobreSuperficieVariante = Color(0xFF3E4C52),
    linea = Color(0xFF7A8C94),
    lineaSuave = Color(0xFFC5D6DD),
)

private val oceanoOscuro = PaletaDiseno(
    primario = Color(0xFF9BD0E4),
    sobrePrimario = Color(0xFF003543),
    primarioContenedor = Color(0xFF1F4A5A),
    sobrePrimarioContenedor = Color(0xFFC1E8FB),
    secundario = Color(0xFFA5CCDF),
    sobreSecundario = Color(0xFF073543),
    secundarioContenedor = Color(0xFF24495A),
    sobreSecundarioContenedor = Color(0xFFD6E7EE),
    fondo = Color(0xFF0D1417),
    sobreFondo = Color(0xFFDEE7EA),
    superficie = Color(0xFF131C20),
    sobreSuperficie = Color(0xFFDEE7EA),
    superficieVariante = Color(0xFF33454C),
    sobreSuperficieVariante = Color(0xFFB9C9CF),
    linea = Color(0xFF71858D),
    lineaSuave = Color(0xFF33454C),
)

private val atardecerClaro = PaletaDiseno(
    primario = Color(0xFF8A4B2A),
    sobrePrimario = Color.White,
    primarioContenedor = Color(0xFFF6D9C8),
    sobrePrimarioContenedor = Color(0xFF3A1A0C),
    secundario = Color(0xFF7A5B3A),
    sobreSecundario = Color.White,
    secundarioContenedor = Color(0xFFEADCC8),
    sobreSecundarioContenedor = Color(0xFF2B1D0E),
    fondo = Color(0xFFFBF7F2),
    sobreFondo = Color(0xFF1F1712),
    superficie = Color(0xFFFFFDFB),
    sobreSuperficie = Color(0xFF1F1712),
    superficieVariante = Color(0xFFF0E6DB),
    sobreSuperficieVariante = Color(0xFF55483C),
    linea = Color(0xFFA89482),
    lineaSuave = Color(0xFFE4D6C8),
)

private val atardecerOscuro = PaletaDiseno(
    primario = Color(0xFFF0B48F),
    sobrePrimario = Color(0xFF4A1F0A),
    primarioContenedor = Color(0xFF6B3A1F),
    sobrePrimarioContenedor = Color(0xFFF6D9C8),
    secundario = Color(0xFFD9BE9C),
    sobreSecundario = Color(0xFF3B2A17),
    secundarioContenedor = Color(0xFF4E3A24),
    sobreSecundarioContenedor = Color(0xFFEADCC8),
    fondo = Color(0xFF17110D),
    sobreFondo = Color(0xFFEDE2D8),
    superficie = Color(0xFF1E1712),
    sobreSuperficie = Color(0xFFEDE2D8),
    superficieVariante = Color(0xFF40342B),
    sobreSuperficieVariante = Color(0xFFCDBCAD),
    linea = Color(0xFF8A7767),
    lineaSuave = Color(0xFF40342B),
)

private val monocromoClaro = PaletaDiseno(
    primario = Color(0xFF1A1A1A),
    sobrePrimario = Color.White,
    primarioContenedor = Color(0xFFE5E5E5),
    sobrePrimarioContenedor = Color(0xFF111111),
    secundario = Color(0xFF555555),
    sobreSecundario = Color.White,
    secundarioContenedor = Color(0xFFEDEDED),
    sobreSecundarioContenedor = Color(0xFF222222),
    fondo = Color(0xFFFAFAFA),
    sobreFondo = Color(0xFF111111),
    superficie = Color.White,
    sobreSuperficie = Color(0xFF111111),
    superficieVariante = Color(0xFFEFEFEF),
    sobreSuperficieVariante = Color(0xFF555555),
    linea = Color(0xFFBDBDBD),
    lineaSuave = Color(0xFFE8E8E8),
)

private val monocromoOscuro = PaletaDiseno(
    primario = Color(0xFFF2F2F2),
    sobrePrimario = Color(0xFF111111),
    primarioContenedor = Color(0xFF3A3A3A),
    sobrePrimarioContenedor = Color(0xFFEDEDED),
    secundario = Color(0xFFBDBDBD),
    sobreSecundario = Color(0xFF222222),
    secundarioContenedor = Color(0xFF303030),
    sobreSecundarioContenedor = Color(0xFFE8E8E8),
    fondo = Color(0xFF0E0E0E),
    sobreFondo = Color(0xFFEDEDED),
    superficie = Color(0xFF151515),
    sobreSuperficie = Color(0xFFEDEDED),
    superficieVariante = Color(0xFF292929),
    sobreSuperficieVariante = Color(0xFFBDBDBD),
    linea = Color(0xFF6E6E6E),
    lineaSuave = Color(0xFF303030),
)

fun paletaDiseno(modo: ModoDiseno, oscuro: Boolean): PaletaDiseno = when (modo) {
    ModoDiseno.MINIMALISTA -> if (oscuro) minimalistaOscuro else minimalistaClaro
    ModoDiseno.BOSQUE -> if (oscuro) bosqueOscuro else bosqueClaro
    ModoDiseno.OCEANO -> if (oscuro) oceanoOscuro else oceanoClaro
    ModoDiseno.ATARDECER -> if (oscuro) atardecerOscuro else atardecerClaro
    ModoDiseno.MONOCROMO -> if (oscuro) monocromoOscuro else monocromoClaro
}

fun esquemaColor(modo: ModoDiseno, oscuro: Boolean): ColorScheme {
    val p = paletaDiseno(modo, oscuro)
    return if (oscuro) {
        darkColorScheme(
            primary = p.primario,
            onPrimary = p.sobrePrimario,
            primaryContainer = p.primarioContenedor,
            onPrimaryContainer = p.sobrePrimarioContenedor,
            secondary = p.secundario,
            onSecondary = p.sobreSecundario,
            secondaryContainer = p.secundarioContenedor,
            onSecondaryContainer = p.sobreSecundarioContenedor,
            tertiary = p.secundario,
            onTertiary = p.sobreSecundario,
            tertiaryContainer = p.secundarioContenedor,
            onTertiaryContainer = p.sobreSecundarioContenedor,
            error = ErrorOscuro,
            onError = SobreErrorOscuro,
            errorContainer = ErrorContenedorOscuro,
            onErrorContainer = SobreErrorContenedorOscuro,
            background = p.fondo,
            onBackground = p.sobreFondo,
            surface = p.superficie,
            onSurface = p.sobreSuperficie,
            surfaceVariant = p.superficieVariante,
            onSurfaceVariant = p.sobreSuperficieVariante,
            outline = p.linea,
            outlineVariant = p.lineaSuave,
            inverseSurface = p.sobreFondo,
            inverseOnSurface = p.fondo,
            inversePrimary = p.primario,
            surfaceTint = p.primario,
        )
    } else {
        lightColorScheme(
            primary = p.primario,
            onPrimary = p.sobrePrimario,
            primaryContainer = p.primarioContenedor,
            onPrimaryContainer = p.sobrePrimarioContenedor,
            secondary = p.secundario,
            onSecondary = p.sobreSecundario,
            secondaryContainer = p.secundarioContenedor,
            onSecondaryContainer = p.sobreSecundarioContenedor,
            tertiary = p.secundario,
            onTertiary = p.sobreSecundario,
            tertiaryContainer = p.secundarioContenedor,
            onTertiaryContainer = p.sobreSecundarioContenedor,
            error = ErrorClaro,
            onError = SobreErrorClaro,
            errorContainer = ErrorContenedorClaro,
            onErrorContainer = SobreErrorContenedorClaro,
            background = p.fondo,
            onBackground = p.sobreFondo,
            surface = p.superficie,
            onSurface = p.sobreSuperficie,
            surfaceVariant = p.superficieVariante,
            onSurfaceVariant = p.sobreSuperficieVariante,
            outline = p.linea,
            outlineVariant = p.lineaSuave,
            inverseSurface = p.sobreFondo,
            inverseOnSurface = p.fondo,
            inversePrimary = p.primarioContenedor,
            surfaceTint = p.primario,
        )
    }
}
