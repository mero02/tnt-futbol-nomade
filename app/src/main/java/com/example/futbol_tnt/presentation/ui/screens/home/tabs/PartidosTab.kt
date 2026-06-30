package com.example.futbol_tnt.presentation.ui.screens.home.tabs

import android.content.Context
import android.content.Intent
import android.Manifest
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.futbol_tnt.core.util.LocationHelper
import com.example.futbol_tnt.core.util.QRHelper
import com.example.futbol_tnt.data.model.*
import com.example.futbol_tnt.data.repository.UserRepository
import com.example.futbol_tnt.presentation.ui.screens.home.components.EstadoPartidoBadge
import com.example.futbol_tnt.presentation.viewmodel.PartidoEvento
import com.example.futbol_tnt.presentation.viewmodel.PartidoViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.firebase.auth.FirebaseAuth
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val partidoFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

private fun compartirPartido(context: Context, partido: Partido) {
    val deepLink = "https://futboltnt.app/partido/${partido.id}"
    val texto = """
        ⚽ *¡Hay partido en Entra a la cancha!*

        🏟️ *Lugar:* ${partido.cancha.nombre} (${partido.cancha.ciudad})
        📅 *Fecha:* ${partido.fecha.format(DateTimeFormatter.ofPattern("EEEE dd/MM"))}
        ⏰ *Hora:* ${partido.fecha.format(DateTimeFormatter.ofPattern("HH:mm"))}hs
        💰 *Precio:* $${partido.precioPorPersona.toInt()} por persona
        👥 *Faltan:* ${partido.jugadoresMaximos - partido.jugadoresActuales} jugadores

        👇 *Unite al equipo entrando acá:*
        $deepLink
    """.trimIndent()

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, texto)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Compartir partido via")
    context.startActivity(shareIntent)
}

