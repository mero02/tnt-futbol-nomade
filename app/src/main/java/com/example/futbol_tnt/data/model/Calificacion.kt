package com.example.futbol_tnt.data.model

import com.google.firebase.Timestamp

data class Calificacion(
    val id: String = "",
    val partidoId: String = "",
    val calificadorId: String = "",
    val calificadoId: String = "",
    val estrellas: Int = 0,
    val comentario: String? = null,
    val fecha: Timestamp = Timestamp.now()
)
