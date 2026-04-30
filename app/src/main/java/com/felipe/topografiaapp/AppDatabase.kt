package com.felipe.topografiaapp

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Se definen las tablas (entities) y la version de la base de datos (1)
@Database(entities = [Fundo::class, Cancha::class, PR::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun fundoDao() : FundoDao
    abstract fun canchaDao() : CanchaDao
    abstract fun prDao() : PRDao

    // Patrón Singleton para evitar crear multiples conexiones
    companion object{
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase{
            // Devuelve la instancia si estaba creada, de lo contrario crea una nueva
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "topografia_database" // nombre del archivo .db dentro del celular
                ).build()

                INSTANCE = instance
                instance
            }
        }
    }
}