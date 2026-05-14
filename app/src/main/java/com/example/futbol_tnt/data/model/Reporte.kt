package com.example.futbol_tnt.data.model

import com.google.firebase.Timestamp

data class Reporte(
    val id: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val tipo: TipoReporte = TipoReporte.ERROR,
    val descripcion: String = "",
    val fecha: Timestamp = Timestamp.now(),
    val dispositivo: String = ""
)

enum class TipoReporte(val displayName: String) {
    ERROR("Error / Bug"),
    SUGERENCIA("Sugerencia"),
    OTRO("Otro")
}
