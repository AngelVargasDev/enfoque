package com.enfoque.app.ui.util

import java.util.Locale

/** Filtra la entrada para aceptar solo un número con un separador decimal. */
fun filtrarEntradaPeso(valor: String): String {
    val limpio = valor.replace(',', '.')
    val salida = StringBuilder()
    var separador = false
    for (caracter in limpio) {
        when {
            caracter.isDigit() -> salida.append(caracter)
            (caracter == '.' || caracter == ',') && !separador -> {
                salida.append('.')
                separador = true
            }
        }
    }
    return salida.toString().take(6)
}

fun parsearPeso(valor: String): Double? =
    valor.replace(',', '.').trim().toDoubleOrNull()

/** Peso con un decimal y punto, por ejemplo 72.4. */
fun formatoPeso(kg: Double): String = String.format(Locale.US, "%.1f", kg)

/** Cambio con signo, por ejemplo +0.3 o -0.3. */
fun formatoCambio(kg: Double): String = String.format(Locale.US, "%+.1f", kg)

fun textoCambio(kg: Double): String = "${formatoCambio(kg)} kg"
