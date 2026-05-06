package com.example.futbol_tnt.presentation.ui.screens.home.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.futbol_tnt.data.model.EstadoPartido
import com.example.futbol_tnt.data.model.EstadoReserva

@Composable
internal fun StatusBadge(color: Color, texto: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.2f)
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
internal fun EstadoPartidoBadge(estado: EstadoPartido) {
    val (color, texto) = when (estado) {
        EstadoPartido.ABIERTO -> MaterialTheme.colorScheme.primary to "Abierto"
        EstadoPartido.LLENO -> MaterialTheme.colorScheme.tertiary to "Lleno"
        EstadoPartido.EN_JUEGO -> MaterialTheme.colorScheme.secondary to "En juego"
        EstadoPartido.FINALIZADO -> MaterialTheme.colorScheme.onSurfaceVariant to "Finalizado"
    }
    StatusBadge(color, texto)
}

@Composable
internal fun EstadoBadge(estado: EstadoReserva) {
    val (color, texto) = when (estado) {
        EstadoReserva.PENDIENTE -> MaterialTheme.colorScheme.tertiary to "Pendiente"
        EstadoReserva.CONFIRMADA -> MaterialTheme.colorScheme.primary to "Confirmada"
        EstadoReserva.CANCELADA -> MaterialTheme.colorScheme.error to "Cancelada"
        EstadoReserva.COMPLETADA -> MaterialTheme.colorScheme.secondary to "Completada"
    }
    StatusBadge(color, texto)
}
