package com.example.futbol_tnt.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futbol_tnt.data.model.Partido
import com.example.futbol_tnt.data.repository.IPartidoRepository
import com.example.futbol_tnt.data.repository.PartidoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class PartidoEvento {
    data object Idle : PartidoEvento()
    data object UnirseExito : PartidoEvento()
    data object PartidoLleno : PartidoEvento()
    data object PartidoCreadoExito : PartidoEvento()
    data class Error(val mensaje: String) : PartidoEvento()
}

class PartidoViewModel(
    private val repository: IPartidoRepository = PartidoRepository(),
) : ViewModel() {

    // El repositorio expone un Flow (snapshots de Firestore en vivo). Lo convertimos
    // a StateFlow con stateIn para que la UI pueda hacer .collectAsState() y reciba
    // un valor inicial (lista vacia) hasta que llegue el primer snapshot.
    val partidos: StateFlow<List<Partido>> = repository.partidos
        .catch { error ->
            _evento.value = PartidoEvento.Error(
                error.message ?: "No se pudieron cargar los partidos",
            )
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = emptyList(),
        )

    private val _evento = MutableStateFlow<PartidoEvento>(PartidoEvento.Idle)
    val evento: StateFlow<PartidoEvento> = _evento.asStateFlow()

    fun unirseAPartido(partidoId: String) {
        viewModelScope.launch {
            _evento.value = runCatching {
                val exito = repository.unirseAPartido(partidoId)
                if (exito) PartidoEvento.UnirseExito else PartidoEvento.PartidoLleno
            }.getOrElse { error ->
                PartidoEvento.Error(error.message ?: "Error al unirse al partido")
            }
        }
    }

    fun crearPartido(partido: Partido) {
        viewModelScope.launch {
            _evento.value = runCatching {
                repository.crearPartido(partido)
                PartidoEvento.PartidoCreadoExito
            }.getOrElse { error ->
                PartidoEvento.Error(error.message ?: "Error al crear el partido")
            }
        }
    }

    suspend fun getPartidoByReservaId(reservaId: String): Partido? {
        return try {
            repository.getPartidoByReservaId(reservaId)
        } catch (e: Exception) {
            null
        }
    }

    fun limpiarEvento() {
        _evento.value = PartidoEvento.Idle
    }

    private companion object {
        // Mantiene el snapshot listener activo 5s despues de que la UI deja de
        // observar — evita reconectar si el usuario rota la pantalla.
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
