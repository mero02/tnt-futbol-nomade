package com.example.futbol_tnt.data.repository

import com.example.futbol_tnt.data.model.Cancha
import com.example.futbol_tnt.data.model.EstadoReserva
import com.example.futbol_tnt.data.model.Notificacion
import com.example.futbol_tnt.data.model.TipoNotificacion
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

    override suspend fun cancelarReserva(reservaId: String) {
        val uid = auth.currentUser?.uid ?: return

        // 1. Obtener datos del partido y sus participantes antes de borrar
        val partidoQuery = firestore.collection("partidos")
            .whereEqualTo("reservaId", reservaId)
            .get()
            .await()

        val partidoDoc = partidoQuery.documents.firstOrNull()
        val participantesIds = partidoDoc?.get("participantesIds") as? List<String> ?: emptyList()
        val nombreCancha = partidoDoc?.get("cancha.nombre") as? String ?: "la cancha"

        firestore.runTransaction { transaction ->
            // 2. Borrar la reserva
            transaction.delete(reservasCol.document(reservaId))
        }.await()

        // 3. Borrar el partido y enviar notificaciones
        for (doc in partidoQuery.documents) {
            doc.reference.delete().await()
        }

        // 4. Enviar notificaciones a los participantes (excepto al que cancela)
        val notifCol = firestore.collection("notificaciones")
        participantesIds.forEach { targetUid ->
            if (targetUid != uid) {
                val notif = Notificacion(
                    userId = targetUid,
                    titulo = "Partido Cancelado",
                    mensaje = "El partido en $nombreCancha ha sido cancelado por el organizador.",
                    tipo = TipoNotificacion.CANCELACION
                )
                notifCol.add(mapOf(
                    "userId" to notif.userId,
                    "titulo" to notif.titulo,
                    "mensaje" to notif.mensaje,
                    "fecha" to notif.fecha,
                    "leido" to notif.leido,
                    "tipo" to notif.tipo.name
                ))
            }
        }
    }

    override suspend fun pagarReserva(reservaId: String, monto: Double) {
        // En una app real, aquí se incrementa el montoPagado y si llega al total se cambia el estado
        val ref = reservasCol.document(reservaId)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(ref)
            val total = snapshot.getDouble("precioTotal") ?: 0.0
            val pagadoActual = snapshot.getDouble("montoPagado") ?: 0.0
            val nuevoPagado = pagadoActual + monto

            val nuevoEstado = if (nuevoPagado >= total) EstadoReserva.CONFIRMADA.name else EstadoReserva.PENDIENTE.name

            transaction.update(ref, mapOf(
                "montoPagado" to nuevoPagado,
                "estado" to nuevoEstado
            ))
        }.await()
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
    "montoPagado" to montoPagado,
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
            montoPagado = getDouble("montoPagado") ?: 0.0,
            estado = parseEstadoReserva(getString("estado")),
            nombreEquipo = getString("nombreEquipo")
        )
    }.getOrNull()
}

private fun parseTipoCancha(raw: String?): TipoCancha =
    raw?.let { runCatching { TipoCancha.valueOf(it) }.getOrNull() } ?: TipoCancha.FUTBOL_5

private fun parseEstadoReserva(raw: String?): EstadoReserva =
    raw?.let { runCatching { EstadoReserva.valueOf(it) }.getOrNull() } ?: EstadoReserva.PENDIENTE
