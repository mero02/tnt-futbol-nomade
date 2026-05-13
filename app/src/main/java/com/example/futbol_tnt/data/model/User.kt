package com.example.futbol_tnt.data.model

data class User(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
    val posicion: Posicion? = null,
    val nivel: NivelJuego? = null,
    val telefono: String? = null,
    val biografia: String? = null
)

enum class Posicion {
    ARQUERO,
    DEFENSA,
    MEDIOCAMPISTA,
    DELANTERO,
    MULTIPOSICION
}

enum class NivelJuego {
    PRINCIPIANTE,
    INTERMEDIO,
    AVANZADO,
    PROFESIONAL
}
