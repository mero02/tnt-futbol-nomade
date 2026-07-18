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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.util.Date

/**
 * Registra y remueve geocercas de canchas con partidos proximos del usuario (HU-36).
 *
 * Reglas clave:
 * - Solo se registran canchas de partidos donde el usuario participa (no de
 *   partidos ajenos): asi no se generan check-ins falsos y se cuida la bateria.
 * - Cada geocerca usa el canchaId como requestId, para que el receiver identifique
 *   la cancha del evento.
 * - Cada geocerca expira al terminar el partido (+ margen): las de partidos
 *   pasados se limpian solas aunque el usuario no reabra la app.
 * - Antes de registrar se remueve el set anterior.
 */
class GeofenceManager(context: Context) {

    // applicationContext: el PendingIntent es persistente y no debe atar una Activity.
    private val appContext: Context = context.applicationContext

    private val geofencingClient: GeofencingClient =
        LocationServices.getGeofencingClient(appContext)

    private val firestore: FirebaseFirestore = Firebase.firestore
    private val auth: FirebaseAuth = Firebase.auth

    private val pendingIntent: PendingIntent by lazy {
        val intent = Intent(appContext, GeofenceBroadcastReceiver::class.java).apply {
            action = ACTION_GEOFENCE
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        PendingIntent.getBroadcast(appContext, 0, intent, flags)
    }

    /**
     * Recalcula las geocercas activas a partir de los partidos proximos del usuario.
     * Idempotente: se puede llamar al abrir la app o al refrescar.
     */
    @SuppressLint("MissingPermission")
    suspend fun sincronizarGeocercas() {
        if (!tienePermisosBackground()) {
            Log.w(TAG, "Sin permiso de ubicacion en background; no se registran geocercas")
            return
        }

        val specs = geocercasDePartidosDelUsuario()
        // Siempre limpiamos el set previo (remueve las de partidos finalizados).
        runCatching { geofencingClient.removeGeofences(pendingIntent).await() }

        if (specs.isEmpty()) {
            Log.d(TAG, "No hay partidos proximos del usuario; geocercas removidas")
            return
        }

        val geofences = specs.map { spec ->
            Geofence.Builder()
                .setRequestId(spec.cancha.id)
                .setCircularRegion(spec.cancha.lat, spec.cancha.lng, spec.cancha.radioGeofence.toFloat())
                // Expira al terminar el partido (+ margen); se limpia sola.
                .setExpirationDuration(spec.expiraEnMs)
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

    private data class GeofenceSpec(val cancha: Cancha, val expiraEnMs: Long)

    /**
     * Geocercas a registrar: canchas de partidos donde el usuario participa, con
     * un partido dentro de la ventana proxima. La expiracion de cada geocerca es
     * el tiempo restante hasta el fin del partido mas proximo en esa cancha.
     */
    private suspend fun geocercasDePartidosDelUsuario(): List<GeofenceSpec> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        val ahoraMs = Date().time
        val hastaMs = ahoraMs + VENTANA_HORAS * 60L * 60L * 1000L

        return try {
            // arrayContains + range en distinto campo exigiria indice compuesto:
            // filtramos por participante en la query y por fecha/estado en cliente.
            val snap = firestore.collection("partidos")
                .whereArrayContains("participantesIds", uid)
                .get()
                .await()

            // Partido mas proximo por cancha, dentro de la ventana y no finalizado.
            val candidatas = ArrayList<Cancha>()
            val expiraPorCancha = HashMap<String, Long>()

            snap.documents
                .mapNotNull { doc ->
                    val inicioMs = doc.getTimestamp("fecha")?.toDate()?.time ?: return@mapNotNull null
                    Triple(doc, inicioMs, doc)
                }
                .sortedBy { it.second } // por fecha ascendente
                .forEach { (doc, inicioMs, _) ->
                    if (doc.getString("estado") == "FINALIZADO") return@forEach
                    if (inicioMs < ahoraMs - VENTANA_PREVIA_MS || inicioMs > hastaMs) return@forEach

                    @Suppress("UNCHECKED_CAST")
                    val canchaMap = doc.get("cancha") as? Map<String, Any?> ?: return@forEach
                    val id = canchaMap["id"] as? String ?: return@forEach
                    val lat = (canchaMap["lat"] as? Number)?.toDouble() ?: return@forEach
                    val lng = (canchaMap["lng"] as? Number)?.toDouble() ?: return@forEach

                    val duracionH = (doc.getLong("duracionHoras") ?: 1L)
                    val finMs = inicioMs + duracionH * 60L * 60L * 1000L
                    val expiraEnMs = finMs + MARGEN_POST_MS - ahoraMs
                    if (expiraEnMs <= 0) return@forEach // ya termino hace rato

                    candidatas.add(
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
                    )
                    // El primero por orden de fecha es el mas proximo: fija la expiracion.
                    expiraPorCancha.putIfAbsent(id, expiraEnMs)
                }

            // Dedup por id, descarta coords (0,0) y aplica el limite del sistema.
            GeofencePolicy.seleccionarCanchasParaGeofence(candidatas, MAX_GEOFENCES)
                .mapNotNull { c ->
                    val expira = expiraPorCancha[c.id] ?: return@mapNotNull null
                    GeofenceSpec(c, expira)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error consultando partidos del usuario", e)
            emptyList()
        }
    }

    private fun tienePermisosBackground(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            appContext, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val background = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                appContext, Manifest.permission.ACCESS_BACKGROUND_LOCATION
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
        // Se sigue considerando "proximo" un partido que empezo hasta 2h atras.
        private const val VENTANA_PREVIA_MS = 2L * 60L * 60L * 1000L
        // Margen tras el fin del partido antes de expirar la geocerca.
        private const val MARGEN_POST_MS = 30L * 60L * 1000L
    }
}
