package com.felipe.topografiaapp

import android.content.Context
import com.felipe.topografiaapp.data.local.entity.CanchaEntity
import com.felipe.topografiaapp.data.local.entity.FundoEntity
import com.felipe.topografiaapp.data.local.entity.PREntity

class LocalDataManager(context: Context) {

    private val db = AppDatabase.getDatabase(context)

    suspend fun guardarFundos(lista: List<FundoEntity>) {
        db.fundoDao().insertarFundos(lista)
    }

    suspend fun leerFundos(): List<FundoEntity> {
        return db.fundoDao().obtenerTodos()
    }

    suspend fun guardarCanchasPorFundo(codigoFundo: String, lista: List<CanchaEntity>) {
        db.canchaDao().insertarCanchas(lista)
    }

    suspend fun leerCanchasPorFundo(codigoFundo: String): List<CanchaEntity> {
        return db.canchaDao().obtenerPorFundo(codigoFundo)
    }

    suspend fun guardarPRsPorCancha(idCancha: Int, lista: List<PREntity>) {
        db.prDao().insertarPRs(lista)
    }

    suspend fun leerPRsPorCancha(idCancha: Int): List<PREntity> {
        return db.prDao().obtenerPorCancha(idCancha)
    }
}