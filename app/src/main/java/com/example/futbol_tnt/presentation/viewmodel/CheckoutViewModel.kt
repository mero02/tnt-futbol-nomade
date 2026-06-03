package com.example.futbol_tnt.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futbol_tnt.data.model.Cancha
import com.example.futbol_tnt.data.model.EstadoReserva
import com.example.futbol_tnt.data.model.MockData
import com.example.futbol_tnt.data.model.Reserva
import com.example.futbol_tnt.data.repository.IReservaRepository
import com.example.futbol_tnt.data.repository.ReservaRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

sealed class CheckoutEvento {
    data object Idle : CheckoutEvento()
    data class PagoExitoso(val reservaId: String) : CheckoutEvento()
    data class Error(val mensaje: String) : CheckoutEvento()
}

class CheckoutViewModel(
    private val repository: IReservaRepository = ReservaRepository()
) : ViewModel() {

    private val _cancha = MutableStateFlow<Cancha?>(null)
    val cancha: StateFlow<Cancha?> = _cancha.asStateFlow()

    private val _isLoading = MutableStateFlow(value = false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _evento = MutableStateFlow<CheckoutEvento>(CheckoutEvento.Idle)
    val evento: StateFlow<CheckoutEvento> = _evento.asStateFlow()

    fun cargarCancha(canchaId: String) {
        _cancha.value = MockData.canchas.find { it.id == canchaId }
    }

    fun confirmarReserva(
        fechaHora: LocalDateTime,
        duracion: Double,
        montoAPagar: Double,
        esTotal: Boolean
    ) {
        val canchaActual = _cancha.value ?: return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Simulamos demora de pasarela de pago
                if (montoAPagar > 0) {
                    delay(2000)
                }

                val reserva = Reserva(
                    id = "", // Generado en el repo
                    cancha = canchaActual,
                    fecha = fechaHora,
                    duracionHoras = duracion,
                    precioTotal = canchaActual.precioPorHora * duracion,
                    montoPagado = montoAPagar,
                    estado = if (esTotal) EstadoReserva.CONFIRMADA else EstadoReserva.PENDIENTE
                )

                val id = repository.crearReserva(reserva)
                _evento.value = CheckoutEvento.PagoExitoso(id)
            } catch (e: Exception) {
                _evento.value = CheckoutEvento.Error(e.message ?: "Error al procesar la reserva")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun limpiarEvento() {
        _evento.value = CheckoutEvento.Idle
    }
}
