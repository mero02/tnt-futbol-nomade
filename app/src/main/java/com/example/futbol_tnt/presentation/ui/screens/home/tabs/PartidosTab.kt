package com.example.futbol_tnt.presentation.ui.screens.home.tabs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.futbol_tnt.data.model.*
import com.example.futbol_tnt.data.repository.UserRepository
import com.example.futbol_tnt.presentation.ui.screens.home.components.EstadoPartidoBadge
import com.example.futbol_tnt.presentation.ui.screens.home.components.HeaderSection
import com.example.futbol_tnt.presentation.viewmodel.PartidoEvento
import com.example.futbol_tnt.presentation.viewmodel.PartidoViewModel
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val partidoFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

@Composable
internal fun PartidosTab(
    viewModel: PartidoViewModel,
    onCrearPartido: () -> Unit,
    onNavigateToProfile: (String) -> Unit = {}
) {
    var filtros by rememberSaveable(
        stateSaver = Saver<FiltroPartidos, Any>(
            save = { listOf(it.fecha.name, it.tipoCancha?.name, it.estado.name) },
            restore = { values ->
                val list = values as List<String?>
                FiltroPartidos(
                    fecha = FiltroFecha.valueOf(list[0] ?: "TODOS"),
                    tipoCancha = list[1]?.let { TipoCancha.valueOf(it) },
                    estado = FiltroEstado.valueOf(list[2] ?: "TODOS")
                )
            }
        )
    ) { mutableStateOf(FiltroPartidos()) }

    var showUnirseDialog by remember { mutableStateOf<Partido?>(null) }
    var showGestionarDialog by remember { mutableStateOf<Partido?>(null) }
    var showAbandonarDialog by remember { mutableStateOf<Partido?>(null) }
    var showParticipantesDialog by remember { mutableStateOf<Partido?>(null) }

    val currentUid = remember { FirebaseAuth.getInstance().currentUser?.uid }
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
            is PartidoEvento.SolicitudEnviada,
            is PartidoEvento.SolicitudGestionada,
            is PartidoEvento.AbandonarExito,
            is PartidoEvento.PartidoLleno,
            is PartidoEvento.Error -> {
                showUnirseDialog = null
                showGestionarDialog = null
                showAbandonarDialog = null
                viewModel.limpiarEvento()
            }
            else -> {}
        }
    }

    showUnirseDialog?.let { partido ->
        UnirsePartidoDialog(
            partido = partido,
            onDismiss = { showUnirseDialog = null },
            onConfirm = { viewModel.enviarSolicitud(partido.id) }
        )
    }

    showGestionarDialog?.let { partido ->
        GestionarSolicitudesDialog(
            partido = partido,
            onDismiss = { showGestionarDialog = null },
            onAceptar = { applicantId -> viewModel.gestionarSolicitud(partido.id, applicantId, true) },
            onRechazar = { applicantId -> viewModel.gestionarSolicitud(partido.id, applicantId, false) },
            onViewProfile = { uid ->
                showGestionarDialog = null
                onNavigateToProfile(uid)
            }
        )
    }

    showAbandonarDialog?.let { partido ->
        ConfirmarAbandonarDialog(
            onDismiss = { showAbandonarDialog = null },
            onConfirm = { viewModel.abandonarPartido(partido.id) }
        )
    }

    showParticipantesDialog?.let { partido ->
        VerParticipantesDialog(
            partido = partido,
            onDismiss = { showParticipantesDialog = null },
            onViewProfile = { uid ->
                showParticipantesDialog = null
                onNavigateToProfile(uid)
            }
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
                subtitulo = "${partidosFiltrados.size} partidos disponibles"
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
                currentUid = currentUid,
                onUnirse = { showUnirseDialog = partido },
                onGestionar = { showGestionarDialog = partido },
                onAbandonar = { showAbandonarDialog = partido },
                onVerParticipantes = { showParticipantesDialog = partido }
            )
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
    currentUid: String?,
    onUnirse: () -> Unit,
    onGestionar: () -> Unit,
    onAbandonar: () -> Unit,
    onVerParticipantes: () -> Unit
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
                    Text(text = "LOCAL", style = MaterialTheme.typography.labelSmall)
                }
                Text(text = "VS", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = partido.nombreVisitante,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(text = "VISITANTE", style = MaterialTheme.typography.labelSmall)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(partido.fecha.format(partidoFormatter), style = MaterialTheme.typography.bodySmall)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onVerParticipantes() }
                ) {
                    Text(
                        text = "${partido.jugadoresActuales}/${partido.jugadoresMaximos} jugadores",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(Icons.Default.Groups, null, modifier = Modifier.size(16.dp).padding(start = 4.dp), tint = MaterialTheme.colorScheme.primary)
                }
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
                    fontWeight = FontWeight.Bold
                )

                when {
                    partido.creatorId == currentUid -> {
                        val reqCount = partido.solicitudesIds.size
                        if (reqCount > 0) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = Color.White
                                    ) {
                                        Text(reqCount.toString())
                                    }
                                }
                            ) {
                                Button(onClick = onGestionar) {
                                    Icon(Icons.Default.GroupAdd, null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Solicitudes")
                                }
                            }
                        } else {
                            OutlinedButton(onClick = {}, enabled = false) {
                                Text("Organizador")
                            }
                        }
                    }
                    partido.participantesIds.contains(currentUid) -> {
                        Button(
                            onClick = onAbandonar,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)
                        ) {
                            Icon(Icons.Default.ExitToApp, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Abandonar")
                        }
                    }
                    partido.solicitudesIds.contains(currentUid) -> {
                        OutlinedButton(onClick = {}, enabled = false) {
                            Icon(Icons.Default.HourglassEmpty, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("En espera")
                        }
                    }
                    partido.estado == EstadoPartido.LLENO -> {
                        OutlinedButton(onClick = {}, enabled = false) { Text("Lleno") }
                    }
                    else -> {
                        FilledTonalButton(onClick = onUnirse) { Text("Unirse") }
                    }
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
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enviar Solicitud") },
        text = {
            Text("¿Querés enviarle una solicitud al organizador para unirte a este partido?")
        },
        confirmButton = {
            Button(onClick = onConfirm) { Text("Enviar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun ConfirmarAbandonarDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("¿Abandonar partido?") },
        text = { Text("¿Estás seguro de que querés salir de este partido? Tu lugar quedará disponible para otro jugador.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun VerParticipantesDialog(
    partido: Partido,
    onDismiss: () -> Unit,
    onViewProfile: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Participantes") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                partido.participantesIds.forEach { uid ->
                    ParticipanteItem(
                        uid = uid,
                        isOrganizer = uid == partido.creatorId,
                        onViewProfile = { onViewProfile(uid) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}

@Composable
private fun ParticipanteItem(
    uid: String,
    isOrganizer: Boolean,
    onViewProfile: () -> Unit
) {
    val userRepository = remember { UserRepository() }
    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(uid) {
        user = userRepository.getUser(uid)
        isLoading = false
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onViewProfile() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
        } else {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(user?.photoUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user?.apodo ?: user?.displayName ?: "Jugador",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                if (isOrganizer) {
                    Text(
                        text = "Organizador",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun GestionarSolicitudesDialog(
    partido: Partido,
    onDismiss: () -> Unit,
    onAceptar: (String) -> Unit,
    onRechazar: (String) -> Unit,
    onViewProfile: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Solicitudes Pendientes") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (partido.solicitudesIds.isEmpty()) {
                    Text("No hay solicitudes pendientes.")
                }
                partido.solicitudesIds.forEach { applicantId ->
                    SolicitanteItem(
                        uid = applicantId,
                        onAceptar = { onAceptar(applicantId) },
                        onRechazar = { onRechazar(applicantId) },
                        onViewProfile = { onViewProfile(applicantId) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}

@Composable
private fun SolicitanteItem(
    uid: String,
    onAceptar: () -> Unit,
    onRechazar: () -> Unit,
    onViewProfile: () -> Unit
) {
    val userRepository = remember { UserRepository() }
    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(uid) {
        user = userRepository.getUser(uid)
        isLoading = false
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onViewProfile() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
        } else {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(user?.photoUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user?.apodo ?: user?.displayName ?: "Jugador",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = user?.posicion?.displayName ?: "Posición no definida",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row {
                IconButton(onClick = onRechazar) {
                    Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.error)
                }
                IconButton(onClick = onAceptar) {
                    Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
