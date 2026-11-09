package com.enfoque.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface PesoDao {

    @Query("SELECT * FROM pesos ORDER BY fecha ASC")
    fun observarTodos(): Flow<List<PesoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(peso: PesoEntity)

    @Query("DELETE FROM pesos WHERE fecha = :fecha")
    suspend fun eliminar(fecha: LocalDate)
}
