package com.example.futbol_tnt.data.model

data class FiltroReservas(
    val fecha: FiltroFecha = FiltroFecha.TODOS,
    val estado: FiltroEstadoReserva = FiltroEstadoReserva.TODOS
)

enum class FiltroEstadoReserva { PENDIENTES, CONFIRMADAS, COMPLETADAS, TODOS }
