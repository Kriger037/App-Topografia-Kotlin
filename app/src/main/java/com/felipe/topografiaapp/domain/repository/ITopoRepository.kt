package com.felipe.topografiaapp.domain.repository

import com.felipe.topografiaapp.domain.model.Cancha
import com.felipe.topografiaapp.domain.model.Fundo
import com.felipe.topografiaapp.domain.model.PR
import kotlinx.coroutines.flow.Flow


interface ITopoRepository {

    // --- Fundos ---
    fun obtenerFundos(): Flow<List<Fundo>>
    suspend fun sincronizarFundos()

    // --- Canchas ---
    fun obtenerCanchasPorFundo(codigoFundo: String): Flow<List<Cancha>>
    suspend fun sincronizarCanchasPorFundo(codigoFundo: String)

    // --- PRs ---
    fun obtenerPRsPorCancha(canchaId: Int): Flow<List<PR>>
    suspend fun sincronizarPRsPorCancha(canchaId: Int)

    // --- Persistencia local (importación offline) ---
    suspend fun guardarPRsLocalmente(prs: List<PR>, canchaId: Int)
    suspend fun obtenerPRsPendientesSincronizacion(): List<PR>
    suspend fun marcarPRsComoSincronizados(canchaId: Int)

    // Eliminación local (marcado pendiente para sincronizar)
    suspend fun eliminarFundoLocalmente(codigoFundo: String)
    suspend fun eliminarCanchaLocalmente(canchaId: Int)
    suspend fun eliminarPRLocalmente(prId: Int)

    // Eliminación en servidor
    suspend fun sincronizarEliminaciones()
}
