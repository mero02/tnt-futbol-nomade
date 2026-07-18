package com.example.futbol_tnt.core.geofence

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.futbol_tnt.data.model.Cancha
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.util.Date

/**
 * Registra y remueve geocercas de canchas con partidos proximos (HU-36).
 *
 * Reglas clave:
 * - Solo se registran canchas que tienen un partido en la ventana proxima; asi
 *   se respeta el limite del sistema (100 geocercas) y se cuida la bateria.
 * - Cada geocerca usa el canchaId como requestId, de modo que el receiver puede
 *   identificar la cancha del evento.
 * - Antes de registrar se remueve el set anterior, limpiando las de partidos ya
 *   finalizados.
 */
class GeofenceManager(private val context: Context) {

    private val geofencingClient: GeofencingClient =
        LocationServices.getGeofencingClient(context)

    private val firestore: FirebaseFirestore = Firebase.firestore

    private val pendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java).apply {
            action = ACTION_GEOFENCE
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        PendingIntent.getBroadcast(context, 0, intent, flags)
    }

    /**
     * Recalcula las geocercas activas a partir de los partidos proximos.
     * Idempotente: se puede llamar al abrir la app o al refrescar.
     */
    @SuppressLint("MissingPermission")
    suspend fun sincronizarGeocercas() {
        if (!tienePermisosBackground()) {
            Log.w(TAG, "Sin permiso de ubicacion en background; no se registran geocercas")
            return
        }

        val canchas = canchasConPartidosProximos()
        // Siempre limpiamos el set previo (remueve las de partidos finalizados).
        runCatching { geofencingClient.removeGeofences(pendingIntent).await() }

        if (canchas.isEmpty()) {
            Log.d(TAG, "No hay partidos proximos; geocercas removidas")
            return
        }

        val geofences = canchas.map { c ->
            Geofence.Builder()
                .setRequestId(c.id)
                .setCircularRegion(c.lat, c.lng, c.radioGeofence.toFloat())
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build()
        }

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(0) // No disparar si ya estamos dentro al registrar
            .addGeofences(geofences)
            .build()

        try {
            geofencingClient.addGeofences(request, pendingIntent).await()
            Log.d(TAG, "Geocercas registradas: ${geofences.size}")
        } catch (e: Exception) {
            Log.e(TAG, "Error registrando geocercas", e)
        }
    }

    /** Remueve todas las geocercas (ej. al cerrar sesion). */
    fun removerTodas() {
        runCatching { geofencingClient.removeGeofences(pendingIntent) }
    }

    /**
     * Canchas unicas con al menos un partido en la ventana [ahora, ahora + VENTANA_HORAS],
     * limitadas a MAX_GEOFENCES (las de los partidos mas cercanos en el tiempo).
     */
    private suspend fun canchasConPartidosProximos(): List<Cancha> {
        val ahora = Date()
        val hasta = Date(ahora.time + VENTANA_HORAS * 60L * 60L * 1000L)

        return try {
            val snap = firestore.collection("partidos")
                .whereGreaterThanOrEqualTo("fecha", Timestamp(ahora))
                .whereLessThanOrEqualTo("fecha", Timestamp(hasta))
                .orderBy("fecha")
                .get()
                .await()

            // Partidos vienen ordenados por fecha; conservamos ese orden para que,
            // si se supera el limite, queden las canchas de los partidos mas proximos.
            val candidatas = snap.documents.mapNotNull { doc ->
                if (doc.getString("estado") == "FINALIZADO") return@mapNotNull null

                @Suppress("UNCHECKED_CAST")
                val canchaMap = doc.get("cancha") as? Map<String, Any?> ?: return@mapNotNull null
                val id = canchaMap["id"] as? String ?: return@mapNotNull null
                val lat = (canchaMap["lat"] as? Number)?.toDouble() ?: return@mapNotNull null
                val lng = (canchaMap["lng"] as? Number)?.toDouble() ?: return@mapNotNull null

                Cancha(
                    id = id,
                    nombre = canchaMap["nombre"] as? String ?: "",
                    direccion = canchaMap["direccion"] as? String ?: "",
                    ciudad = canchaMap["ciudad"] as? String ?: "",
                    lat = lat,
                    lng = lng,
                    precioPorHora = 0.0,
                    tipo = com.example.futbol_tnt.data.model.TipoCancha.FUTBOL_5,
                    radioGeofence = (canchaMap["radioGeofence"] as? Number)?.toDouble()
                        ?: Cancha.RADIO_GEOFENCE_DEFAULT,
                )
            }
            // Dedup por id, descarta coords (0,0) y aplica el limite del sistema.
            GeofencePolicy.seleccionarCanchasParaGeofence(candidatas, MAX_GEOFENCES)
        } catch (e: Exception) {
            Log.e(TAG, "Error consultando partidos proximos", e)
            emptyList()
        }
    }

    private fun tienePermisosBackground(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val background = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        return fine && background
    }

    companion object {
        private const val TAG = "GeofenceManager"
        const val ACTION_GEOFENCE = "com.example.futbol_tnt.ACTION_GEOFENCE"
        // Limite del sistema Android por app.
        private const val MAX_GEOFENCES = 100
        // Ventana de anticipacion: registramos canchas con partidos dentro de este rango.
        private const val VENTANA_HORAS = 24
    }
}
