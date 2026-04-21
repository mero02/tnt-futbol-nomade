package com.example.futbol_tnt.data.model

import java.time.LocalDateTime
import java.time.LocalTime

data class Cancha(
    val id: String,
    val nombre: String,
    val direccion: String,
    val precioPorHora: Double,
    val tipo: TipoCancha,
    val imagenUrl: String? = null,
    val disponibilidad: List<Horario> = emptyList()
)

data class Horario(
    val hora: LocalTime,
    val disponible: Boolean
)

enum class TipoCancha {
    FUTBOL_5, FUTBOL_7, FUTBOL_11, PADDEL
}

data class Reserva(
    val id: String,
    val cancha: Cancha,
    val fecha: LocalDateTime,
    val duracionHoras: Int,
    val precioTotal: Double,
    val estado: EstadoReserva,
    val nombreEquipo: String? = null
)

enum class EstadoReserva {
    PENDIENTE, CONFIRMADA, CANCELADA, COMPLETADA
}

object MockData {

    val canchas = listOf(
        Cancha(
            id = "1",
            nombre = "Quincho La Palmera",
            direccion = "Av. Santa Fe 1234, Palermo",
            precioPorHora = 15000.0,
            tipo = TipoCancha.FUTBOL_5,
            disponibilidad = generarHorarios()
        ),
        Cancha(
            id = "2",
            nombre = "Club Deportivo Norte",
            direccion = "Av. Córdoba 5678, Núñez",
            precioPorHora = 18000.0,
            tipo = TipoCancha.FUTBOL_7,
            disponibilidad = generarHorarios()
        ),
        Cancha(
            id = "3",
            nombre = "Estadio Verde",
            direccion = "Av. del Libertador 900, Vicente López",
            precioPorHora = 25000.0,
            tipo = TipoCancha.FUTBOL_11,
            disponibilidad = generarHorarios()
        ),
        Cancha(
            id = "4",
            nombre = "Paddel Arena",
            direccion = "Av. Rivadavia 4321, Caballito",
            precioPorHora = 8000.0,
            tipo = TipoCancha.PADDEL,
            disponibilidad = generarHorarios()
        ),
        Cancha(
            id = "5",
            nombre = "Mini Fútbol Villa",
            direccion = "Av. Juan B. Justo 2345, Flores",
            precioPorHora = 12000.0,
            tipo = TipoCancha.FUTBOL_5,
            disponibilidad = generarHorarios()
        )
    )

    val partidos = listOf(
        Partido(
            id = "p1",
            nombreLocal = "Los Chetos FC",
            nombreVisitante = "Los Boludos",
            fecha = LocalDateTime.now().plusDays(1).withHour(17).withMinute(0),
            cancha = canchas[0],
            duracionHoras = 1,
            precioPorPersona = 2500.0,
            jugadoresActuales = 6,
            jugadoresMaximos = 10,
            estado = EstadoPartido.ABIERTO,
            nombreOrganizador = "Juan Pérez"
        ),
        Partido(
            id = "p2",
            nombreLocal = "Racing Club",
            nombreVisitante = "River Plate",
            fecha = LocalDateTime.now().plusDays(2).withHour(19).withMinute(0),
            cancha = canchas[1],
            duracionHoras = 2,
            precioPorPersona = 3000.0,
            jugadoresActuales = 14,
            jugadoresMaximos = 14,
            estado = EstadoPartido.LLENO,
            nombreOrganizador = "Pedro Gómez"
        ),
        Partido(
            id = "p3",
            nombreLocal = "Boca Jrs",
            nombreVisitante = "Independiente",
            fecha = LocalDateTime.now().plusDays(3).withHour(16).withMinute(0),
            cancha = canchas[2],
            duracionHoras = 1,
            precioPorPersona = 4000.0,
            jugadoresActuales = 8,
            jugadoresMaximos = 22,
            estado = EstadoPartido.ABIERTO,
            nombreOrganizador = "Carlos Díaz"
        ),
        Partido(
            id = "p4",
            nombreLocal = "San Lorenzo",
            nombreVisitante = "Huracán",
            fecha = LocalDateTime.now().plusDays(4).withHour(18).withMinute(0),
            cancha = canchas[4],
            duracionHoras = 1,
            precioPorPersona = 2000.0,
            jugadoresActuales = 9,
            jugadoresMaximos = 10,
            estado = EstadoPartido.LLENO,
            nombreOrganizador = "María López"
        )
    )

    val reservas = listOf(
        Reserva(
            id = "r1",
            cancha = canchas[0],
            fecha = LocalDateTime.now().plusDays(1).withHour(16).withMinute(0),
            duracionHoras = 1,
            precioTotal = 15000.0,
            estado = EstadoReserva.CONFIRMADA,
            nombreEquipo = "Los Chetos FC"
        ),
        Reserva(
            id = "r2",
            cancha = canchas[1],
            fecha = LocalDateTime.now().plusDays(2).withHour(18).withMinute(0),
            duracionHoras = 2,
            precioTotal = 36000.0,
            estado = EstadoReserva.PENDIENTE,
            nombreEquipo = "River Plate"
        ),
        Reserva(
            id = "r3",
            cancha = canchas[4],
            fecha = LocalDateTime.now().minusDays(3).withHour(10).withMinute(0),
            duracionHoras = 1,
            precioTotal = 12000.0,
            estado = EstadoReserva.COMPLETADA,
            nombreEquipo = "Boca Jrs"
        )
    )

    private fun generarHorarios(): List<Horario> {
        return (8..22).map { hora ->
            Horario(
                hora = LocalTime.of(hora, 0),
                disponible = hora % 3 != 0
            )
        }
    }
}