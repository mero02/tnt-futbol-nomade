package com.example.futbol_tnt.data.repository

import com.example.futbol_tnt.data.model.Reserva
import kotlinx.coroutines.flow.Flow

interface IReservaRepository {
    val reservas: Flow<List<Reserva>>
    suspend fun crearReserva(reserva: Reserva): String
    suspend fun getReservaById(id: String): Reserva?
    suspend fun getReservasPorCanchaYFecha(canchaId: String, fecha: java.time.LocalDate): List<Reserva>
    suspend fun cancelarReserva(reservaId: String)
    suspend fun pagarReserva(reservaId: String, monto: Double)
}
