package com.example.futbol_tnt.data.model

/**
 * Evento de geocerca registrado cuando un jugador llega a una cancha (HU-39).
 *
 * Se persiste en la coleccion Firestore "geofence_events". El origen puede ser
 * automatico (transicion ENTER del geofence) o manual (boton "Ya llegue", HU-40).
 */
data class GeofenceEvent(
    val id: String = "",
    val userId: String,
    val canchaId: String,
    // Partido asociado. En el ENTER automatico puede no conocerse (varios partidos
    // en la misma cancha); el check-in manual siempre lo lleva.
    val partidoId: String? = null,
    val tipo: TipoEventoGeofence = TipoEventoGeofence.ENTER,
    // Coordenadas del dispositivo al momento del evento (pueden faltar).
    val lat: Double? = null,
    val lng: Double? = null,
    // true si lo genero el jugador a mano (fallback GPS), false si fue el geofence.
    val manual: Boolean = false,
    // Epoch millis del evento (se completa con el server timestamp al escribir).
    val timestamp: Long = 0L,
)

enum class TipoEventoGeofence {
    ENTER,
    EXIT
}
