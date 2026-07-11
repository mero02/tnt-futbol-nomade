package com.example.futbol_tnt.data.repository

import com.example.futbol_tnt.data.model.Cancha
import com.example.futbol_tnt.data.model.EstadoPartido
import com.example.futbol_tnt.data.model.Partido
import com.example.futbol_tnt.data.model.TipoCancha
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.ZoneId
import java.util.Date

import java.time.format.DateTimeFormatter

/**
 * Repositorio de partidos persistidos en Cloud Firestore (coleccion "partidos").
 */
class PartidoRepository(
    private val firestore: FirebaseFirestore = Firebase.firestore,
    private val auth: FirebaseAuth = Firebase.auth,
) : IPartidoRepository {

    private val notifFormatter = DateTimeFormatter.ofPattern("dd/MM HH:mm")
    private val partidosCol get() = firestore.collection(COLLECTION)

    override val partidos: Flow<List<Partido>> = callbackFlow {
        val listener = partidosCol
            .orderBy("fecha", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val lista = snapshot?.documents
                    ?.mapNotNull { it.toPartidoOrNull() }
                    .orEmpty()
                trySend(lista)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun crearPartido(partido: Partido) {
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("Necesitas iniciar sesion para crear un partido")

        val id = partido.id.ifBlank { partidosCol.document().id }

        // El creador se agrega automáticamente como participante
        val conMetadata = partido.copy(
            id = id,
            creatorId = uid,
            participantesIds = listOf(uid),
            jugadoresActuales = 1
        )

        partidosCol.document(id).set(conMetadata.toFirestoreMap()).await()
    }

    override suspend fun unirseAPartido(partidoId: String): Boolean {
        // Mantenemos por compatibilidad, pero ahora redirige a enviarSolicitud o lanza error si prefieres
        return enviarSolicitud(partidoId)
    }

    override suspend fun enviarSolicitud(partidoId: String): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        val ref = partidosCol.document(partidoId)

        return firestore.runTransaction { tx ->
            val snap = tx.get(ref)
            if (!snap.exists()) return@runTransaction false

            val actuales = (snap.getLong("jugadoresActuales") ?: 0L).toInt()
            val maximos = (snap.getLong("jugadoresMaximos") ?: 0L).toInt()
            val participantes = snap.get("participantesIds") as? List<*> ?: emptyList<String>()
            val solicitudes = snap.get("solicitudesIds") as? List<*> ?: emptyList<String>()

            if (actuales >= maximos) return@runTransaction false
            if (participantes.contains(uid)) return@runTransaction false
            if (solicitudes.contains(uid)) return@runTransaction false

            tx.update(ref, "solicitudesIds", FieldValue.arrayUnion(uid))

            // Notificar al organizador sobre la nueva solicitud
            val creatorId = snap.getString("creatorId") ?: ""
            if (creatorId.isNotBlank()) {
                val notifCol = firestore.collection("notificaciones")
                val canchaMap = snap.get("cancha") as? Map<*, *>
                val nombreCancha = canchaMap?.get("nombre") as? String ?: "la cancha"

                val newNotifRef = notifCol.document()
                tx.set(newNotifRef, mapOf(
                    "userId" to creatorId,
                    "titulo" to "Nueva Solicitud",
                    "mensaje" to "Un jugador quiere unirse a tu partido en $nombreCancha.",
                    "fecha" to Timestamp.now(),
                    "leido" to false,
                    "tipo" to "SOLICITUD_RECIBIDA",
                    "partidoId" to partidoId
                ))
            }
            true
        }.await()
    }

    override suspend fun gestionarSolicitud(partidoId: String, applicantId: String, aceptar: Boolean): Boolean {
        val ref = partidosCol.document(partidoId)

        return firestore.runTransaction { tx ->
            val snap = tx.get(ref)
            val actuales = (snap.getLong("jugadoresActuales") ?: 0L).toInt()
            val maximos = (snap.getLong("jugadoresMaximos") ?: 0L).toInt()

            // Primero removemos de la lista de solicitudes
            tx.update(ref, "solicitudesIds", FieldValue.arrayRemove(applicantId))

            if (aceptar) {
                if (actuales >= maximos) return@runTransaction false

                val nuevos = actuales + 1
                val nuevoEstado = if (nuevos >= maximos) EstadoPartido.LLENO.name else snap.getString("estado")

                tx.update(ref, mapOf(
                    "participantesIds" to FieldValue.arrayUnion(applicantId),
                    "jugadoresActuales" to nuevos,
                    "estado" to nuevoEstado
                ))

                // Notificar al jugador que fue aceptado
                val notifCol = firestore.collection("notificaciones")
                val canchaMap = snap.get("cancha") as? Map<*, *>
                val nombreCancha = canchaMap?.get("nombre") as? String ?: "la cancha"

                val nombrePartido = "${snap.getString("nombreLocal")} vs ${snap.getString("nombreVisitante")}"
                val fechaTimestamp = snap.getTimestamp("fecha")
                val localDateTime = fechaTimestamp?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDateTime()
                val fechaStr = localDateTime?.format(notifFormatter) ?: ""

                val newNotifRef = notifCol.document()
                tx.set(newNotifRef, mapOf(
                    "userId" to applicantId,
                    "titulo" to "¡Solicitud Aprobada!",
                    "mensaje" to "Has sido aceptado en $nombrePartido ($nombreCancha) para el $fechaStr. ¡A jugar!",
                    "fecha" to Timestamp.now(),
                    "leido" to false,
                    "tipo" to "SOLICITUD_APROBADA",
                    "partidoId" to partidoId
                ))
            } else {
                // Notificar al jugador que fue rechazado
                val notifCol = firestore.collection("notificaciones")
                val canchaMap = snap.get("cancha") as? Map<*, *>
                val nombreCancha = canchaMap?.get("nombre") as? String ?: "la cancha"

                val nombrePartido = "${snap.getString("nombreLocal")} vs ${snap.getString("nombreVisitante")}"

                val newNotifRef = notifCol.document()
                tx.set(newNotifRef, mapOf(
                    "userId" to applicantId,
                    "titulo" to "Solicitud Rechazada",
                    "mensaje" to "Tu solicitud para el partido $nombrePartido en $nombreCancha no ha sido aprobada.",
                    "fecha" to Timestamp.now(),
                    "leido" to false,
                    "tipo" to "SOLICITUD_RECHAZADA",
                    "partidoId" to partidoId
                ))
            }
            true
        }.await()
    }

    override suspend fun abandonarPartido(partidoId: String): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        val ref = partidosCol.document(partidoId)

        return firestore.runTransaction { tx ->
            val snap = tx.get(ref)
            val creatorId = snap.getString("creatorId") ?: ""
            val participantes = snap.get("participantesIds") as? List<*> ?: emptyList<String>()
            val actuales = (snap.getLong("jugadoresActuales") ?: 0L).toInt()

            // El organizador no puede abandonar su propio partido
            if (uid == creatorId) return@runTransaction false
            // Solo si es participante puede abandonar
            if (!participantes.contains(uid)) return@runTransaction false

            val nuevos = actuales - 1
            tx.update(ref, mapOf(
                "participantesIds" to FieldValue.arrayRemove(uid),
                "jugadoresActuales" to nuevos,
                "estado" to EstadoPartido.ABIERTO.name // Siempre vuelve a abierto si alguien sale
            ))

            // Notificar al organizador
            val notifCol = firestore.collection("notificaciones")
            val canchaMap = snap.get("cancha") as? Map<*, *>
            val nombreCancha = canchaMap?.get("nombre") as? String ?: "la cancha"

            val notifData = mapOf(
                "userId" to creatorId,
                "titulo" to "Jugador abandonó el partido",
                "mensaje" to "Un jugador ha salido del partido en $nombreCancha. Hay un nuevo lugar disponible.",
                "fecha" to Timestamp.now(),
                "leido" to false,
                "tipo" to "INFO",
                "partidoId" to partidoId
            )
            val newNotifRef = notifCol.document()
            tx.set(newNotifRef, notifData)

            true
        }.await()
    }

    override suspend fun getPartidoByReservaId(reservaId: String): Partido? {
        return partidosCol
            .whereEqualTo("reservaId", reservaId)
            .limit(1)
            .get()
            .await()
            .documents
            .firstOrNull()
            ?.toPartidoOrNull()
    }

    override suspend fun getPartidoById(id: String): Partido? {
        return try {
            partidosCol.document(id).get().await().toPartidoOrNull()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun setUrgencia(partidoId: String, esUrgente: Boolean): Boolean {
        return try {
            val partidoRef = partidosCol.document(partidoId)
            partidoRef.update("esUrgente", esUrgente).await()

            if (esUrgente) {
                // Disparar notificaciones a jugadores cercanos
                val snap = partidoRef.get().await()
                val partido = snap.toPartidoOrNull() ?: return true

                // Buscamos usuarios con notificaciones activas
                val usersSnap = firestore.collection("users")
                    .whereEqualTo("notificacionesCercania", true)
                    .get().await()

                val notifCol = firestore.collection("notificaciones")
                val batch = firestore.batch()

                usersSnap.documents.forEach { userDoc ->
                    val userId = userDoc.id
                    // No notificar al propio creador
                    if (userId == partido.creatorId) return@forEach

                    val userLat = userDoc.getDouble("lastLat")
                    val userLng = userDoc.getDouble("lastLng")

                    if (userLat != null && userLng != null) {
                        val results = FloatArray(1)
                        android.location.Location.distanceBetween(
                            partido.cancha.lat, partido.cancha.lng,
                            userLat, userLng,
                            results
                        )

                        // Si está a menos de 15km (ajustable)
                        if (results[0] <= 15000) {
                            val notifRef = notifCol.document()
                            batch.set(notifRef, mapOf(
                                "userId" to userId,
                                "titulo" to "🚨 ¡PARTIDO URGENTE!",
                                "mensaje" to "Se necesita un jugador urgente para ${partido.nombreLocal} vs ${partido.nombreVisitante} en ${partido.cancha.nombre}.",
                                "fecha" to Timestamp.now(),
                                "leido" to false,
                                "tipo" to "SOLICITUD_URGENTE",
                                "partidoId" to partidoId
                            ))
                        }
                    }
                }
                batch.commit().await()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    private companion object {
        const val COLLECTION = "partidos"
    }
}

private fun Partido.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "nombreLocal" to nombreLocal,
    "nombreVisitante" to nombreVisitante,
    "fecha" to Timestamp(
        Date.from(fecha.atZone(ZoneId.systemDefault()).toInstant())
    ),
    "cancha" to mapOf(
        "id" to cancha.id,
        "nombre" to cancha.nombre,
        "direccion" to cancha.direccion,
        "ciudad" to cancha.ciudad,
        "lat" to cancha.lat,
        "lng" to cancha.lng,
        "precioPorHora" to cancha.precioPorHora,
        "tipo" to cancha.tipo.name,
        "imagenUrl" to cancha.imagenUrl,
    ),
    "duracionHoras" to duracionHoras,
    "precioPorPersona" to precioPorPersona,
    "jugadoresActuales" to jugadoresActuales,
    "jugadoresMaximos" to jugadoresMaximos,
    "estado" to estado.name,
    "nombreOrganizador" to nombreOrganizador,
    "reservaId" to reservaId,
    "creatorId" to creatorId,
    "participantesIds" to participantesIds,
    "solicitudesIds" to solicitudesIds,
    "esUrgente" to esUrgente
)

@Suppress("UNCHECKED_CAST")
private fun DocumentSnapshot.toPartidoOrNull(): Partido? {
    return runCatching {
        val canchaMap = get("cancha") as? Map<String, Any?>
            ?: return@runCatching null
        val fechaTimestamp = getTimestamp("fecha")
            ?: return@runCatching null

        Partido(
            id = getString("id") ?: id,
            nombreLocal = getString("nombreLocal").orEmpty(),
            nombreVisitante = getString("nombreVisitante").orEmpty(),
            fecha = fechaTimestamp.toDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime(),
            cancha = Cancha(
                id = canchaMap["id"] as? String ?: "",
                nombre = canchaMap["nombre"] as? String ?: "",
                direccion = canchaMap["direccion"] as? String ?: "",
                ciudad = canchaMap["ciudad"] as? String ?: "",
                lat = (canchaMap["lat"] as? Number)?.toDouble() ?: 0.0,
                lng = (canchaMap["lng"] as? Number)?.toDouble() ?: 0.0,
                precioPorHora = (canchaMap["precioPorHora"] as? Number)?.toDouble() ?: 0.0,
                tipo = parseTipoCancha(canchaMap["tipo"] as? String),
                imagenUrl = canchaMap["imagenUrl"] as? String,
            ),
            duracionHoras = (getLong("duracionHoras") ?: 1L).toInt(),
            precioPorPersona = getDouble("precioPorPersona") ?: 0.0,
            jugadoresActuales = (getLong("jugadoresActuales") ?: 0L).toInt(),
            jugadoresMaximos = (getLong("jugadoresMaximos") ?: 0L).toInt(),
            estado = parseEstadoPartido(getString("estado")),
            nombreOrganizador = getString("nombreOrganizador").orEmpty(),
            reservaId = getString("reservaId"),
            creatorId = getString("creatorId").orEmpty(),
            participantesIds = get("participantesIds") as? List<String> ?: emptyList(),
            solicitudesIds = get("solicitudesIds") as? List<String> ?: emptyList(),
            esUrgente = getBoolean("esUrgente") ?: false
        )
    }.getOrNull()
}

private fun parseTipoCancha(raw: String?): TipoCancha =
    raw?.let { runCatching { TipoCancha.valueOf(it) }.getOrNull() } ?: TipoCancha.FUTBOL_5

private fun parseEstadoPartido(raw: String?): EstadoPartido =
    raw?.let { runCatching { EstadoPartido.valueOf(it) }.getOrNull() } ?: EstadoPartido.ABIERTO
