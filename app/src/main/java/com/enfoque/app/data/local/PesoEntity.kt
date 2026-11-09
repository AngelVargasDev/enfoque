package com.enfoque.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/** Un peso por día; volver a guardar la misma fecha actualiza el registro. */
@Entity(tableName = "pesos")
data class PesoEntity(
    @PrimaryKey
    val fecha: LocalDate,
    val pesoKg: Double,
    val nota: String,
    val creadoEn: Long,
)
