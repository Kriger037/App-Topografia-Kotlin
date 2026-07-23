package com.felipe.topografiaapp.domain.usecase

import com.felipe.topografiaapp.domain.model.Cancha
import com.felipe.topografiaapp.domain.model.Fundo
import com.felipe.topografiaapp.domain.model.PR
import com.felipe.topografiaapp.domain.repository.ITopoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObtenerFundosUseCase @Inject constructor(
    private val repository: ITopoRepository
) {
    operator fun invoke(): Flow<List<Fundo>> = repository.obtenerFundos()
    suspend fun sincronizar() = repository.sincronizarFundos()
    suspend fun eliminarLocalmente(codigoFundo: String) = repository.eliminarFundoLocalmente(codigoFundo)
}

class ObtenerCanchasUseCase @Inject constructor(
    private val repository: ITopoRepository
) {
    operator fun invoke(codigoFundo: String): Flow<List<Cancha>> = repository.obtenerCanchasPorFundo(codigoFundo)
    suspend fun sincronizar(codigoFundo: String) = repository.sincronizarCanchasPorFundo(codigoFundo)
    suspend fun eliminarLocalmente(canchaId: Int) = repository.eliminarCanchaLocalmente(canchaId)
}

class ObtenerPRsUseCase @Inject constructor(
    private val repository: ITopoRepository
) {
    operator fun invoke(canchaId: Int): Flow<List<PR>> = repository.obtenerPRsPorCancha(canchaId)
    suspend fun sincronizar(canchaId: Int) = repository.sincronizarPRsPorCancha(canchaId)
    suspend fun eliminarLocalmente(prId: Int) = repository.eliminarPRLocalmente(prId)
}