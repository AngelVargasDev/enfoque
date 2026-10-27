package com.enfoque.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val base = Typography()

/**
 * Tipografía sobria: pocos pesos y títulos contenidos para que la jerarquía la
 * marque el espacio, no el color.
 */
val Tipografia = Typography(
    headlineMedium = base.headlineMedium.copy(
        fontWeight = FontWeight.Medium,
        letterSpacing = (-0.8).sp,
    ),
    headlineSmall = base.headlineSmall.copy(
        fontWeight = FontWeight.Medium,
        letterSpacing = (-0.4).sp,
    ),
    titleLarge = base.titleLarge.copy(fontWeight = FontWeight.Medium),
    titleMedium = base.titleMedium.copy(fontWeight = FontWeight.Medium),
    bodyLarge = base.bodyLarge.copy(lineHeight = 23.sp),
    bodyMedium = base.bodyMedium.copy(lineHeight = 21.sp),
    labelLarge = base.labelLarge.copy(fontWeight = FontWeight.Medium),
    labelMedium = base.labelMedium.copy(letterSpacing = 0.3.sp),
    labelSmall = base.labelSmall.copy(
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.7.sp,
    ),
)
