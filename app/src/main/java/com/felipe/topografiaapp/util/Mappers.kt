package com.felipe.topografiaapp.util

import com.felipe.topografiaapp.data.local.entity.CanchaEntity
import com.felipe.topografiaapp.data.local.entity.FundoEntity
import com.felipe.topografiaapp.data.local.entity.PREntity
import com.felipe.topografiaapp.data.remote.dto.CanchaDto
import com.felipe.topografiaapp.data.remote.dto.FundoDto
import com.felipe.topografiaapp.data.remote.dto.PRDto
import com.felipe.topografiaapp.domain.model.Cancha
import com.felipe.topografiaapp.domain.model.Fundo
import com.felipe.topografiaapp.domain.model.PR

// ---------------------------------------------------------------------------
// Mappers — funciones de extensión para convertir entre las tres capas:
//   DTO (API) ↔ Entity (Room) ↔ DomainModel (negocio/UI)
//
// Mantener estos en un archivo separado facilita testearlos de forma unitaria
// sin necesidad de Android SDK.
// ---------------------------------------------------------------------------

// ---- FundoDto → FundoEntity (guardar en Room lo que viene del servidor) ----
fun FundoDto.toEntity(): FundoEntity = FundoEntity(
    id = id,
    codigoFundo = codigo_fundo,
    nombreFundo = nombre_fundo,
    comuna = comuna,
    lastSyncAt = System.currentTimeMillis()
)

// ---- FundoEntity → Fundo (dominio) para exponer a ViewModels ----
fun FundoEntity.toDomain(): Fundo = Fundo(
    id = id,
    codigoFundo = codigoFundo,
    nombreFundo = nombreFundo,
    comuna = comuna
)

// ---- FundoDto → Fundo (directo, sin pasar por Room) ----
fun FundoDto.toDomain(): Fundo = Fundo(
    id = id,
    codigoFundo = codigo_fundo,
    nombreFundo = nombre_fundo,
    comuna = comuna
)

// ---- CanchaDto → CanchaEntity ----
fun CanchaDto.toEntity(huso: String = "18"): CanchaEntity = CanchaEntity(
    id = id,
    codigoFundo = codigo_fundo,
    nombreFundo = nombre_fundo,
    numeroCancha = numero_cancha,
    huso = huso,
    fechaCreacion = fecha_creacion,
    fechaActualizacion = fecha_actualizacion,
    lastSyncAt = System.currentTimeMillis()
)

// ---- CanchaEntity → Cancha (dominio) ----
fun CanchaEntity.toDomain(): Cancha = Cancha(
    id = id,
    codigoFundo = codigoFundo,
    nombreFundo = nombreFundo,
    numeroCancha = numeroCancha,
    huso = huso.toIntOrNull() ?: 18,
    fechaCreacion = fechaCreacion,
    fechaActualizacion = fechaActualizacion
)

// ---- PRDto → PREntity ----
fun PRDto.toEntity(canchaId: Int): PREntity = PREntity(
    id = id,
    canchaId = canchaId,
    descriptor = descriptor,
    norte = norte,
    este = este,
    cota = cota,
    latitud = latitud,
    longitud = longitud,
    fechaCreacion = fecha_creacion,
    fechaModificacion = fecha_modificacion,
    isDirty = false,
    lastSyncAt = System.currentTimeMillis()
)

// ---- PREntity → PR (dominio) ----
fun PREntity.toDomain(): PR = PR(
    id = id,
    canchaId = canchaId,
    descriptor = descriptor,
    norte = norte,
    este = este,
    cota = cota,
    latitud = latitud,
    longitud = longitud,
    fechaCreacion = fechaCreacion,
    fechaModificacion = fechaModificacion,
    isDirty = isDirty
)

// ---- PR (dominio) → PREntity (para guardar PRs importados localmente) ----
fun PR.toEntity(): PREntity = PREntity(
    id = id,
    canchaId = canchaId,
    descriptor = descriptor,
    norte = norte,
    este = este,
    cota = cota,
    latitud = latitud,
    longitud = longitud,
    fechaCreacion = fechaCreacion,
    fechaModificacion = fechaModificacion,
    isDirty = isDirty,
    lastSyncAt = System.currentTimeMillis()
)
