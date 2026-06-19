package com.example.futbol_tnt.data.repository

import com.example.futbol_tnt.data.model.Cancha
import com.example.futbol_tnt.data.model.Horario
import com.example.futbol_tnt.data.model.TipoCancha
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.time.LocalTime

class CanchaRepository(
    private val firestore: FirebaseFirestore = Firebase.firestore
) {
    private val canchasCol = firestore.collection("canchas")

    suspend fun getCanchas(): List<Cancha> {
        return try {
            val snapshot = canchasCol.get().await()
            snapshot.documents.mapNotNull { it.toCanchaOrNull() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getCanchaById(id: String): Cancha? {
        return try {
            canchasCol.document(id).get().await().toCanchaOrNull()
        } catch (e: Exception) {
            null
        }
    }

    private fun DocumentSnapshot.toCanchaOrNull(): Cancha? {
        return try {
            Cancha(
                id = id,
                nombre = getString("nombre") ?: "",
                direccion = getString("direccion") ?: "",
                ciudad = getString("ciudad") ?: "",
                lat = (get("lat") as? Number)?.toDouble() ?: 0.0,
                lng = (get("lng") as? Number)?.toDouble() ?: 0.0,
                precioPorHora = (get("precioPorHora") as? Number)?.toDouble() ?: 0.0,
                tipo = try {
                    TipoCancha.valueOf(getString("tipo") ?: "FUTBOL_5")
                } catch (e: Exception) {
                    TipoCancha.FUTBOL_5
                },
                imagenUrl = getString("imagenUrl"),
                disponibilidad = generarHorarios()
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun generarHorarios(): List<Horario> {
        return (8..22).map { hora ->
            Horario(
                hora = LocalTime.of(hora, 0),
                disponible = true
            )
        }
    }

    suspend fun seedCanchas() {
        try {
            val snapshot = canchasCol.limit(1).get().await()
            if (snapshot.isEmpty) {
                val canchasIniciales = listOf(
                    // TRELEW
                    mapOf("nombre" to "Cancha El Túnel", "direccion" to "Av. Fontana 450", "ciudad" to "Trelew", "lat" to -43.2489, "lng" to -65.3051, "precioPorHora" to 12000.0, "tipo" to "FUTBOL_5"),
                    mapOf("nombre" to "Club Independiente", "direccion" to "Rivadavia 500", "ciudad" to "Trelew", "lat" to -43.2530, "lng" to -65.3120, "precioPorHora" to 15000.0, "tipo" to "FUTBOL_7"),
                    mapOf("nombre" to "Complejo Racing", "direccion" to "25 de Mayo 900", "ciudad" to "Trelew", "lat" to -43.2460, "lng" to -65.2980, "precioPorHora" to 18000.0, "tipo" to "FUTBOL_11"),

                    // PUERTO MADRYN
                    mapOf("nombre" to "Golfo Fútbol", "direccion" to "Bv. Brown 1200", "ciudad" to "Pto Madryn", "lat" to -42.7660, "lng" to -65.0300, "precioPorHora" to 20000.0, "tipo" to "FUTBOL_5"),
                    mapOf("nombre" to "Cancha de la Costa", "direccion" to "Av. Roca 600", "ciudad" to "Pto Madryn", "lat" to -42.7580, "lng" to -65.0380, "precioPorHora" to 22000.0, "tipo" to "FUTBOL_7"),
                    mapOf("nombre" to "Arena Sport", "direccion" to "Gales 300", "ciudad" to "Pto Madryn", "lat" to -42.7710, "lng" to -65.0450, "precioPorHora" to 15000.0, "tipo" to "PADDEL"),

                    // GAIMAN
                    mapOf("nombre" to "Gaiman FC Cancha 1", "direccion" to "Av. Tello 150", "ciudad" to "Gaiman", "lat" to -43.2890, "lng" to -65.4920, "precioPorHora" to 10000.0, "tipo" to "FUTBOL_5"),
                    mapOf("nombre" to "Cancha del Valle", "direccion" to "Eugenio Tello 400", "ciudad" to "Gaiman", "lat" to -43.2920, "lng" to -65.4950, "precioPorHora" to 11000.0, "tipo" to "FUTBOL_5"),

                    // RAWSON
                    mapOf("nombre" to "Puerto Rawson Fútbol", "direccion" to "Av. Marcelino González", "ciudad" to "Rawson", "lat" to -43.3330, "lng" to -65.0500, "precioPorHora" to 14000.0, "tipo" to "FUTBOL_5"),
                    mapOf("nombre" to "Complejo Playa Unión", "direccion" to "Av. Guillermo Rawson 1000", "ciudad" to "Rawson", "lat" to -43.3160, "lng" to -65.0400, "precioPorHora" to 16000.0, "tipo" to "FUTBOL_7"),

                    // DOLAVON
                    mapOf("nombre" to "Dolavon Central", "direccion" to "25 de Mayo 200", "ciudad" to "Dolavon", "lat" to -43.3080, "lng" to -65.7050, "precioPorHora" to 9000.0, "tipo" to "FUTBOL_5"),
                    mapOf("nombre" to "Cancha de la Noria", "direccion" to "Roca y Maipú", "ciudad" to "Dolavon", "lat" to -43.3100, "lng" to -65.7080, "precioPorHora" to 10000.0, "tipo" to "FUTBOL_5")
                )

                for (cancha in canchasIniciales) {
                    canchasCol.add(cancha).await()
                }
            }
        } catch (_: Exception) { }
    }
}
