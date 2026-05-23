package com.example.futbol_tnt.presentation.ui.screens.home.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.futbol_tnt.data.model.EstadoReserva
import com.example.futbol_tnt.data.model.FiltroEstadoReserva
import com.example.futbol_tnt.data.model.FiltroFecha
import com.example.futbol_tnt.data.model.FiltroReservas
import com.example.futbol_tnt.data.model.Reserva
import com.example.futbol_tnt.presentation.ui.screens.home.components.EstadoBadge
import com.example.futbol_tnt.presentation.ui.screens.home.components.HeaderSection
import com.example.futbol_tnt.presentation.viewmodel.MisReservasEvento
import com.example.futbol_tnt.presentation.viewmodel.ReservaViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val reservaFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

@Composable
internal fun MisReservasTab(
    viewModel: ReservaViewModel,
    onOrganizarPartido: (String) -> Unit
) {
    val reservas by viewModel.reservas.collectAsState()
    val rawEvento by viewModel.evento.collectAsState()
    var filtros by rememberSaveable(
        stateSaver = Saver<FiltroReservas, Any>(
            save = { listOf(it.fecha.name, it.estado.name) },
            restore = { values ->
                val list = values as List<String>
                FiltroReservas(
                    fecha = FiltroFecha.valueOf(list[0]),
                    estado = FiltroEstadoReserva.valueOf(list[1])
                )
            }
        )
    ) { mutableStateOf(FiltroReservas()) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Estados para diálogos
    var reservaParaCancelar by remember { mutableStateOf<Reserva?>(null) }
    var reservaParaPagar by remember { mutableStateOf<Reserva?>(null) }
    var reservaParaTicket by remember { mutableStateOf<Reserva?>(null) }
    var isPaying by remember { mutableStateOf(false) }

    LaunchedEffect(rawEvento) {
        val evento = rawEvento
        if (evento is MisReservasEvento.Error) {
            snackbarHostState.showSnackbar(evento.mensaje)
            viewModel.limpiarEvento()
        }
    }

    // Diálogo de Confirmación de Cancelación
    reservaParaCancelar?.let { reserva ->
        AlertDialog(
            onDismissRequest = { reservaParaCancelar = null },
            title = { Text("¿Cancelar reserva?") },
            text = { Text("Esta acción es irreversible, borrará cualquier partido organizado asociado y liberará la cancha para otros jugadores.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.cancelarReserva(reserva.id)
                        reservaParaCancelar = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sí, cancelar")
                }
            },
            dismissButton = {
                TextButton(onClick = { reservaParaCancelar = null }) {
                    Text("No, volver")
                }
            }
        )
    }

    // Diálogo de Pago de Saldo (Simulado)
    reservaParaPagar?.let { reserva ->
        val saldo = reserva.precioTotal - reserva.montoPagado
        AlertDialog(
            onDismissRequest = { if (!isPaying) reservaParaPagar = null },
            title = { Text("Pagar saldo pendiente") },
            text = {
                Column {
                    Text("Estás por pagar el saldo restante de la reserva en ${reserva.cancha.nombre}.")
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Saldo a pagar:", fontWeight = FontWeight.Bold)
                        Text("$${saldo.toInt()}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { isPaying = true },
                    enabled = !isPaying
                ) {
                    if (isPaying) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                    } else {
                        Text("Pagar con Tarjeta")
                    }
                }
            },
            dismissButton = {
                if (!isPaying) {
                    TextButton(onClick = { reservaParaPagar = null }) {
                        Text("Cancelar")
                    }
                }
            }
        )

        // Simulación de delay de pago
        if (isPaying) {
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(2000)
                viewModel.pagarSaldo(reserva.id, saldo)
                isPaying = false
                reservaParaPagar = null
            }
        }
    }

    // Diálogo de Ticket
    reservaParaTicket?.let { reserva ->
        TicketDialog(
            reserva = reserva,
            onDismiss = { reservaParaTicket = null }
        )
    }

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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
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
                    ReservaCard(
                        reserva = reserva,
                        onCancelar = { reservaParaCancelar = reserva },
                        onPagar = { reservaParaPagar = reserva },
                        onOrganizar = { onOrganizarPartido(reserva.id) },
                        onVerTicket = { reservaParaTicket = reserva }
                    )
                }
            }
        }
    }
}

@Composable
private fun TicketDialog(
    reserva: Reserva,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.ConfirmationNumber, null, tint = MaterialTheme.colorScheme.primary)
                Text("Ticket de Reserva", style = MaterialTheme.typography.headlineSmall)
            }
        },
        text = {
            if (reserva.estado == EstadoReserva.CONFIRMADA) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(reserva.cancha.nombre, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Text(reserva.cancha.direccion, style = MaterialTheme.typography.bodySmall)

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("FECHA", style = MaterialTheme.typography.labelSmall)
                                    Text(reserva.fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), fontWeight = FontWeight.Bold)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("HORA", style = MaterialTheme.typography.labelSmall)
                                    Text(reserva.fecha.format(DateTimeFormatter.ofPattern("HH:mm'hs'")), fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Box(
                                modifier = Modifier
                                    .size(140.dp)
                                    .background(Color.White, RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCode2,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    tint = Color.Black
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text("ID: ${reserva.id.take(8).uppercase()}", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Text(
                        "Mostrá este código al llegar al predio.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        "Ticket no disponible",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Tu reserva aún está pendiente de pago. Debes completar el pago total para obtener tu ticket de acceso.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    )
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
private fun ReservaCard(
    reserva: Reserva,
    onCancelar: () -> Unit,
    onPagar: () -> Unit,
    onOrganizar: () -> Unit,
    onVerTicket: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

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
                Column {
                    Text(
                        text = reserva.cancha.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    EstadoBadge(estado = reserva.estado)
                }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Ver Ticket") },
                            onClick = { onVerTicket(); showMenu = false },
                            leadingIcon = { Icon(Icons.Default.ConfirmationNumber, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Cancelar Reserva", color = MaterialTheme.colorScheme.error) },
                            onClick = { onCancelar(); showMenu = false },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
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

            Spacer(modifier = Modifier.height(12.dp))

            // Sección de Pagos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Total: $${reserva.precioTotal.toInt()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (reserva.estado == EstadoReserva.CONFIRMADA) "Pagado" else "Pagado: $${reserva.montoPagado.toInt()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (reserva.estado == EstadoReserva.CONFIRMADA) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (reserva.estado == EstadoReserva.PENDIENTE) {
                        Button(onClick = onPagar, contentPadding = PaddingValues(horizontal = 12.dp)) {
                            Text("Pagar Saldo", fontSize = 12.sp)
                        }
                    }
                    OutlinedButton(onClick = onOrganizar, contentPadding = PaddingValues(horizontal = 12.dp)) {
                        Text("Organizar", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
