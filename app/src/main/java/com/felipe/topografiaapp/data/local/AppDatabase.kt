package com.felipe.topografiaapp.data.local

import android.content.Context
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

@Database(
    entities = [FundoEntity::class, CanchaEntity::class, PREntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun fundoDao(): FundoDao
    abstract fun canchaDao(): CanchaDao
    abstract fun prDao(): PRDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE tabla_fundos ADD COLUMN lastSyncAt INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE tabla_canchas ADD COLUMN huso INTEGER NOT NULL DEFAULT 18")
                database.execSQL("ALTER TABLE tabla_canchas ADD COLUMN lastSyncAt INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE tabla_prs ADD COLUMN isDirty INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE tabla_prs ADD COLUMN lastSyncAt INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE tabla_prs_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL DEFAULT 0,
                        canchaId INTEGER NOT NULL,
                        descriptor TEXT NOT NULL,
                        norte REAL NOT NULL,
                        este REAL NOT NULL,
                        cota REAL NOT NULL,
                        latitud REAL,
                        longitud REAL,
                        fechaCreacion TEXT NOT NULL,
                        fechaModificacion TEXT NOT NULL,
                        isDirty INTEGER NOT NULL DEFAULT 0,
                        lastSyncAt INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
                database.execSQL("""
                    INSERT INTO tabla_prs_new
                    SELECT id, canchaId, descriptor, norte, este, cota, latitud, longitud,
                           fechaCreacion, fechaModificacion, isDirty, lastSyncAt
                    FROM tabla_prs
                """.trimIndent())
                database.execSQL("DROP TABLE tabla_prs")
                database.execSQL("ALTER TABLE tabla_prs_new RENAME TO tabla_prs")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE tabla_prs_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL DEFAULT 0,
                        canchaId INTEGER NOT NULL,
                        descriptor TEXT NOT NULL,
                        norte REAL NOT NULL,
                        este REAL NOT NULL,
                        cota REAL NOT NULL,
                        latitud REAL,
                        longitud REAL,
                        fechaCreacion TEXT NOT NULL,
                        fechaModificacion TEXT NOT NULL,
                        isDirty INTEGER NOT NULL DEFAULT 0,
                        lastSyncAt INTEGER NOT NULL DEFAULT 0,
                        UNIQUE(canchaId, descriptor)
                    )
                """.trimIndent())
                database.execSQL("""
                    INSERT OR IGNORE INTO tabla_prs_new
                    SELECT id, canchaId, descriptor, norte, este, cota, latitud, longitud,
                           fechaCreacion, fechaModificacion, isDirty, lastSyncAt
                    FROM tabla_prs
                """.trimIndent())
                database.execSQL("DROP TABLE tabla_prs")
                database.execSQL("ALTER TABLE tabla_prs_new RENAME TO tabla_prs")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "topografia_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}