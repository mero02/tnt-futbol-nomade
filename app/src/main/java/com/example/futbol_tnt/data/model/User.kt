package com.example.futbol_tnt.data.model

data class User(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
    val posicion: Posicion? = null,
    val nivel: NivelJuego? = null,
    val telefono: String? = null,
    val biografia: String? = null,
    val apodo: String? = null,
    val fechaNacimiento: String? = null,
    val sexo: Sexo? = null,
    val piernaDominante: PiernaDominante? = null,
    val formatoPreferido: FormatoPreferido? = null,
    val equipo: String? = null,
    val valoracionPromedio: Double = 0.0,
    val fcmToken: String? = null,
    val notificacionesCercania: Boolean = true,
    val notificacionesAceptacion: Boolean = true,
    val lastLat: Double? = null,
    val lastLng: Double? = null,
    val isBanned: Boolean = false
)

interface HasDisplayName {
    val displayName: String
}

enum class Posicion(override val displayName: String) : HasDisplayName {
    ARQUERO("Arquero"),
    DEFENSA("Defensa"),
    MEDIOCAMPISTA("Mediocampista"),
    DELANTERO("Delantero"),
    MULTIPOSICION("Multiposición")
}

enum class NivelJuego(override val displayName: String) : HasDisplayName {
    PRINCIPIANTE("Principiante"),
    INTERMEDIO("Intermedio"),
    AVANZADO("Avanzado"),
    PROFESIONAL("Profesional")
}

enum class Sexo(override val displayName: String) : HasDisplayName {
    HOMBRE("Hombre"),
    MUJER("Mujer"),
    OTRO("Otro")
}

enum class PiernaDominante(override val displayName: String) : HasDisplayName {
    DIESTRO("Diestro"),
    ZURDO("Zurdo"),
    AMBIDIESTRO("Ambidiestro")
}

enum class FormatoPreferido(override val displayName: String) : HasDisplayName {
    F5("Fútbol 5"),
    F7("Fútbol 7"),
    F11("Fútbol 11")
}
