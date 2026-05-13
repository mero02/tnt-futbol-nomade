package com.example.futbol_tnt.data.model

import java.time.LocalDateTime

data class Reserva(
    val id: String,
    val usuarioId: String = "",
    val cancha: Cancha,
    val fecha: LocalDateTime,
    val duracionHoras: Int,
    val precioTotal: Double,
    val estado: EstadoReserva,
    val nombreEquipo: String? = null
)
