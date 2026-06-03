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
    data object SolicitudEnviada : PartidoEvento()
    data object SolicitudGestionada : PartidoEvento()
    data object AbandonarExito : PartidoEvento()
    data object PartidoLleno : PartidoEvento()
    data object PartidoCreadoExito : PartidoEvento()
    data class Error(val mensaje: String) : PartidoEvento()
}

class PartidoViewModel(
    private val repository: IPartidoRepository = PartidoRepository(),
) : ViewModel() {

    val partidos: StateFlow<List<Partido>> = repository.partidos
        .catch { error ->
            if (error.message?.contains("PERMISSION_DENIED") == false) {
                _evento.value = PartidoEvento.Error(
                    error.message ?: "No se pudieron cargar los partidos",
                )
            }
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = emptyList(),
        )

    private val _evento = MutableStateFlow<PartidoEvento>(PartidoEvento.Idle)
    val evento: StateFlow<PartidoEvento> = _evento.asStateFlow()

    fun enviarSolicitud(partidoId: String) {
        viewModelScope.launch {
            _evento.value = runCatching {
                val exito = repository.enviarSolicitud(partidoId)
                if (exito) PartidoEvento.SolicitudEnviada else PartidoEvento.PartidoLleno
            }.getOrElse { error ->
                PartidoEvento.Error(error.message ?: "Error al enviar solicitud")
            }
        }
    }

    fun gestionarSolicitud(partidoId: String, applicantId: String, aceptar: Boolean) {
        viewModelScope.launch {
            _evento.value = runCatching {
                val exito = repository.gestionarSolicitud(partidoId, applicantId, aceptar)
                if (exito) PartidoEvento.SolicitudGestionada else PartidoEvento.PartidoLleno
            }.getOrElse { error ->
                PartidoEvento.Error(error.message ?: "Error al gestionar solicitud")
            }
        }
    }

    fun abandonarPartido(partidoId: String) {
        viewModelScope.launch {
            _evento.value = runCatching {
                val exito = repository.abandonarPartido(partidoId)
                if (exito) PartidoEvento.AbandonarExito else PartidoEvento.Error("No se pudo abandonar el partido")
            }.getOrElse { error ->
                PartidoEvento.Error(error.message ?: "Error al abandonar el partido")
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
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
