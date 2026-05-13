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
    onReservaSuccess: (String) -> Unit // Recibe reservaId
) {
    val cancha by viewModel.cancha.collectAsState()
    val fechaSeleccionada by viewModel.fechaSeleccionada.collectAsState()
    val horaSeleccionada by viewModel.horaSeleccionada.collectAsState()
    val duracionSeleccionada by viewModel.duracionSeleccionada.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val evento by viewModel.evento.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showSuccessDialog by remember { mutableStateOf(false) }

    LaunchedEffect(canchaId) {
        viewModel.cargarCancha(canchaId)
    }

    LaunchedEffect(evento) {
        when (evento) {
            is ReservaEvento.ReservaExitosa -> {
                showSuccessDialog = true
            }
            is ReservaEvento.Error -> {
                snackbarHostState.showSnackbar((evento as ReservaEvento.Error).mensaje)
                viewModel.limpiarEvento()
            }
            else -> {}
        }
    }

    if (showSuccessDialog) {
        val reserva = (evento as? ReservaEvento.ReservaExitosa)?.reserva
        AlertDialog(
            onDismissRequest = { /* No permitir cerrar sin elegir */ },
            title = { Text("¡Reserva Confirmada!") },
            text = { Text("Tu cancha ya está reservada. ¿Te faltan jugadores para completar el equipo? Podés crear un partido público ahora.") },
            confirmButton = {
                Button(onClick = {
                    showSuccessDialog = false
                    viewModel.limpiarEvento()
                    if (reserva != null) {
                        onReservaSuccess(reserva.id) // Aquí navegaremos a crear partido
                    }
                }) {
                    Text("Crear Partido Público")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSuccessDialog = false
                    viewModel.limpiarEvento()
                    onBack() // Solo volver atrás
                }) {
                    Text("Solo reservar")
                }
            }
        )
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
                    onFechaSelected = { viewModel.seleccionarFecha(it) }
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
                    horaSeleccionada = horaSeleccionada,
                    onHoraSelected = { viewModel.seleccionarHora(it) }
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
                    onDuracionSelected = { viewModel.seleccionarDuracion(it) }
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
                            text = "Total: $${viewModel.calcularPrecioTotal().toInt()}",
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
                        onClick = { viewModel.realizarReserva() },
                        enabled = horaSeleccionada != null && !isLoading,
                        modifier = Modifier.height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Reservar Ahora")
                        }
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
                    OutlinedButton(
                        onClick = { onHoraSelected(horario.hora) },
                        modifier = Modifier.weight(1f),
                        enabled = horario.disponible,
                        colors = if (isSelected) {
                            ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        } else ButtonDefaults.outlinedButtonColors(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(horario.hora.toString())
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
    onDuracionSelected: (Double) -> Unit
) {
    val opciones = listOf(1.0, 1.5, 2.0)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        opciones.forEach { opcion ->
            val isSelected = opcion == duracionSeleccionada
            InputChip(
                selected = isSelected,
                onClick = { onDuracionSelected(opcion) },
                label = { Text("${opcion}h") }
            )
        }
    }
}
