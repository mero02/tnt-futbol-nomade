package com.example.futbol_tnt.core.geofence

import com.example.futbol_tnt.data.model.Cancha
import com.example.futbol_tnt.data.model.TipoCancha
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

/**
 * Tests de la logica de decision del sistema de geocercas (HU-35/36/40).
 * Puras en JVM: no cubren el geofence del SO ni Firestore (requieren device).
 */
class GeofencePolicyTest {

    private fun cancha(
        id: String,
        lat: Double = -42.76,
        lng: Double = -65.03,
        radio: Double = Cancha.RADIO_GEOFENCE_DEFAULT,
    ) = Cancha(
        id = id,
        nombre = "Cancha $id",
        direccion = "Dir",
        ciudad = "Madryn",
        lat = lat,
        lng = lng,
        precioPorHora = 0.0,
        tipo = TipoCancha.FUTBOL_5,
        radioGeofence = radio,
    )

    // --- enVentanaCheckIn (HU-40) ---

    private val fecha = LocalDateTime.of(2026, 7, 18, 20, 0) // partido 20:00, dura 1h

    @Test
    fun `check-in habilitado justo dentro de la ventana previa`() {
        // 1h antes del inicio (dentro de las 2h previas)
        assertTrue(GeofencePolicy.enVentanaCheckIn(fecha, 1, fecha.minusHours(1)))
    }

    @Test
    fun `check-in deshabilitado demasiado temprano`() {
        // 3h antes: fuera de la ventana de 2h
        assertFalse(GeofencePolicy.enVentanaCheckIn(fecha, 1, fecha.minusHours(3)))
    }

    @Test
    fun `check-in habilitado durante el partido`() {
        assertTrue(GeofencePolicy.enVentanaCheckIn(fecha, 1, fecha.plusMinutes(30)))
    }

    @Test
    fun `check-in deshabilitado despues de terminar`() {
        // 1h de duracion: a las 21:30 ya termino
        assertFalse(GeofencePolicy.enVentanaCheckIn(fecha, 1, fecha.plusMinutes(90)))
    }

    // --- seleccionarCanchasParaGeofence (HU-36) ---

    @Test
    fun `deduplica canchas por id conservando la primera aparicion`() {
        val entrada = listOf(cancha("A"), cancha("B"), cancha("A"))
        val salida = GeofencePolicy.seleccionarCanchasParaGeofence(entrada)
        assertEquals(listOf("A", "B"), salida.map { it.id })
    }

    @Test
    fun `descarta coordenadas invalidas 0 0`() {
        val entrada = listOf(cancha("A"), cancha("SIN_COORDS", lat = 0.0, lng = 0.0))
        val salida = GeofencePolicy.seleccionarCanchasParaGeofence(entrada)
        assertEquals(listOf("A"), salida.map { it.id })
    }

    @Test
    fun `descarta id en blanco`() {
        val entrada = listOf(cancha(""), cancha("B"))
        val salida = GeofencePolicy.seleccionarCanchasParaGeofence(entrada)
        assertEquals(listOf("B"), salida.map { it.id })
    }

    @Test
    fun `respeta el limite maximo de geocercas`() {
        val entrada = (1..150).map { cancha("C$it") }
        val salida = GeofencePolicy.seleccionarCanchasParaGeofence(entrada, max = 100)
        assertEquals(100, salida.size)
        // Conserva las primeras (partidos mas proximos por orden de fecha)
        assertEquals("C1", salida.first().id)
        assertEquals("C100", salida.last().id)
    }

    // --- radioValido (HU-35) ---

    @Test
    fun `radio dentro del rango se mantiene`() {
        assertEquals(120.0, GeofencePolicy.radioValido(120.0), 0.001)
    }

    @Test
    fun `radio por debajo del minimo se ajusta`() {
        assertEquals(Cancha.RADIO_GEOFENCE_MIN, GeofencePolicy.radioValido(50.0), 0.001)
    }

    @Test
    fun `radio por encima del maximo se ajusta`() {
        assertEquals(Cancha.RADIO_GEOFENCE_MAX, GeofencePolicy.radioValido(500.0), 0.001)
    }
}
