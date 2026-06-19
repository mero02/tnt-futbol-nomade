package com.example.futbol_tnt.data.model

data class Cancha(
    val id: String,
    val nombre: String,
    val direccion: String,
    val ciudad: String,
    val lat: Double,
    val lng: Double,
    val precioPorHora: Double,
    val tipo: TipoCancha,
    val imagenUrl: String? = null,
    val disponibilidad: List<Horario> = emptyList(),
)
