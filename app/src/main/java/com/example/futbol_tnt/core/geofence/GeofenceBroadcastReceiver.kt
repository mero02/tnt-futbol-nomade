package com.example.futbol_tnt.core.geofence

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.futbol_tnt.R
import com.example.futbol_tnt.data.model.TipoEventoGeofence
import com.example.futbol_tnt.data.repository.GeofenceEventRepository
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import android.app.PendingIntent
import com.example.futbol_tnt.MainActivity

/**
 * Recibe las transiciones de geocerca del sistema (HU-38).
 *
 * Al detectar un ENTER identifica la cancha (requestId) y escribe un evento en
 * `geofence_events`. Usa goAsync() para tener margen mientras persiste en
 * Firestore, ya que el broadcast se ejecuta con la app potencialmente cerrada.
 */
class GeofenceBroadcastReceiver : BroadcastReceiver() {

    private val eventRepository = GeofenceEventRepository()

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return

        if (geofencingEvent.hasError()) {
            Log.e(TAG, "Error en evento de geocerca: ${geofencingEvent.errorCode}")
            return
        }

        // Solo nos interesa la entrada a la cancha.
        if (geofencingEvent.geofenceTransition != Geofence.GEOFENCE_TRANSITION_ENTER) return

        val triggering = geofencingEvent.triggeringGeofences ?: return
        val uid = Firebase.auth.currentUser?.uid ?: run {
            Log.w(TAG, "Evento de geocerca sin usuario logueado; se ignora")
            return
        }

        val lat = geofencingEvent.triggeringLocation?.latitude
        val lng = geofencingEvent.triggeringLocation?.longitude
        val diaKey = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                triggering.forEach { geofence ->
                    val canchaId = geofence.requestId
                    // Firestore encola la escritura localmente de inmediato y la
                    // sincroniza al recuperar la red (cola offline propia del SDK).
                    runCatching {
                        eventRepository.registrarEventoAutomatico(
                            userId = uid,
                            canchaId = canchaId,
                            tipo = TipoEventoGeofence.ENTER,
                            lat = lat,
                            lng = lng,
                            diaKey = diaKey,
                        )
                    }
                }
                mostrarNotificacion(context)
            } catch (e: Exception) {
                Log.e(TAG, "Error procesando geocerca", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun mostrarNotificacion(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "Sin permiso POST_NOTIFICATIONS; se omite la notificacion de llegada")
            return
        }

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Llegadas a la cancha",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifica cuando entras al predio deportivo"
                enableLights(true)
                lightColor = android.graphics.Color.GREEN
                enableVibration(true)
            }
            nm.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("⚽ ¡Ya estás en la cancha!")
            .setContentText("Registramos tu llegada correctamente. El equipo ya sabe que estás aquí.")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        nm.notify(NOTIF_ID, notif)
    }

    private companion object {
        const val TAG = "GeofenceReceiver"
        const val CHANNEL_ID = "geofence_channel"
        const val NOTIF_ID = 2001
    }
}
