package com.example.futbol_tnt.data.repository

import com.example.futbol_tnt.data.model.EstadoPartido
import com.example.futbol_tnt.data.model.MockData
import com.example.futbol_tnt.data.model.Partido
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PartidoRepository : IPartidoRepository {

    private val _partidos = MutableStateFlow<List<Partido>>(MockData.partidos)
    override val partidos: StateFlow<List<Partido>> = _partidos.asStateFlow()

    override fun unirseAPartido(partidoId: String): Boolean {
        val lista = _partidos.value.toMutableList()
        val idx = lista.indexOfFirst { it.id == partidoId }
        if (idx == -1) return false
        val partido = lista[idx]
        if (partido.jugadoresActuales >= partido.jugadoresMaximos) return false
        val nuevos = partido.jugadoresActuales + 1
        lista[idx] = partido.copy(
            jugadoresActuales = nuevos,
            estado = if (nuevos >= partido.jugadoresMaximos) EstadoPartido.LLENO else partido.estado
        )
        _partidos.value = lista
        return true
    }

    override fun crearPartido(partido: Partido) {
        _partidos.value = _partidos.value + partido
    }
}
