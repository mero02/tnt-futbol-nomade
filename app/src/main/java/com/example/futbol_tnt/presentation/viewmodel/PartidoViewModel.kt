package com.example.futbol_tnt.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futbol_tnt.data.model.Partido
import com.example.futbol_tnt.data.repository.IPartidoRepository
import com.example.futbol_tnt.data.repository.PartidoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PartidoEvento {
    data object Idle : PartidoEvento()
    data object UnirseExito : PartidoEvento()
    data object PartidoLleno : PartidoEvento()
    data object PartidoCreadoExito : PartidoEvento()
}

class PartidoViewModel(
    private val repository: IPartidoRepository = PartidoRepository()
) : ViewModel() {

    val partidos: StateFlow<List<Partido>> = repository.partidos

    private val _evento = MutableStateFlow<PartidoEvento>(PartidoEvento.Idle)
    val evento: StateFlow<PartidoEvento> = _evento.asStateFlow()

    fun unirseAPartido(partidoId: String) {
        viewModelScope.launch {
            val exito = repository.unirseAPartido(partidoId)
            _evento.value = if (exito) PartidoEvento.UnirseExito else PartidoEvento.PartidoLleno
        }
    }

    fun crearPartido(partido: Partido) {
        viewModelScope.launch {
            repository.crearPartido(partido)
            _evento.value = PartidoEvento.PartidoCreadoExito
        }
    }

    fun limpiarEvento() {
        _evento.value = PartidoEvento.Idle
    }
}
