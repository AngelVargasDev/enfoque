package com.enfoque.app.domain.repository

import com.enfoque.app.domain.model.Peso
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/** Acceso al historial diario de peso. */
interface PesoRepository {

    /** Registros ordenados de más antiguo a más reciente. */
    fun observarPesos(): Flow<List<Peso>>

    suspend fun guardar(peso: Peso)

    suspend fun eliminar(fecha: LocalDate)
}
