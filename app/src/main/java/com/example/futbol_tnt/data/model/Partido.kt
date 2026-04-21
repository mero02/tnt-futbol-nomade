package com.example.futbol_tnt.data.model

import java.time.LocalDateTime
import java.time.LocalTime

data class Partido(
    val id: String,
    val nombreLocal: String,
    val nombreVisitante: String,
    val fecha: LocalDateTime,
    val cancha: Cancha,
    val duracionHoras: Int = 1,
    val precioPorPersona: Double,
    val jugadoresActuales: Int,
    val jugadoresMaximos: Int,
    val estado: EstadoPartido,
    val nombreOrganizador: String
)

enum class EstadoPartido {
    ABIERTO,      // Buscando jugadores
    LLENO,        // Completo
    EN_JUEGO,    // En curso
    FINALIZADO    // Terminado
}