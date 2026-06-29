package com.example.futbol_tnt.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import com.example.futbol_tnt.data.model.EstadoPartido
import com.example.futbol_tnt.data.model.Partido
import com.example.futbol_tnt.presentation.ui.screens.home.components.EstadoPartidoBadge
import com.example.futbol_tnt.presentation.viewmodel.PartidoEvento
import com.example.futbol_tnt.presentation.viewmodel.PartidoViewModel
import com.google.firebase.auth.FirebaseAuth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetallePartidoScreen(
    partidoId: String,
    viewModel: PartidoViewModel,
    onBack: () -> Unit
) {
    var partido by remember { mutableStateOf<Partido?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val currentUid = remember { FirebaseAuth.getInstance().currentUser?.uid }
    val evento by viewModel.evento.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(partidoId) {
        partido = viewModel.getPartidoById(partidoId)
        isLoading = false
    }

    LaunchedEffect(evento) {
        when (evento) {
            is PartidoEvento.SolicitudEnviada -> {
                snackbarHostState.showSnackbar("Solicitud enviada correctamente")
                partido = viewModel.getPartidoById(partidoId) // Refresh
                viewModel.limpiarEvento()
            }
            is PartidoEvento.Error -> {
                snackbarHostState.showSnackbar((evento as PartidoEvento.Error).mensaje)
                viewModel.limpiarEvento()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Partido") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (partido == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No se encontró el partido.")
            }
        } else {
            val p = partido!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Cabecera: Equipos
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                ) {
                    Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly, Alignment.CenterVertically) {
                            TeamInfo(nombre = p.nombreLocal, label = "LOCAL", color = MaterialTheme.colorScheme.primary)
                            Text("VS", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                            TeamInfo(nombre = p.nombreVisitante, label = "VISITANTE", color = MaterialTheme.colorScheme.secondary)
                        }
                        Spacer(Modifier.height(16.dp))
                        EstadoPartidoBadge(estado = p.estado)
                    }
                }

                // Info de la Cancha
                SectionInfo(
                    icon = Icons.Default.Stadium,
                    title = p.cancha.nombre,
                    subtitle = "${p.cancha.direccion}, ${p.cancha.ciudad}"
                )

                // Fecha y Hora
                SectionInfo(
                    icon = Icons.Default.Event,
                    title = p.fecha.format(DateTimeFormatter.ofPattern("EEEE dd 'de' MMMM")),
                    subtitle = p.fecha.format(DateTimeFormatter.ofPattern("HH:mm'hs'"))
                )

                // Jugadores y Precio
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Card(Modifier.weight(1f)) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Jugadores", style = MaterialTheme.typography.labelSmall)
                            Text("${p.jugadoresActuales}/${p.jugadoresMaximos}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                    Card(Modifier.weight(1f)) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Precio/Persona", style = MaterialTheme.typography.labelSmall)
                            Text("$${p.precioPorPersona.toInt()}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Spacer(Modifier.weight(1f))

                // Accion
                val yaEsta = p.participantesIds.contains(currentUid) || p.solicitudesIds.contains(currentUid)
                val esCreador = p.creatorId == currentUid

                if (!esCreador) {
                    Button(
                        onClick = { viewModel.enviarSolicitud(p.id) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled = !yaEsta && p.estado == EstadoPartido.ABIERTO,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        val text = when {
                            p.participantesIds.contains(currentUid) -> "Ya eres participante"
                            p.solicitudesIds.contains(currentUid) -> "Solicitud enviada"
                            p.estado == EstadoPartido.LLENO -> "Partido lleno"
                            else -> "Quiero unirme"
                        }
                        Text(text)
                    }
                } else {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Eres el organizador")
                    }
                }
            }
        }
    }
}

@Composable
private fun TeamInfo(nombre: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(nombre, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color, textAlign = TextAlign.Center)
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun SectionInfo(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
        }
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
