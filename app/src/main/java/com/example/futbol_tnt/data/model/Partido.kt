package com.example.futbol_tnt.data.model

import java.time.LocalDateTime

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
    val nombreOrganizador: String,
    // ID de la reserva asociada (opcional, solo si el partido nació de una reserva)
    val reservaId: String? = null,
    // uid del usuario que creo el partido (Firebase Auth). Necesario para
    // que las reglas de Firestore puedan validar quien escribe/edita.
    val creatorId: String = "",
    // Lista de UIDs de los jugadores confirmados
    val participantesIds: List<String> = emptyList(),
    // Lista de UIDs de los jugadores que enviaron solicitud y esperan aprobación
    val solicitudesIds: List<String> = emptyList(),
    val esUrgente: Boolean = false
)
