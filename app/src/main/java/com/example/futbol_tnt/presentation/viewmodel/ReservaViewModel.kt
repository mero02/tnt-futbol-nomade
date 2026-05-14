package com.example.futbol_tnt.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futbol_tnt.data.model.Reserva
import com.example.futbol_tnt.data.repository.IReservaRepository
import com.example.futbol_tnt.data.repository.ReservaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class MisReservasEvento {
    data object Idle : MisReservasEvento()
    data class Error(val mensaje: String) : MisReservasEvento()
}

class ReservaViewModel(
    private val repository: IReservaRepository = ReservaRepository(),
) : ViewModel() {

    val reservas: StateFlow<List<Reserva>> = repository.reservas
        .catch { error ->
            // Durante el logout es normal recibir PERMISSION_DENIED.
            // Simplemente emitimos lista vacía y evitamos el crash.
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = emptyList(),
        )

    private val _evento = MutableStateFlow<MisReservasEvento>(MisReservasEvento.Idle)
    val evento: StateFlow<MisReservasEvento> = _evento.asStateFlow()

    fun cancelarReserva(id: String) {
        viewModelScope.launch {
            try {
                repository.cancelarReserva(id)
            } catch (e: Exception) {
                _evento.value = MisReservasEvento.Error(e.message ?: "No se pudo cancelar")
            }
        }
    }

    fun pagarSaldo(id: String, monto: Double) {
        viewModelScope.launch {
            try {
                repository.pagarReserva(id, monto)
            } catch (e: Exception) {
                _evento.value = MisReservasEvento.Error(e.message ?: "Error al pagar")
            }
        }
    }

    fun limpiarEvento() {
        _evento.value = MisReservasEvento.Idle
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
