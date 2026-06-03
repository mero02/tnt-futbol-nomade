package com.example.futbol_tnt.data.repository

import com.example.futbol_tnt.data.model.Calificacion
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class CalificacionRepository(
    private val firestore: FirebaseFirestore = Firebase.firestore
) {
    private val califCol = firestore.collection("calificaciones")

    suspend fun calificarJugador(calificacion: Calificacion) {
        // Usamos un ID compuesto para evitar que un mismo jugador califique dos veces al mismo en un partido
        val id = "${calificacion.partidoId}_${calificacion.calificadorId}_${calificacion.calificadoId}"
        califCol.document(id).set(calificacion.toFirestoreMap()).await()

        // Aquí podríamos disparar una Cloud Function para recalcular el promedio del usuario calificado
        // O hacerlo manualmente actualizando el documento del usuario (menos escalable pero funciona para MVP)
        actualizarPromedioUsuario(calificacion.calificadoId)
    }

    private suspend fun actualizarPromedioUsuario(userId: String) {
        val calificaciones = califCol.whereEqualTo("calificadoId", userId).get().await()
        if (calificaciones.isEmpty) return

        val totalEstrellas = calificaciones.documents.sumOf { it.getLong("estrellas") ?: 0L }
        val promedio = totalEstrellas.toDouble() / calificaciones.size()

        firestore.collection("users").document(userId).update("valoracionPromedio", promedio).await()
    }

    suspend fun getCalificacionesPorPartido(partidoId: String, calificadorId: String): List<String> {
        val snapshot = califCol
            .whereEqualTo("partidoId", partidoId)
            .whereEqualTo("calificadorId", calificadorId)
            .get()
            .await()
        return snapshot.documents.mapNotNull { it.getString("calificadoId") }
    }

    private fun Calificacion.toFirestoreMap(): Map<String, Any?> = mapOf(
        "partidoId" to partidoId,
        "calificadorId" to calificadorId,
        "calificadoId" to calificadoId,
        "estrellas" to estrellas,
        "comentario" to comentario,
        "fecha" to fecha
    )
}
