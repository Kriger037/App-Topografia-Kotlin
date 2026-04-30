package com.felipe.topografiaapp

import android.content.Context

class LocalDataManager(context: Context) {

    private val db = AppDatabase.getDatabase(context)

    suspend fun guardarFundos(lista: List<Fundo>) {
        db.fundoDao().insertarFundos(lista)
    }

    suspend fun leerFundos(): List<Fundo> {
        return db.fundoDao().obtenerTodosLosFundos()
    }

    suspend fun guardarCanchasPorFundo(codigoFundo: String, lista: List<Cancha>) {
        db.canchaDao().insertarCanchas(lista)
    }

    suspend fun leerCanchasPorFundo(codigoFundo: String): List<Cancha> {
        return db.canchaDao().obtenerCanchasPorFundo(codigoFundo)
    }

    suspend fun guardarPRsPorCancha(idCancha: Int, lista: List<PR>) {
        lista.forEach { it.cancha_id = idCancha }

        db.prDao().insertarPRs(lista)
    }

    suspend fun leerPRsPorCancha(idCancha: Int): List<PR> {
        return db.prDao().obtenerPRsPorCancha(idCancha)
    }
}