@Composable
private fun QRInvitationDialog(url: String, onDismiss: () -> Unit) {
    val qrBitmap = remember(url) { QRHelper.generateQRCode(url) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.padding(16.dp).fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Invitación QR",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Tus amigos pueden escanear este código para unirse al partido.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "QR Code",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        CircularProgressIndicator()
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cerrar")
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun PartidosTab(
    viewModel: PartidoViewModel,
    onCrearPartido: () -> Unit,
    onVerDetalle: (String) -> Unit,
    onNavigateToProfile: (String) -> Unit = {},
    onNavigateToCalificar: (String) -> Unit = {}
) {
    var internalSelectedTab by rememberSaveable { mutableIntStateOf(0) } // 0: Todos, 1: Mis Partidos
    var qrUrlToShow by remember { mutableStateOf<String?>(null) }

    var filtros by rememberSaveable(
        stateSaver = Saver<FiltroPartidos, Any>(
            save = { listOf(it.fecha.name, it.tipoCancha?.name, it.estado.name, it.ciudad) },
            restore = { values ->
                val list = values as List<String?>
                FiltroPartidos(
                    fecha = FiltroFecha.valueOf(list[0] ?: "TODOS"),
                    tipoCancha = list[1]?.let { TipoCancha.valueOf(it) },
                    estado = FiltroEstado.valueOf(list[2] ?: "TODOS"),
                    ciudad = list[3] ?: ""
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
    val userLocation by viewModel.userLocation.collectAsState()

    val context = LocalContext.current
    val locationHelper = remember { LocationHelper(context) }
    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    LaunchedEffect(locationPermissionState.status.isGranted) {
        if (locationPermissionState.status.isGranted) {
            val location = locationHelper.getCurrentLocation()
            viewModel.setUserLocation(location)
        }
    }

    val partidosFiltrados = remember(filtros, todosLosPartidos, internalSelectedTab, currentUid) {
        val ahora = LocalDateTime.now()
        todosLosPartidos.map { partido ->
            val horaFin = partido.fecha.plusHours(partido.duracionHoras.toLong())
            val nuevoEstado = when {
                ahora.isAfter(horaFin) -> EstadoPartido.FINALIZADO
                ahora.isAfter(partido.fecha) && ahora.isBefore(horaFin) -> EstadoPartido.EN_JUEGO
                else -> partido.estado
            }
            partido.copy(estado = nuevoEstado)
        }.filter { partido ->
            val perteneceAMisPartidos = if (internalSelectedTab == 1) {
                partido.creatorId == currentUid || partido.participantesIds.contains(currentUid)
            } else true

            val filtroEstadoOk = when (filtros.estado) {
                FiltroEstado.ABIERTOS -> partido.estado == EstadoPartido.ABIERTO
                FiltroEstado.LLENOS -> partido.estado == EstadoPartido.LLENO
                FiltroEstado.TODOS -> if (internalSelectedTab == 1) true else partido.estado != EstadoPartido.FINALIZADO
            }

            val filtroFechaOk = when (filtros.fecha) {
                FiltroFecha.HOY -> partido.fecha.toLocalDate() == LocalDate.now()
                FiltroFecha.ESTA_SEMANA -> {
                    val hoy = LocalDate.now()
                    partido.fecha.toLocalDate() in hoy..hoy.plusDays(7)
                }
                FiltroFecha.TODOS -> true
            }
            val filtroTipoOk = filtros.tipoCancha == null || partido.cancha.tipo == filtros.tipoCancha
            val filtroCiudadOk = filtros.ciudad.isBlank() ||
                                partido.cancha.ciudad.contains(filtros.ciudad, ignoreCase = true)

            perteneceAMisPartidos && filtroFechaOk && filtroTipoOk && filtroEstadoOk && filtroCiudadOk
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

    if (qrUrlToShow != null) {
        QRInvitationDialog(url = qrUrlToShow!!, onDismiss = { qrUrlToShow = null })
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = internalSelectedTab,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            divider = {}
        ) {
            Tab(
                selected = internalSelectedTab == 0,
                onClick = { internalSelectedTab = 0 },
                text = { Text("Todos") }
            )
            Tab(
                selected = internalSelectedTab == 1,
                onClick = { internalSelectedTab = 1 },
                text = { Text("Mis Partidos") }
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (internalSelectedTab == 0) {
                item {
                    OutlinedTextField(
                        value = filtros.ciudad,
                        onValueChange = { filtros = filtros.copy(ciudad = it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        placeholder = { Text("Buscar partidos por ciudad...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (userLocation == null) {
                                IconButton(onClick = { locationPermissionState.launchPermissionRequest() }) {
                                    Icon(
                                        imageVector = Icons.Default.MyLocation,
                                        contentDescription = "Mi ubicación",
                                        tint = if (locationPermissionState.status.isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        )
                    )

                    FiltrosPartidos(
                        filtros = filtros,
                        onFiltrosChange = { filtros = it }
                    )
                }
            }

            if (partidosFiltrados.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (internalSelectedTab == 1) "No tenés partidos todavía." else "No se encontraron partidos.",
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                items(partidosFiltrados, key = { it.id }) { partido ->
                    PartidoCard(
                        partido = partido,
                        currentUid = currentUid,
                        userLocation = userLocation,
                        onUnirse = { showUnirseDialog = partido },
                        onGestionar = { showGestionarDialog = partido },
                        onAbandonar = { showAbandonarDialog = partido },
                        onVerParticipantes = { showParticipantesDialog = partido },
                        onCalificar = { onNavigateToCalificar(partido.id) },
                        onShowQR = { qrUrlToShow = "https://futboltnt.app/partido/${partido.id}" },
                        onVerDetalle = { onVerDetalle(partido.id) }
                    )
                }
            }
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
    userLocation: android.location.Location?,
    onUnirse: () -> Unit,
    onGestionar: () -> Unit,
    onAbandonar: () -> Unit,
    onVerParticipantes: () -> Unit,
    onCalificar: () -> Unit,
    onShowQR: () -> Unit,
    onVerDetalle: () -> Unit
) {
    val context = LocalContext.current
    val distanceText = remember(userLocation, partido.cancha) {
        if (userLocation != null) {
            val results = FloatArray(1)
            android.location.Location.distanceBetween(
                userLocation.latitude, userLocation.longitude,
                partido.cancha.lat, partido.cancha.lng,
                results
            )
            val distanceKm = results[0] / 1000
            "a ${String.format("%.1f", distanceKm)} km"
        } else null
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable { onVerDetalle() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = partido.cancha.nombre,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${partido.cancha.direccion}, ${partido.cancha.ciudad}${if (distanceText != null) " ($distanceText)" else ""}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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
                    partido.estado == EstadoPartido.FINALIZADO -> {
                        Button(
                            onClick = onCalificar,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Default.Star, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Calificar Jugadores")
                        }
                    }
                    partido.estado == EstadoPartido.EN_JUEGO -> {
                        OutlinedButton(onClick = {}, enabled = false) {
                            Text("En juego")
                        }
                    }
                    partido.creatorId == currentUid && !currentUid.isNullOrBlank() -> {
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
                        val ahora = LocalDateTime.now()
                        val horasRestantes = Duration.between(ahora, partido.fecha).toHours()
                        val puedeAbandonar = horasRestantes >= 2

                        Column(horizontalAlignment = Alignment.End) {
                            Button(
                                onClick = onAbandonar,
                                enabled = puedeAbandonar,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Abandonar")
                            }
                            if (!puedeAbandonar) {
                                Text(
                                    "Límite excedido (2h antes)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
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
