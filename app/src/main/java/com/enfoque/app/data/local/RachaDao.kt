package com.enfoque.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RachaDao {

    @Query("SELECT * FROM dias_completados ORDER BY fecha DESC")
    fun observarDias(): Flow<List<DiaCompletadoEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun registrar(dia: DiaCompletadoEntity)
}
