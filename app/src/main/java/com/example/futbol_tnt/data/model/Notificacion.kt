package com.example.futbol_tnt.data.model

import com.google.firebase.Timestamp

data class Notificacion(
    val id: String = "",
    val userId: String = "",
    val titulo: String = "",
    val mensaje: String = "",
    val fecha: Timestamp = Timestamp.now(),
    val leido: Boolean = false,
    val tipo: TipoNotificacion = TipoNotificacion.INFO,
    val partidoId: String? = null
)

enum class TipoNotificacion {
    CANCELACION,
    SOLICITUD_RECIBIDA,
    SOLICITUD_APROBADA,
    SOLICITUD_RECHAZADA,
    INFO
}
