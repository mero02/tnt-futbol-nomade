package com.example.futbol_tnt.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futbol_tnt.data.model.Cancha
import com.example.futbol_tnt.data.model.Horario
import com.example.futbol_tnt.data.model.Reserva
import com.example.futbol_tnt.data.model.EstadoReserva
import com.example.futbol_tnt.data.repository.CanchaRepository
import com.example.futbol_tnt.data.repository.IReservaRepository
import com.example.futbol_tnt.data.repository.ReservaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

sealed class ReservaEvento {
    data object Idle : ReservaEvento()
    data class ReservaExitosa(val reserva: Reserva) : ReservaEvento()
    data class Error(val mensaje: String) : ReservaEvento()
}

class CanchaViewModel(
    private val reservaRepository: IReservaRepository = ReservaRepository(),
    private val canchaRepository: CanchaRepository = CanchaRepository(),
) : ViewModel() {

    private val _cancha = MutableStateFlow<Cancha?>(null)
    val cancha: StateFlow<Cancha?> = _cancha.asStateFlow()

    private val _reservasDelDia = MutableStateFlow<List<Reserva>>(emptyList())
    val reservasDelDia: StateFlow<List<Reserva>> = _reservasDelDia.asStateFlow()

    private val _isLoading = MutableStateFlow(value = false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _evento = MutableStateFlow<ReservaEvento>(ReservaEvento.Idle)
    val evento: StateFlow<ReservaEvento> = _evento.asStateFlow()

    fun cargarCancha(canchaId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _cancha.value = canchaRepository.getCanchaById(canchaId)
            } catch (_: Exception) {
                // Manejar error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun cargarDisponibilidad(fecha: LocalDate) {
        val id = _cancha.value?.id ?: return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                _reservasDelDia.value = reservaRepository.getReservasPorCanchaYFecha(id, fecha)
            } catch (_: Exception) {
                // Manejar error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun realizarReserva(fecha: LocalDate, hora: LocalTime, duracion: Double) {
        val canchaActual = _cancha.value ?: return
        val precioTotal = (canchaActual.precioPorHora) * duracion

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val nuevaReserva = Reserva(
                    id = "",
                    cancha = canchaActual,
                    fecha = LocalDateTime.of(fecha, hora),
                    duracionHoras = duracion,
                    precioTotal = precioTotal,
                    estado = EstadoReserva.CONFIRMADA,
                )

                val id = reservaRepository.crearReserva(nuevaReserva)
                _evento.value = ReservaEvento.ReservaExitosa(nuevaReserva.copy(id = id))
            } catch (e: Exception) {
                _evento.value = ReservaEvento.Error(e.message ?: "Error al realizar la reserva")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun limpiarEvento() {
        _evento.value = ReservaEvento.Idle
    }
}
