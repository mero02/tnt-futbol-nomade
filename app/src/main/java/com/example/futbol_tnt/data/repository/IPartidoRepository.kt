package com.example.futbol_tnt.data.repository

import com.example.futbol_tnt.data.model.Partido
import kotlinx.coroutines.flow.Flow

interface IPartidoRepository {
    // Flow en tiempo real desde Firestore (snapshots).
    val partidos: Flow<List<Partido>>

    // Operaciones suspend porque tocan red (Firestore).
    suspend fun crearPartido(partido: Partido)
    suspend fun unirseAPartido(partidoId: String): Boolean // Mantener por compatibilidad o remover luego
    suspend fun enviarSolicitud(partidoId: String): Boolean
    suspend fun gestionarSolicitud(partidoId: String, applicantId: String, aceptar: Boolean): Boolean
    suspend fun abandonarPartido(partidoId: String): Boolean
    suspend fun getPartidoByReservaId(reservaId: String): Partido?
}
