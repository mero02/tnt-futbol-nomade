package com.example.futbol_tnt.core.geofence

import com.example.futbol_tnt.data.model.Cancha
import java.time.LocalDateTime

/**
 * Reglas puras (sin dependencias de Android/Firestore) del sistema de geocercas.
 * Se aislan aca para poder testearlas en la JVM sin emulador ni device.
 */
object GeofencePolicy {

    /** Anticipacion con la que se habilita el check-in antes del horario. */
    const val VENTANA_PREVIA_HORAS = 2L

    /** Limite de geocercas activas que impone el sistema Android por app. */
    const val MAX_GEOFENCES = 100

    /**
     * Determina si un jugador puede hacer check-in manual (HU-40): desde
     * VENTANA_PREVIA_HORAS antes del inicio y hasta el fin del partido
     * (inicio + duracion).
     */
    fun enVentanaCheckIn(
        fechaPartido: LocalDateTime,
        duracionHoras: Int,
        ahora: LocalDateTime,
    ): Boolean {
        val inicio = fechaPartido.minusHours(VENTANA_PREVIA_HORAS)
        val fin = fechaPartido.plusHours(duracionHoras.toLong())
        return ahora.isAfter(inicio) && ahora.isBefore(fin)
    }

    /**
     * Selecciona las canchas a registrar como geocercas (HU-36): una por id
     * (dedup), descartando coordenadas invalidas (0,0), respetando el orden de
     * entrada (por eso el llamador pasa los partidos ordenados por fecha) y el
     * limite del sistema.
     */
    fun seleccionarCanchasParaGeofence(
        canchas: List<Cancha>,
        max: Int = MAX_GEOFENCES,
    ): List<Cancha> {
        val vistas = LinkedHashMap<String, Cancha>()
        for (c in canchas) {
            if (c.id.isBlank()) continue
            if (c.lat == 0.0 && c.lng == 0.0) continue
            if (vistas.containsKey(c.id)) continue
            vistas[c.id] = c
            if (vistas.size >= max) break
        }
        return vistas.values.toList()
    }

    /** Ajusta el radio de geocerca al rango valido (HU-35). */
    fun radioValido(radio: Double): Double =
        radio.coerceIn(Cancha.RADIO_GEOFENCE_MIN, Cancha.RADIO_GEOFENCE_MAX)
}
