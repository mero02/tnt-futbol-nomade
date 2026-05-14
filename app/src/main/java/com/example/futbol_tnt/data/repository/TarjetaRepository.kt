package com.example.futbol_tnt.data.repository

import com.example.futbol_tnt.data.model.Tarjeta
import com.example.futbol_tnt.data.model.MarcaTarjeta
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class TarjetaRepository(
    private val firestore: FirebaseFirestore = Firebase.firestore
) {
    private fun getTarjetasCol(userId: String) =
        firestore.collection("users").document(userId).collection("tarjetas")

    suspend fun getTarjetas(userId: String): List<Tarjeta> {
        val snapshot = getTarjetasCol(userId).get().await()
        return snapshot.documents.mapNotNull { it.toTarjeta() }
    }

    suspend fun agregarTarjeta(userId: String, tarjeta: Tarjeta) {
        val docRef = getTarjetasCol(userId).document()
        val tarjetaConId = tarjeta.copy(id = docRef.id)
        docRef.set(tarjetaConId.toFirestoreMap()).await()
    }

    suspend fun eliminarTarjeta(userId: String, tarjetaId: String) {
        getTarjetasCol(userId).document(tarjetaId).delete().await()
    }

    private fun Tarjeta.toFirestoreMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "nombreTitular" to nombreTitular,
        "last4" to last4,
        "vencimiento" to vencimiento,
        "marca" to marca.name,
        "esPredeterminada" to esPredeterminada
    )

    private fun com.google.firebase.firestore.DocumentSnapshot.toTarjeta(): Tarjeta? {
        return if (exists()) {
            Tarjeta(
                id = getString("id") ?: id,
                nombreTitular = getString("nombreTitular") ?: "",
                last4 = getString("last4") ?: "",
                vencimiento = getString("vencimiento") ?: "",
                marca = getString("marca")?.let { runCatching { MarcaTarjeta.valueOf(it) }.getOrNull() } ?: MarcaTarjeta.OTRA,
                esPredeterminada = getBoolean("esPredeterminada") ?: false
            )
        } else null
    }
}
