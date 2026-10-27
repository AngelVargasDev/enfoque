package com.enfoque.app.data.repository

import com.enfoque.app.data.local.DiaCompletadoEntity
import com.enfoque.app.data.local.RachaDao
import com.enfoque.app.domain.repository.RachaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.LocalDate

class RachaRepositoryImpl(
    private val dao: RachaDao,
    private val reloj: Clock = Clock.systemDefaultZone(),
) : RachaRepository {

    override fun observarDias(): Flow<List<LocalDate>> =
        dao.observarDias().map { dias -> dias.map { it.fecha } }

    override suspend fun registrarHoy() {
        dao.registrar(DiaCompletadoEntity(fecha = LocalDate.now(reloj)))
    }
}
