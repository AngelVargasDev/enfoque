package com.enfoque.app.data.local

import com.enfoque.app.domain.model.Tarea
import com.enfoque.app.domain.model.TareaRecurrente
import java.time.LocalTime

fun TareaEntity.aDominio(): Tarea = Tarea(
    id = id,
    titulo = titulo,
    descripcion = descripcion,
    fechaVencimiento = fechaVencimiento,
    estado = estado,
    categoria = categoria,
    duracionMinutos = duracionMinutos,
    horaRecordatorio = horaRecordatorio?.let { LocalTime.of(it / 60, it % 60) },
    usarPomodoro = usarPomodoro,
    recurrenteId = recurrenteId,
    creadaEn = creadaEn,
)

fun Tarea.aEntidad(): TareaEntity = TareaEntity(
    id = id,
    titulo = titulo,
    descripcion = descripcion,
    fechaVencimiento = fechaVencimiento,
    estado = estado,
    categoria = categoria,
    duracionMinutos = duracionMinutos,
    horaRecordatorio = horaRecordatorio?.let { it.hour * 60 + it.minute },
    usarPomodoro = usarPomodoro,
    recurrenteId = recurrenteId,
    creadaEn = creadaEn,
)

fun TareaRecurrenteEntity.aDominio(): TareaRecurrente = TareaRecurrente(
    id = id,
    titulo = titulo,
    descripcion = descripcion,
    duracionMinutos = duracionMinutos,
    usarPomodoro = usarPomodoro,
    horaRecordatorio = horaRecordatorio?.let { LocalTime.of(it / 60, it % 60) },
    creadaEn = creadaEn,
)

fun TareaRecurrente.aEntidad(): TareaRecurrenteEntity = TareaRecurrenteEntity(
    id = id,
    titulo = titulo,
    descripcion = descripcion,
    duracionMinutos = duracionMinutos,
    horaRecordatorio = horaRecordatorio?.let { it.hour * 60 + it.minute },
    usarPomodoro = usarPomodoro,
    creadaEn = creadaEn,
)
