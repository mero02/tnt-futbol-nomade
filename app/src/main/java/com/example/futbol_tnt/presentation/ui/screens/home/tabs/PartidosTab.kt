package com.example.futbol_tnt.presentation.ui.screens.home.tabs

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.futbol_tnt.data.model.EstadoPartido
import com.example.futbol_tnt.data.model.FiltroEstado
import com.example.futbol_tnt.data.model.FiltroFecha
import com.example.futbol_tnt.data.model.FiltroPartidos
import com.example.futbol_tnt.data.model.Partido
import com.example.futbol_tnt.presentation.ui.screens.home.components.EstadoPartidoBadge
import com.example.futbol_tnt.presentation.ui.screens.home.components.HeaderSection
import com.example.futbol_tnt.presentation.viewmodel.PartidoEvento
import com.example.futbol_tnt.presentation.viewmodel.PartidoViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val partidoFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

@Composable
internal fun PartidosTab(
    viewModel: PartidoViewModel,
    onCrearPartido: () -> Unit
) {
    var filtros by remember { mutableStateOf(FiltroPartidos()) }
    var showUnirseDialog by remember { mutableStateOf<Partido?>(null) }

    val todosLosPartidos by viewModel.partidos.collectAsState()
    val evento by viewModel.evento.collectAsState()

    val partidosFiltrados = remember(filtros, todosLosPartidos) {
        todosLosPartidos.filter { partido ->
            val filtroFechaOk = when (filtros.fecha) {
                FiltroFecha.HOY -> partido.fecha.toLocalDate() == LocalDate.now()
                FiltroFecha.ESTA_SEMANA -> {
                    val hoy = LocalDate.now()
                    partido.fecha.toLocalDate() in hoy..hoy.plusDays(7)
                }
                FiltroFecha.TODOS -> true
            }
            val filtroTipoOk = filtros.tipoCancha == null || partido.cancha.tipo == filtros.tipoCancha
            val filtroEstadoOk = when (filtros.estado) {
                FiltroEstado.ABIERTOS -> partido.estado == EstadoPartido.ABIERTO
                FiltroEstado.LLENOS -> partido.estado == EstadoPartido.LLENO
                FiltroEstado.TODOS -> true
            }
            filtroFechaOk && filtroTipoOk && filtroEstadoOk
        }
    }

    LaunchedEffect(evento) {
        when (evento) {
            is PartidoEvento.UnirseExito, is PartidoEvento.PartidoLleno -> {
                showUnirseDialog = null
                viewModel.limpiarEvento()
            }
            else -> {}
        }
    }

    showUnirseDialog?.let { partido ->
        UnirsePartidoDialog(
            partido = partido,
            onDismiss = { showUnirseDialog = null },
            onConfirm = { viewModel.unirseAPartido(partido.id) }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            HeaderSection(
                titulo = "Partidos",
                subtitulo = "${partidosFiltrados.size} partidos"
            )
        }
        item {
            FiltrosPartidos(
                filtros = filtros,
                onFiltrosChange = { filtros = it }
            )
        }
        items(partidosFiltrados, key = { it.id }) { partido ->
            PartidoCard(
                partido = partido,
                onUnirse = { showUnirseDialog = partido }
            )
        }
        item {
            Spacer(modifier = Modifier.height(8.dp))
            CrearPartidoCard(onClick = onCrearPartido)
        }
    }
}

@Composable
private fun FiltrosPartidos(
    filtros: FiltroPartidos,
    onFiltrosChange: (FiltroPartidos) -> Unit
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
            FilterChip(
                selected = filtros.fecha == FiltroFecha.ESTA_SEMANA,
                onClick = {
                    onFiltrosChange(filtros.copy(
                        fecha = if (filtros.fecha == FiltroFecha.ESTA_SEMANA) FiltroFecha.TODOS else FiltroFecha.ESTA_SEMANA
                    ))
                },
                label = { Text("Esta semana") },
                leadingIcon = if (filtros.fecha == FiltroFecha.ESTA_SEMANA) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else null
            )
            FilterChip(
                selected = filtros.estado == FiltroEstado.ABIERTOS,
                onClick = {
                    onFiltrosChange(filtros.copy(
                        estado = if (filtros.estado == FiltroEstado.ABIERTOS) FiltroEstado.TODOS else FiltroEstado.ABIERTOS
                    ))
                },
                label = { Text("Abiertos") },
                leadingIcon = if (filtros.estado == FiltroEstado.ABIERTOS) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else null
            )
        }
    }
}

@Composable
private fun PartidoCard(
    partido: Partido,
    onUnirse: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = partido.cancha.nombre,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                EstadoPartidoBadge(estado = partido.estado)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = partido.nombreLocal,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "LOCAL",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "VS",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = partido.nombreVisitante,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "VISITANTE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = partido.fecha.format(partidoFormatter),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(
                    text = "${partido.jugadoresActuales}/${partido.jugadoresMaximos} jugadores",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$${partido.precioPorPersona.toInt()}/persona",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                when (partido.estado) {
                    EstadoPartido.ABIERTO -> FilledTonalButton(onClick = onUnirse) { Text("Unirse") }
                    EstadoPartido.LLENO -> OutlinedButton(onClick = {}, enabled = false) { Text("Lleno") }
                    else -> {}
                }
            }
        }
    }
}

@Composable
private fun UnirsePartidoDialog(
    partido: Partido,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var nombreEquipo by remember { mutableStateOf("") }
    var posicionSeleccionada by remember { mutableStateOf("Delantero") }
    val posiciones = listOf("Delantero", "Mediocampista", "Defensor", "Arquero")
    var showPosiciones by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Unirse al partido",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${partido.nombreLocal} vs ${partido.nombreVisitante}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${partido.fecha.format(partidoFormatter)} - ${partido.cancha.nombre}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$${partido.precioPorPersona.toInt()}/persona",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                OutlinedTextField(
                    value = nombreEquipo,
                    onValueChange = { nombreEquipo = it },
                    label = { Text("Nombre de tu equipo") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Ej: Los Chetos FC") }
                )
                Column {
                    Text(
                        text = "Posición",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = SolidColor(MaterialTheme.colorScheme.outline)
                        ),
                        onClick = { showPosiciones = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(posicionSeleccionada)
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = showPosiciones,
                        onDismissRequest = { showPosiciones = false }
                    ) {
                        posiciones.forEach { posicion ->
                            DropdownMenuItem(
                                text = { Text(posicion) },
                                onClick = {
                                    posicionSeleccionada = posicion
                                    showPosiciones = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = nombreEquipo.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun CrearPartidoCard(onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.SportsSoccer,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Organizá tu partido",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Creá un partido y buscá rivales",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onClick) { Text("Crear Partido") }
        }
    }
}
