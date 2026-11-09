package com.enfoque.app.data.repository

import com.enfoque.app.data.local.PesoDao
import com.enfoque.app.data.local.aDominio
import com.enfoque.app.data.local.aEntidad
import com.enfoque.app.domain.model.Peso
import com.enfoque.app.domain.repository.PesoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class PesoRepositoryImpl(
    private val dao: PesoDao,
) : PesoRepository {

    override fun observarPesos(): Flow<List<Peso>> =
        dao.observarTodos().map { pesos -> pesos.map { it.aDominio() } }

    override suspend fun guardar(peso: Peso) {
        dao.guardar(peso.aEntidad())
    }

    override suspend fun eliminar(fecha: LocalDate) {
        dao.eliminar(fecha)
    }
}
