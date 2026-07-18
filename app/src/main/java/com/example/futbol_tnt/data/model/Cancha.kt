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
    // Radio (metros) de la geocerca para detectar llegada a la cancha (HU-34/35).
    // Rango recomendado 100-150 m segun el tamano real de la cancha.
    val radioGeofence: Double = RADIO_GEOFENCE_DEFAULT,
) {
    companion object {
        const val RADIO_GEOFENCE_DEFAULT = 120.0
        const val RADIO_GEOFENCE_MIN = 100.0
        const val RADIO_GEOFENCE_MAX = 150.0
    }
}
