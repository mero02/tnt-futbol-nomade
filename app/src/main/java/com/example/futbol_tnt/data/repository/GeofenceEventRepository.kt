package com.example.futbol_tnt.data.repository

import com.example.futbol_tnt.data.model.GeofenceEvent
import com.example.futbol_tnt.data.model.TipoEventoGeofence
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

/**
 * Escribe eventos de geocerca en la coleccion "geofence_events" (HU-39).
 *
 * Firestore mantiene una cola offline propia: si no hay conexion, el `set()`
 * queda pendiente y se sincroniza al recuperar la red. No hace falta una cola
 * manual para el caso comun; por eso el registro es "fire and forget" salvo el
 * check-in manual, donde se espera confirmacion para dar feedback al usuario.
 */
class GeofenceEventRepository(
    private val firestore: FirebaseFirestore = Firebase.firestore,
    private val auth: FirebaseAuth = Firebase.auth,
) {
    private val eventsCol = firestore.collection(COLLECTION)

    /**
     * Registro automatico desde el BroadcastReceiver (HU-38/39). No requiere UI.
     * Un ENTER dispara una sola vez al cruzar el borde, asi que un evento por
     * entrada por dia es suficiente; el docId deterministico evita spam si el SO
     * reentrega la transicion.
     */
    fun registrarEventoAutomatico(
        userId: String,
        canchaId: String,
        tipo: TipoEventoGeofence,
        lat: Double?,
        lng: Double?,
        diaKey: String,
    ) {
        val docId = "${canchaId}_${userId}_${diaKey}"
        eventsCol.document(docId).set(
            baseMap(userId, canchaId, partidoId = null, tipo = tipo, lat = lat, lng = lng, manual = false),
            SetOptions.merge()
        )
    }

    /**
     * Check-in manual (HU-40). Idempotente por partido+usuario para evitar el
     * doble check-in. Devuelve Result: fallo si ya existia un check-in previo.
     */
    suspend fun registrarCheckInManual(
        partidoId: String,
        canchaId: String,
        lat: Double?,
        lng: Double?,
    ): Result<Unit> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Necesitas iniciar sesion"))
        val docId = "${partidoId}_${uid}_manual"
        val ref = eventsCol.document(docId)
        return try {
            val existente = ref.get().await()
            if (existente.exists()) {
                return Result.failure(YaRegistradoException())
            }
            ref.set(
                baseMap(uid, canchaId, partidoId, TipoEventoGeofence.ENTER, lat, lng, manual = true)
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            // Race: dos taps casi simultaneos pasan el get() y el segundo set() cae
            // como update sobre doc existente -> la regla lo niega (PERMISSION_DENIED).
            // No es un error real: ya quedo registrada la llegada.
            if (e is FirebaseFirestoreException &&
                e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED
            ) {
                Result.failure(YaRegistradoException())
            } else {
                Result.failure(e)
            }
        }
    }

    private fun baseMap(
        userId: String,
        canchaId: String,
        partidoId: String?,
        tipo: TipoEventoGeofence,
        lat: Double?,
        lng: Double?,
        manual: Boolean,
    ): Map<String, Any?> = mapOf(
        "userId" to userId,
        "canchaId" to canchaId,
        "partidoId" to partidoId,
        "tipo" to tipo.name,
        "lat" to lat,
        "lng" to lng,
        "manual" to manual,
        // Server timestamp: fuente de verdad del momento de llegada.
        "timestamp" to FieldValue.serverTimestamp(),
        "createdAt" to Timestamp.now(),
    )

    class YaRegistradoException : Exception("Ya registraste tu llegada a este partido")

    private companion object {
        const val COLLECTION = "geofence_events"
    }
}
