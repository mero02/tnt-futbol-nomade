package com.example.futbol_tnt.data.repository

import com.example.futbol_tnt.data.model.Cancha
import com.example.futbol_tnt.data.model.EstadoPartido
import com.example.futbol_tnt.data.model.Partido
import com.example.futbol_tnt.data.model.TipoCancha
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
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
 *
 * - partidos: Flow en tiempo real (snapshot listener) ordenado por fecha asc.
 * - crearPartido: upsert con id autogenerado si viene vacio. Inyecta creatorId desde Firebase Auth.
 * - unirseAPartido: transaccion atomica para evitar race conditions cuando dos
 *   personas se unen casi al mismo tiempo.
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

        // Si el partido viene sin id (caso comun al crear desde el form), generamos uno
        // unico via .document() — asi el id local matchea con el de Firestore.
        val id = partido.id.ifBlank { partidosCol.document().id }
        val conMetadata = partido.copy(id = id, creatorId = uid)

        partidosCol.document(id).set(conMetadata.toFirestoreMap()).await()
    }

    override suspend fun unirseAPartido(partidoId: String): Boolean {
        val ref = partidosCol.document(partidoId)
        return firestore.runTransaction { tx ->
            val snap = tx.get(ref)
            val actuales = (snap.getLong("jugadoresActuales") ?: 0L).toInt()
            val maximos = (snap.getLong("jugadoresMaximos") ?: 0L).toInt()
            if (actuales >= maximos) return@runTransaction false

            val nuevos = actuales + 1
            val nuevoEstado = if (nuevos >= maximos) {
                EstadoPartido.LLENO.name
            } else {
                snap.getString("estado") ?: EstadoPartido.ABIERTO.name
            }
            tx.update(
                ref,
                mapOf(
                    "jugadoresActuales" to nuevos,
                    "estado" to nuevoEstado,
                )
            )
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

// ---------------------------------------------------------------------------
// Mappers Partido <-> Firestore.
//
// Hacemos el mapeo a mano (en vez de usar .toObject<Partido>()) por:
// - LocalDateTime no es serializable nativo de Firestore (necesita Timestamp).
// - La cancha embebida tiene LocalTime en disponibilidad, que tampoco serializa.
// - Mantenemos control sobre defaults y deserializacion defensiva.
// ---------------------------------------------------------------------------

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
        )
    }.getOrNull()
}

private fun parseTipoCancha(raw: String?): TipoCancha =
    raw?.let { runCatching { TipoCancha.valueOf(it) }.getOrNull() } ?: TipoCancha.FUTBOL_5

private fun parseEstadoPartido(raw: String?): EstadoPartido =
    raw?.let { runCatching { EstadoPartido.valueOf(it) }.getOrNull() } ?: EstadoPartido.ABIERTO
