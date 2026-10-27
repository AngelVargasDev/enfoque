package com.enfoque.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * Un día en el que el usuario completó al menos una tarea. La fecha es la clave:
 * si se completa otra tarea el mismo día, el registro se ignora.
 */
@Entity(tableName = "dias_completados")
data class DiaCompletadoEntity(
    @PrimaryKey
    val fecha: LocalDate,
    val creadaEn: Long = System.currentTimeMillis(),
)
