package com.example.futbol_tnt.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.futbol_tnt.data.model.Cancha
import com.example.futbol_tnt.data.model.Horario
import com.example.futbol_tnt.data.model.Reserva
import com.example.futbol_tnt.presentation.viewmodel.CanchaViewModel
import com.example.futbol_tnt.presentation.viewmodel.ReservaEvento
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CanchaDetailScreen(
    canchaId: String,
    viewModel: CanchaViewModel,
    onBack: () -> Unit,
    onReservaSuccess: (String) -> Unit, // Recibe reservaId
    onNavigateToCheckout: (String, java.time.LocalDate, java.time.LocalTime, Double) -> Unit
) {
    val cancha by viewModel.cancha.collectAsState()
    val reservasDelDia by viewModel.reservasDelDia.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val evento by viewModel.evento.collectAsState()

    // Estado local que sobrevive cambios de configuración (rotación)
    var fechaSeleccionadaStr by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    var horaSeleccionadaStr by rememberSaveable { mutableStateOf<String?>(null) }
    var duracionSeleccionada by rememberSaveable { mutableStateOf(1.0) }

    val fechaSeleccionada = LocalDate.parse(fechaSeleccionadaStr)
    val horaSeleccionada = horaSeleccionadaStr?.let { LocalTime.parse(it) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(canchaId) {
        viewModel.cargarCancha(canchaId)
    }

    LaunchedEffect(canchaId, fechaSeleccionada) {
        viewModel.cargarDisponibilidad(fechaSeleccionada)
    }

    LaunchedEffect(evento) {
        if (evento is ReservaEvento.Error) {
            snackbarHostState.showSnackbar((evento as ReservaEvento.Error).mensaje)
            viewModel.limpiarEvento()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Cancha") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        cancha?.let { c ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Imagen de la cancha (Placeholder)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsSoccer,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = c.nombre,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${c.tipo.name.replace("_", " ")} - $${c.precioPorHora.toInt()}/hr",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Seleccionar Fecha",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                FechaSelector(
                    fechaSeleccionada = fechaSeleccionada,
                    onFechaSelected = { nuevaFecha ->
                        fechaSeleccionadaStr = nuevaFecha.toString()
                        horaSeleccionadaStr = null
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Horarios Disponibles",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                HorariosGrid(
                    horarios = c.disponibilidad,
                    reservasOcupadas = reservasDelDia,
                    fechaSeleccionada = fechaSeleccionada,
                    horaSeleccionada = horaSeleccionada,
                    onHoraSelected = { horaSeleccionadaStr = it.toString() }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Duración",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                DuracionSelector(
                    duracionSeleccionada = duracionSeleccionada,
                    horaSeleccionada = horaSeleccionada,
                    reservasOcupadas = reservasDelDia,
                    onDuracionSelected = { duracionSeleccionada = it }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Resumen y Botón
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total: $${((c.precioPorHora) * duracionSeleccionada).toInt()}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "(${duracionSeleccionada}h)",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Button(
                        onClick = {
                            horaSeleccionada?.let { h ->
                                onNavigateToCheckout(canchaId, fechaSeleccionada, h, duracionSeleccionada)
                            }
                        },
                        enabled = horaSeleccionada != null && !isLoading,
                        modifier = Modifier.height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Continuar")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun FechaSelector(
    fechaSeleccionada: LocalDate,
    onFechaSelected: (LocalDate) -> Unit
) {
    val fechas = remember { (0..6).map { LocalDate.now().plusDays(it.toLong()) } }

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(fechas) { fecha ->
            val isSelected = fecha == fechaSeleccionada
            FilterChip(
                selected = isSelected,
                onClick = { onFechaSelected(fecha) },
                label = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(fecha.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("es")).uppercase())
                        Text(fecha.dayOfMonth.toString(), fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

@Composable
fun HorariosGrid(
    horarios: List<Horario>,
    reservasOcupadas: List<Reserva>,
    fechaSeleccionada: LocalDate,
    horaSeleccionada: LocalTime?,
    onHoraSelected: (LocalTime) -> Unit
) {
    // Usamos un Box con altura fija para el grid dentro de un Column scrolleable
    // o mejor aun, una Grid simple sin scroll interno
    Column {
        val chunkedHorarios = horarios.chunked(3)
        chunkedHorarios.forEach { fila ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                fila.forEach { horario ->
                    val isSelected = horario.hora == horaSeleccionada
                    val estaOcupado = reservasOcupadas.any { res ->
                        val inicio = res.fecha.toLocalTime()
                        val fin = inicio.plusMinutes((res.duracionHoras * 60).toLong())
                        // El slot está ocupado si la hora del botón está dentro del rango [inicio, fin)
                        !horario.hora.isBefore(inicio) && horario.hora.isBefore(fin)
                    }

                    // Bloquear si el horario ya pasó (solo para hoy)
                    val esPasado = LocalDate.now() == fechaSeleccionada && LocalTime.now().isAfter(horario.hora)

                    OutlinedButton(
                        onClick = { onHoraSelected(horario.hora) },
                        modifier = Modifier.weight(1f),
                        enabled = horario.disponible && !estaOcupado && !esPasado,
                        colors = if (isSelected) {
                            ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        } else ButtonDefaults.outlinedButtonColors(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = horario.hora.toString(),
                            color = if (estaOcupado) MaterialTheme.colorScheme.error.copy(alpha = 0.6f) else Color.Unspecified
                        )
                    }
                }
                // Rellenar si la fila no está completa
                repeat(3 - fila.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun DuracionSelector(
    duracionSeleccionada: Double,
    horaSeleccionada: LocalTime?,
    reservasOcupadas: List<Reserva>,
    onDuracionSelected: (Double) -> Unit
) {
    val opciones = listOf(1.0, 1.5, 2.0)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        opciones.forEach { opcion ->
            val isSelected = opcion == duracionSeleccionada

            // Lógica para deshabilitar duraciones que pisen otras reservas
            val habilitado = if (horaSeleccionada != null) {
                val finPropuesto = horaSeleccionada.plusMinutes((opcion * 60).toLong())
                reservasOcupadas.none { res ->
                    val exInicio = res.fecha.toLocalTime()
                    val exFin = exInicio.plusMinutes((res.duracionHoras * 60).toLong())
                    // El nuevo turno termina después de que empieza uno existente?
                    finPropuesto.isAfter(exInicio) && horaSeleccionada.isBefore(exFin)
                }
            } else true

            InputChip(
                selected = isSelected,
                onClick = { onDuracionSelected(opcion) },
                enabled = habilitado,
                label = { Text("${opcion}h") }
            )
        }
    }
}
