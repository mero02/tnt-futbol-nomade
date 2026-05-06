package com.example.futbol_tnt.data.repository

import com.example.futbol_tnt.data.model.Partido
import kotlinx.coroutines.flow.StateFlow

interface IPartidoRepository {
    val partidos: StateFlow<List<Partido>>
    fun unirseAPartido(partidoId: String): Boolean
    fun crearPartido(partido: Partido)
}
