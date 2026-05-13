package com.example.futbol_tnt.presentation.ui.screens.home.tabs

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.futbol_tnt.data.model.EstadoReserva
import com.example.futbol_tnt.data.model.FiltroEstadoReserva
import com.example.futbol_tnt.data.model.FiltroFecha
import com.example.futbol_tnt.data.model.FiltroReservas
import com.example.futbol_tnt.data.model.Reserva
import com.example.futbol_tnt.presentation.ui.screens.home.components.EstadoBadge
import com.example.futbol_tnt.presentation.ui.screens.home.components.HeaderSection
import com.example.futbol_tnt.presentation.viewmodel.ReservaViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val reservaFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

@Composable
internal fun MisReservasTab(
    viewModel: ReservaViewModel
) {
    val reservas by viewModel.reservas.collectAsState()
    var filtros by remember { mutableStateOf(FiltroReservas()) }

    val reservasFiltradas = remember(reservas, filtros) {
        reservas.filter { reserva ->
            val filtroFechaOk = when (filtros.fecha) {
                FiltroFecha.HOY -> reserva.fecha.toLocalDate() == LocalDate.now()
                FiltroFecha.ESTA_SEMANA -> {
                    val hoy = LocalDate.now()
                    reserva.fecha.toLocalDate() in hoy..hoy.plusDays(7)
                }
                FiltroFecha.TODOS -> true
            }
            val filtroEstadoOk = when (filtros.estado) {
                FiltroEstadoReserva.PENDIENTES -> reserva.estado == EstadoReserva.PENDIENTE
                FiltroEstadoReserva.CONFIRMADAS -> reserva.estado == EstadoReserva.CONFIRMADA
                FiltroEstadoReserva.COMPLETADAS -> reserva.estado == EstadoReserva.COMPLETADA
                FiltroEstadoReserva.TODOS -> true
            }
            filtroFechaOk && filtroEstadoOk
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            HeaderSection(
                titulo = "Mis Reservas",
                subtitulo = "${reservasFiltradas.size} reservas"
            )
        }

        item {
            FiltrosReservas(
                filtros = filtros,
                onFiltrosChange = { filtros = it }
            )
        }

        if (reservasFiltradas.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (reservas.isEmpty()) "Aún no tenés reservas realizadas." else "No hay reservas que coincidan con los filtros.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(reservasFiltradas, key = { it.id }) { reserva ->
                ReservaCard(reserva = reserva)
            }
        }
    }
}

@Composable
private fun FiltrosReservas(
    filtros: FiltroReservas,
    onFiltrosChange: (FiltroReservas) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Filtros",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Filtro Fecha
            FilterChip(
                selected = filtros.fecha == FiltroFecha.HOY,
                onClick = {
                    onFiltrosChange(filtros.copy(
                        fecha = if (filtros.fecha == FiltroFecha.HOY) FiltroFecha.TODOS else FiltroFecha.HOY
                    ))
                },
                label = { Text("Hoy") },
                leadingIcon = if (filtros.fecha == FiltroFecha.HOY) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else null
            )

            // Filtro Estado: Confirmadas
            FilterChip(
                selected = filtros.estado == FiltroEstadoReserva.CONFIRMADAS,
                onClick = {
                    onFiltrosChange(filtros.copy(
                        estado = if (filtros.estado == FiltroEstadoReserva.CONFIRMADAS) FiltroEstadoReserva.TODOS else FiltroEstadoReserva.CONFIRMADAS
                    ))
                },
                label = { Text("Confirmadas") },
                leadingIcon = if (filtros.estado == FiltroEstadoReserva.CONFIRMADAS) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else null
            )

            // Filtro Estado: Pendientes
            FilterChip(
                selected = filtros.estado == FiltroEstadoReserva.PENDIENTES,
                onClick = {
                    onFiltrosChange(filtros.copy(
                        estado = if (filtros.estado == FiltroEstadoReserva.PENDIENTES) FiltroEstadoReserva.TODOS else FiltroEstadoReserva.PENDIENTES
                    ))
                },
                label = { Text("Pendientes") },
                leadingIcon = if (filtros.estado == FiltroEstadoReserva.PENDIENTES) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else null
            )
        }
    }
}

@Composable
private fun ReservaCard(reserva: Reserva) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = reserva.cancha.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                EstadoBadge(estado = reserva.estado)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = reserva.fecha.format(reservaFormatter),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${reserva.duracionHoras}h",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (reserva.nombreEquipo != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = reserva.nombreEquipo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Total: $${reserva.precioTotal.toInt()}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
