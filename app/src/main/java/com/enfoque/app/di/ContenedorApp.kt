package com.enfoque.app.di

import android.content.Context
import com.enfoque.app.data.calendario.CalendarioRepositoryNoOp
import com.enfoque.app.data.local.EnfoqueDatabase
import com.enfoque.app.data.recordatorio.RecordatorioProgramador
import com.enfoque.app.data.repository.TareaRepositoryImpl
import com.enfoque.app.data.temporizador.TemporizadorAndroid
import com.enfoque.app.domain.calendario.CalendarioRepository
import com.enfoque.app.domain.repository.TareaRepository
import com.enfoque.app.domain.temporizador.Temporizador

/** Contenedor de dependencias manual, suficiente para esta fase y fácil de migrar a Hilt. */
interface ContenedorApp {
    val tareas: TareaRepository
    val temporizador: Temporizador
    val calendario: CalendarioRepository
}

class ContenedorAppImpl(context: Context) : ContenedorApp {

    private val contexto = context.applicationContext

    private val baseDeDatos by lazy { EnfoqueDatabase.construir(contexto) }
    private val recordatorios by lazy { RecordatorioProgramador(contexto) }

    override val tareas: TareaRepository by lazy {
        TareaRepositoryImpl(
            dao = baseDeDatos.tareaDao(),
            recurrentesDao = baseDeDatos.tareaRecurrenteDao(),
            recordatorios = recordatorios,
        )
    }

    override val temporizador: Temporizador by lazy { TemporizadorAndroid(contexto) }

    /** Sustituir por una implementación de Google Calendar en la siguiente fase. */
    override val calendario: CalendarioRepository = CalendarioRepositoryNoOp()
}
