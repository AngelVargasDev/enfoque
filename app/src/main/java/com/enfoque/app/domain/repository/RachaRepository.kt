package com.enfoque.app.domain.repository

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/** Días en los que el usuario completó al menos una tarea. */
interface RachaRepository {

    fun observarDias(): Flow<List<LocalDate>>

    /** Registra el día actual; si ya existe, no hace nada. */
    suspend fun registrarHoy()
}
