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

        val fechaNormalizada = reserva.fecha.withSecond(0).withNano(0)
        val slotId = "slot_${reserva.cancha.id}_$fechaNormalizada"
        val conMetadata = reserva.copy(id = slotId, usuarioId = uid, fecha = fechaNormalizada)

        // Verificamos solapamiento antes de guardar
        val inicioDia = fechaNormalizada.toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant()
        val finDia = fechaNormalizada.toLocalDate().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()

        val reservasExistentes = reservasCol
            .whereEqualTo("cancha.id", reserva.cancha.id)
            .whereGreaterThanOrEqualTo("fecha", Timestamp(Date.from(inicioDia)))
            .whereLessThan("fecha", Timestamp(Date.from(finDia)))
            .get()
            .await()
            .documents
            .mapNotNull { it.toReservaOrNull() }

        val nuevoInicio = reserva.fecha.toLocalTime()
        val nuevoFin = nuevoInicio.plusMinutes((reserva.duracionHoras * 60).toLong())

        val haySolapamiento = reservasExistentes.any { r ->
            val exInicio = r.fecha.toLocalTime()
            val exFin = exInicio.plusMinutes((r.duracionHoras * 60).toLong())
            nuevoInicio.isBefore(exFin) && nuevoFin.isAfter(exInicio)
        }

        if (haySolapamiento) {
            throw IllegalStateException("Este horario se solapa con una reserva existente.")
        }

        reservasCol.document(slotId).set(conMetadata.toFirestoreMap()).await()
        return slotId
    }

    override suspend fun getReservaById(id: String): Reserva? {
        return reservasCol.document(id).get().await().toReservaOrNull()
    }

    override suspend fun getReservasPorCanchaYFecha(canchaId: String, fecha: java.time.LocalDate): List<Reserva> {
        val inicioDia = fecha.atStartOfDay(ZoneId.systemDefault()).toInstant()
        val finDia = fecha.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()

        return reservasCol
            .whereEqualTo("cancha.id", canchaId)
            .whereGreaterThanOrEqualTo("fecha", Timestamp(Date.from(inicioDia)))
            .whereLessThan("fecha", Timestamp(Date.from(finDia)))
            .get()
            .await()
            .documents
            .mapNotNull { it.toReservaOrNull() }
    }

    private companion object {
        const val COLLECTION = "reservas"
    }
}

private fun Reserva.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "usuarioId" to usuarioId,
    "fecha" to Timestamp(
        Date.from(fecha.atZone(ZoneId.systemDefault()).toInstant()),
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
        val canchaMap = (get("cancha") as? Map<String, Any?>)
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
            duracionHoras = (get("duracionHoras") as? Number)?.toDouble() ?: 1.0,
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
