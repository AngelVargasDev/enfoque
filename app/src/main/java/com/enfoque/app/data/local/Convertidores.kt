package com.enfoque.app.data.local

import androidx.room.TypeConverter
import com.enfoque.app.domain.model.CategoriaTarea
import com.enfoque.app.domain.model.EstadoTarea
import java.time.LocalDate

/** Conversores explícitos: la base guarda fechas como epoch day y enums como texto. */
class Convertidores {

    @TypeConverter
    fun fechaAEntero(fecha: LocalDate): Long = fecha.toEpochDay()

    @TypeConverter
    fun enteroAFecha(valor: Long): LocalDate = LocalDate.ofEpochDay(valor)

    @TypeConverter
    fun categoriaATexto(categoria: CategoriaTarea): String = categoria.name

    @TypeConverter
    fun textoACategoria(texto: String): CategoriaTarea = CategoriaTarea.valueOf(texto)

    @TypeConverter
    fun estadoATexto(estado: EstadoTarea): String = estado.name

    @TypeConverter
    fun textoAEstado(texto: String): EstadoTarea = EstadoTarea.valueOf(texto)
}
