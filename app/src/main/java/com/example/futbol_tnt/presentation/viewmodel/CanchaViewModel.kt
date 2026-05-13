package com.example.futbol_tnt.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futbol_tnt.data.model.Cancha
import com.example.futbol_tnt.data.model.MockData
import com.example.futbol_tnt.data.model.Reserva
import com.example.futbol_tnt.data.model.EstadoReserva
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
    private val repository: IReservaRepository = ReservaRepository(),
) : ViewModel() {

    private val _cancha = MutableStateFlow<Cancha?>(null)
    val cancha: StateFlow<Cancha?> = _cancha.asStateFlow()

    private val _fechaSeleccionada = MutableStateFlow(LocalDate.now())
    val fechaSeleccionada: StateFlow<LocalDate> = _fechaSeleccionada.asStateFlow()

    private val _horaSeleccionada = MutableStateFlow<LocalTime?>(null)
    val horaSeleccionada: StateFlow<LocalTime?> = _horaSeleccionada.asStateFlow()

    private val _duracionSeleccionada = MutableStateFlow(1.0)
    val duracionSeleccionada: StateFlow<Double> = _duracionSeleccionada.asStateFlow()

    private val _reservasDelDia = MutableStateFlow<List<Reserva>>(emptyList())
    val reservasDelDia: StateFlow<List<Reserva>> = _reservasDelDia.asStateFlow()

    private val _isLoading = MutableStateFlow(value = false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _evento = MutableStateFlow<ReservaEvento>(ReservaEvento.Idle)
    val evento: StateFlow<ReservaEvento> = _evento.asStateFlow()

    fun cargarCancha(canchaId: String) {
        // En el futuro esto vendrá de un repositorio
        _cancha.value = MockData.canchas.find { it.id == canchaId }
        actualizarDisponibilidad()
    }

    fun seleccionarFecha(fecha: LocalDate) {
        _fechaSeleccionada.value = fecha
        _horaSeleccionada.value = null // Reset hora al cambiar fecha
        actualizarDisponibilidad()
    }

    private fun actualizarDisponibilidad() {
        val id = _cancha.value?.id ?: return
        val fecha = _fechaSeleccionada.value

        viewModelScope.launch {
            _isLoading.value = true
            try {
                _reservasDelDia.value = repository.getReservasPorCanchaYFecha(id, fecha)
            } catch (e: Exception) {
                // Manejar error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun seleccionarHora(hora: LocalTime) {
        _horaSeleccionada.value = hora
    }

    fun seleccionarDuracion(duracion: Double) {
        _duracionSeleccionada.value = duracion
    }

    fun calcularPrecioTotal(): Double {
        val precioHora = _cancha.value?.precioPorHora ?: 0.0
        return precioHora * _duracionSeleccionada.value
    }

    fun realizarReserva() {
        val canchaActual = _cancha.value ?: return
        val fecha = _fechaSeleccionada.value
        val hora = _horaSeleccionada.value ?: return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val nuevaReserva = Reserva(
                    id = "",
                    cancha = canchaActual,
                    fecha = LocalDateTime.of(fecha, hora),
                    duracionHoras = _duracionSeleccionada.value,
                    precioTotal = calcularPrecioTotal(),
                    estado = EstadoReserva.CONFIRMADA,
                )

                val id = repository.crearReserva(nuevaReserva)
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
