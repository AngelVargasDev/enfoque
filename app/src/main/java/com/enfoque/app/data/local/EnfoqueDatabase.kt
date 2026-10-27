package com.enfoque.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        TareaEntity::class,
        TareaRecurrenteEntity::class,
        DiaCompletadoEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
@TypeConverters(Convertidores::class)
abstract class EnfoqueDatabase : RoomDatabase() {

    abstract fun tareaDao(): TareaDao

    abstract fun tareaRecurrenteDao(): TareaRecurrenteDao

    abstract fun rachaDao(): RachaDao

    companion object {
        private const val NOMBRE = "enfoque.db"

        /**
         * Añade el vínculo con rutinas y la hora de recordatorio por tarea.
         * Las tareas existentes conservan el aviso de las 09:00.
         */
        val MIGRACION_1_2 = object : Migration(1, 2) {
            override fun migrate(baseDeDatos: SupportSQLiteDatabase) {
                baseDeDatos.execSQL("ALTER TABLE tareas ADD COLUMN horaRecordatorio INTEGER DEFAULT 540")
                baseDeDatos.execSQL("ALTER TABLE tareas ADD COLUMN recurrenteId INTEGER DEFAULT NULL")
                baseDeDatos.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_tareas_recurrenteId` ON `tareas` (`recurrenteId`)",
                )
                baseDeDatos.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `tareas_recurrentes` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `titulo` TEXT NOT NULL,
                        `descripcion` TEXT NOT NULL,
                        `duracionMinutos` INTEGER,
                        `horaRecordatorio` INTEGER DEFAULT 540,
                        `creadaEn` INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
            }
        }

        /** Añade el modo Pomodoro por tarea y la tabla de días para la racha. */
        val MIGRACION_2_3 = object : Migration(2, 3) {
            override fun migrate(baseDeDatos: SupportSQLiteDatabase) {
                baseDeDatos.execSQL("ALTER TABLE tareas ADD COLUMN usarPomodoro INTEGER NOT NULL DEFAULT 0")
                baseDeDatos.execSQL(
                    "ALTER TABLE tareas_recurrentes ADD COLUMN usarPomodoro INTEGER NOT NULL DEFAULT 0",
                )
                baseDeDatos.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `dias_completados` (
                        `fecha` INTEGER NOT NULL,
                        `creadaEn` INTEGER NOT NULL,
                        PRIMARY KEY(`fecha`)
                    )
                    """.trimIndent(),
                )
            }
        }

        fun construir(context: Context): EnfoqueDatabase =
            Room.databaseBuilder(context, EnfoqueDatabase::class.java, NOMBRE)
                .addMigrations(MIGRACION_1_2, MIGRACION_2_3)
                .build()
    }
}
