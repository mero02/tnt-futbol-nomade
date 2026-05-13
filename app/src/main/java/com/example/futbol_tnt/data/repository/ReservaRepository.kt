package com.example.futbol_tnt.data.repository

import com.example.futbol_tnt.data.model.Cancha
import com.example.futbol_tnt.data.model.EstadoReserva
import com.example.futbol_tnt.data.model.Reserva
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

class ReservaRepository(
    private val firestore: FirebaseFirestore = Firebase.firestore,
    private val auth: FirebaseAuth = Firebase.auth,
) : IReservaRepository {

    private val reservasCol get() = firestore.collection(COLLECTION)

    override val reservas: Flow<List<Reserva>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            return@callbackFlow
        }

        val listener = reservasCol
            .whereEqualTo("usuarioId", uid)
            // Quitamos el orderBy temporalmente para evitar el crash por falta de índice
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val lista = snapshot?.documents
                    ?.mapNotNull { it.toReservaOrNull() }
                    .orEmpty()
                trySend(lista)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun crearReserva(reserva: Reserva): String {
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("Necesitas iniciar sesion para reservar")

        val id = if (reserva.id.isBlank()) reservasCol.document().id else reserva.id
        val conMetadata = reserva.copy(id = id, usuarioId = uid)

        reservasCol.document(id).set(conMetadata.toFirestoreMap()).await()
        return id
    }

    override suspend fun getReservaById(id: String): Reserva? {
        return reservasCol.document(id).get().await().toReservaOrNull()
    }

    private companion object {
        const val COLLECTION = "reservas"
    }
}

private fun Reserva.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "usuarioId" to usuarioId,
    "fecha" to Timestamp(
        Date.from(fecha.atZone(ZoneId.systemDefault()).toInstant())
    ),
    "cancha" to mapOf(
        "id" to cancha.id,
        "nombre" to cancha.nombre,
        "direccion" to cancha.direccion,
        "precioPorHora" to cancha.precioPorHora,
        "tipo" to cancha.tipo.name,
    ),
    "duracionHoras" to duracionHoras,
    "precioTotal" to precioTotal,
    "estado" to estado.name,
    "nombreEquipo" to nombreEquipo,
)

private fun DocumentSnapshot.toReservaOrNull(): Reserva? {
    return runCatching {
        val canchaMap = get("cancha") as? Map<String, Any?>
            ?: return@runCatching null
        val fechaTimestamp = getTimestamp("fecha")
            ?: return@runCatching null

        Reserva(
            id = getString("id") ?: id,
            usuarioId = getString("usuarioId").orEmpty(),
            fecha = fechaTimestamp.toDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime(),
            cancha = Cancha(
                id = canchaMap["id"] as? String ?: "",
                nombre = canchaMap["nombre"] as? String ?: "",
                direccion = canchaMap["direccion"] as? String ?: "",
                precioPorHora = (canchaMap["precioPorHora"] as? Number)?.toDouble() ?: 0.0,
                tipo = parseTipoCancha(canchaMap["tipo"] as? String),
            ),
            duracionHoras = (getLong("duracionHoras") ?: 1L).toInt(),
            precioTotal = getDouble("precioTotal") ?: 0.0,
            estado = parseEstadoReserva(getString("estado")),
            nombreEquipo = getString("nombreEquipo")
        )
    }.getOrNull()
}

private fun parseTipoCancha(raw: String?): TipoCancha =
    raw?.let { runCatching { TipoCancha.valueOf(it) }.getOrNull() } ?: TipoCancha.FUTBOL_5

private fun parseEstadoReserva(raw: String?): EstadoReserva =
    raw?.let { runCatching { EstadoReserva.valueOf(it) }.getOrNull() } ?: EstadoReserva.PENDIENTE
