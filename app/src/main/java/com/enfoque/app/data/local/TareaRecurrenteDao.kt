package com.enfoque.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TareaRecurrenteDao {

    @Query("SELECT * FROM tareas_recurrentes ORDER BY creadaEn ASC")
    fun observarTodas(): Flow<List<TareaRecurrenteEntity>>

    @Query("SELECT * FROM tareas_recurrentes WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Long): TareaRecurrenteEntity?

    @Insert
    suspend fun insertar(rutina: TareaRecurrenteEntity): Long

    @Update
    suspend fun actualizar(rutina: TareaRecurrenteEntity)

    @Query("DELETE FROM tareas_recurrentes WHERE id = :id")
    suspend fun eliminar(id: Long)
}
