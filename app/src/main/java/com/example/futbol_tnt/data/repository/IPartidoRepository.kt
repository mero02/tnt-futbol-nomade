package com.example.futbol_tnt.data.repository

import com.example.futbol_tnt.data.model.Partido
import kotlinx.coroutines.flow.Flow

interface IPartidoRepository {
    // Flow en tiempo real desde Firestore (snapshots).
    val partidos: Flow<List<Partido>>

    // Operaciones suspend porque tocan red (Firestore).
    suspend fun crearPartido(partido: Partido)
    suspend fun unirseAPartido(partidoId: String): Boolean
}
