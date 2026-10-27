package com.enfoque.app.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.enfoque.app.domain.model.CategoriaTarea
import com.enfoque.app.domain.model.EstadoTarea
import java.time.LocalDate

@Entity(
    tableName = "tareas",
    indices = [Index("categoria"), Index("estado"), Index("recurrenteId")],
)
data class TareaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val titulo: String,
    val descripcion: String,
    val fechaVencimiento: LocalDate,
    val estado: EstadoTarea,
    val categoria: CategoriaTarea,
    val duracionMinutos: Int?,
    /** Minutos desde medianoche (0..1439). Null desactiva el recordatorio. */
    @ColumnInfo(defaultValue = "540")
    val horaRecordatorio: Int?,
    @ColumnInfo(defaultValue = "0")
    val usarPomodoro: Boolean = false,
    @ColumnInfo(defaultValue = "NULL")
    val recurrenteId: Long?,
    val creadaEn: Long,
)
