package com.enfoque.app.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tareas_recurrentes")
data class TareaRecurrenteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val titulo: String,
    val descripcion: String,
    val duracionMinutos: Int?,
    /** Minutos desde medianoche (0..1439). Null desactiva el recordatorio. */
    @ColumnInfo(defaultValue = "540")
    val horaRecordatorio: Int?,
    val creadaEn: Long,
)
