package com.felipe.topografiaapp.data.local

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.felipe.topografiaapp.data.local.dao.CanchaDao
import com.felipe.topografiaapp.data.local.dao.FundoDao
import com.felipe.topografiaapp.data.local.dao.PRDao
import com.felipe.topografiaapp.data.local.entity.CanchaEntity
import com.felipe.topografiaapp.data.local.entity.FundoEntity
import com.felipe.topografiaapp.data.local.entity.PREntity

// ---------------------------------------------------------------------------
// AppDatabase — versión 2
//
// Cambios desde v1:
//   - FundoEntity  → añade columna last_sync_at (INTEGER, default 0)
//   - CanchaEntity → añade columnas huso (INTEGER, default 18) y last_sync_at
//   - PREntity     → añade columnas is_dirty (INTEGER, default 0) y last_sync_at
//
// La Migration manual es necesaria porque cambiamos nombres de columnas
// (snake_case en BD → camelCase en Kotlin mediante @ColumnInfo si se prefiere,
// o mantenemos snake_case en BD para consistencia con el esquema MySQL existente).
// ---------------------------------------------------------------------------

@Database(
    entities = [FundoEntity::class, CanchaEntity::class, PREntity::class],
    version = 2,
    exportSchema = true    // true para producción: genera JSON de schema en /schemas/
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun fundoDao(): FundoDao
    abstract fun canchaDao(): CanchaDao
    abstract fun prDao(): PRDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migration explícita: agrega las nuevas columnas sin destruir datos existentes
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // FundoEntity: nueva columna last_sync_at
                database.execSQL(
                    "ALTER TABLE tabla_fundos ADD COLUMN lastSyncAt INTEGER NOT NULL DEFAULT 0"
                )

                // CanchaEntity: nuevas columnas huso y last_sync_at
                database.execSQL(
                    "ALTER TABLE tabla_canchas ADD COLUMN huso INTEGER NOT NULL DEFAULT 18"
                )
                database.execSQL(
                    "ALTER TABLE tabla_canchas ADD COLUMN lastSyncAt INTEGER NOT NULL DEFAULT 0"
                )

                // PREntity: nuevas columnas is_dirty y last_sync_at
                database.execSQL(
                    "ALTER TABLE tabla_prs ADD COLUMN isDirty INTEGER NOT NULL DEFAULT 0"
                )
                database.execSQL(
                    "ALTER TABLE tabla_prs ADD COLUMN lastSyncAt INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "topografia_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
