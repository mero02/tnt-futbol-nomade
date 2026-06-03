package com.example.futbol_tnt.data.repository

import com.example.futbol_tnt.data.model.Notificacion
import com.example.futbol_tnt.data.model.TipoNotificacion
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class NotificacionRepository(
    private val firestore: FirebaseFirestore = Firebase.firestore
) {
    private val notifCol = firestore.collection("notificaciones")

    fun getNotificaciones(userId: String): Flow<List<Notificacion>> = callbackFlow {
        val listener = notifCol
            .whereEqualTo("userId", userId)
            // Quitamos el orderBy temporalmente para evitar crash por falta de índice.
            // Una vez creado el índice en Firebase, se puede volver a poner:
            // .orderBy("fecha", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toNotificacion() } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun enviarNotificacion(notificacion: Notificacion) {
        notifCol.add(notificacion.toFirestoreMap()).await()
    }

    suspend fun marcarComoLeida(notifId: String) {
        notifCol.document(notifId).update("leido", true).await()
    }

    private fun Notificacion.toFirestoreMap(): Map<String, Any?> = mapOf(
        "userId" to userId,
        "titulo" to titulo,
        "mensaje" to mensaje,
        "fecha" to fecha,
        "leido" to leido,
        "tipo" to tipo.name
    )

    private fun com.google.firebase.firestore.DocumentSnapshot.toNotificacion(): Notificacion? {
        return if (exists()) {
            Notificacion(
                id = id,
                userId = getString("userId") ?: "",
                titulo = getString("titulo") ?: "",
                mensaje = getString("mensaje") ?: "",
                fecha = getTimestamp("fecha") ?: com.google.firebase.Timestamp.now(),
                leido = getBoolean("leido") ?: false,
                tipo = getString("tipo")?.let { runCatching { TipoNotificacion.valueOf(it) }.getOrNull() } ?: TipoNotificacion.INFO
            )
        } else null
    }
}
