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

/**
 * Repositorio de partidos persistidos en Cloud Firestore (coleccion "partidos").
 */
class PartidoRepository(
    private val firestore: FirebaseFirestore = Firebase.firestore,
    private val auth: FirebaseAuth = Firebase.auth,
) : IPartidoRepository {

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
            val actuales = (snap.getLong("jugadoresActuales") ?: 0L).toInt()
            val maximos = (snap.getLong("jugadoresMaximos") ?: 0L).toInt()
            val participantes = snap.get("participantesIds") as? List<*> ?: emptyList<String>()
            val solicitudes = snap.get("solicitudesIds") as? List<*> ?: emptyList<String>()

            if (actuales >= maximos) return@runTransaction false
            if (participantes.contains(uid)) return@runTransaction false
            if (solicitudes.contains(uid)) return@runTransaction false

            tx.update(ref, "solicitudesIds", FieldValue.arrayUnion(uid))
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
            }
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
    "solicitudesIds" to solicitudesIds
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
            solicitudesIds = get("solicitudesIds") as? List<String> ?: emptyList()
        )
    }.getOrNull()
}

private fun parseTipoCancha(raw: String?): TipoCancha =
    raw?.let { runCatching { TipoCancha.valueOf(it) }.getOrNull() } ?: TipoCancha.FUTBOL_5

private fun parseEstadoPartido(raw: String?): EstadoPartido =
    raw?.let { runCatching { EstadoPartido.valueOf(it) }.getOrNull() } ?: EstadoPartido.ABIERTO
