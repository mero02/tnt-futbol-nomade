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
}
