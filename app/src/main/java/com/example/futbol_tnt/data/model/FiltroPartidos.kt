package com.example.futbol_tnt.data.model

data class FiltroPartidos(
    val fecha: FiltroFecha = FiltroFecha.TODOS,
    val tipoCancha: TipoCancha? = null,
    val estado: FiltroEstado = FiltroEstado.TODOS
)

enum class FiltroFecha { HOY, ESTA_SEMANA, TODOS }
enum class FiltroEstado { ABIERTOS, LLENOS, TODOS }
