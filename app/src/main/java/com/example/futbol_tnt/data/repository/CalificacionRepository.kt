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
        val id = "${calificacion.partidoId}_${calificacion.calificadorId}_${calificacion.calificadoId}"
        califCol.document(id).set(calificacion.toFirestoreMap()).await()
    }

    suspend fun getPromedioUsuario(userId: String): Double {
        val snapshot = califCol.whereEqualTo("calificadoId", userId).get().await()
        if (snapshot.isEmpty) return 0.0

        val totalEstrellas = snapshot.documents.sumOf { it.getLong("estrellas") ?: 0L }
        return totalEstrellas.toDouble() / snapshot.size()
    }

    suspend fun getCalificacionesPorPartido(partidoId: String, calificadorId: String): List<String> {
        val snapshot = califCol
            .whereEqualTo("partidoId", partidoId)
            .whereEqualTo("calificadorId", calificadorId)
            .get()
            .await()
        return snapshot.documents.mapNotNull { it.getString("calificadoId") }
    }

    suspend fun getCalificacionesRecibidas(userId: String): List<Calificacion> {
        val snapshot = califCol
            .whereEqualTo("calificadoId", userId)
            // Quitamos el orderBy de Firestore para evitar el error de índice.
            // Ordenaremos en memoria en el ViewModel o aquí mismo.
            .get()
            .await()
        return snapshot.documents
            .mapNotNull { it.toCalificacion() }
            .sortedByDescending { it.fecha }
    }

    private fun Calificacion.toFirestoreMap(): Map<String, Any?> = mapOf(
        "partidoId" to partidoId,
        "calificadorId" to calificadorId,
        "calificadoId" to calificadoId,
        "estrellas" to estrellas.toLong(),
        "comentario" to comentario,
        "fecha" to fecha
    )

    private fun com.google.firebase.firestore.DocumentSnapshot.toCalificacion(): Calificacion? {
        return if (exists()) {
            Calificacion(
                id = id,
                partidoId = getString("partidoId") ?: "",
                calificadorId = getString("calificadorId") ?: "",
                calificadoId = getString("calificadoId") ?: "",
                estrellas = (getLong("estrellas") ?: 0L).toInt(),
                comentario = getString("comentario"),
                fecha = getTimestamp("fecha") ?: com.google.firebase.Timestamp.now()
            )
        } else null
    }
}
