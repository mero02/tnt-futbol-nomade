package com.example.futbol_tnt.presentation.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.futbol_tnt.core.util.QRHelper
import com.example.futbol_tnt.data.model.EstadoPartido
import com.example.futbol_tnt.data.model.Partido
import com.example.futbol_tnt.data.model.User
import com.example.futbol_tnt.data.repository.UserRepository
import com.example.futbol_tnt.presentation.ui.screens.home.components.EstadoPartidoBadge
import com.example.futbol_tnt.presentation.viewmodel.PartidoEvento
import com.example.futbol_tnt.presentation.viewmodel.PartidoViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetallePartidoScreen(
    partidoId: String,
    viewModel: PartidoViewModel,
    onBack: () -> Unit,
    onViewProfile: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var partido by remember { mutableStateOf<Partido?>(null) }
    var participantes by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showQR by remember { mutableStateOf(false) }

    val currentUid = remember { FirebaseAuth.getInstance().currentUser?.uid }
    val userRepository = remember { UserRepository() }
    val evento by viewModel.evento.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Función para cargar o refrescar datos
    val refreshData = {
        scope.launch {
            isLoading = true
            val p = viewModel.getPartidoById(partidoId)
            partido = p
            if (p != null) {
                participantes = p.participantesIds.mapNotNull { uid ->
                    userRepository.getUser(uid)
                }
            }
            isLoading = false
        }
    }

    LaunchedEffect(partidoId) {
        refreshData()
    }

    LaunchedEffect(evento) {
        when (evento) {
            is PartidoEvento.SolicitudEnviada -> {
                snackbarHostState.showSnackbar("¡Solicitud enviada! Espera la aprobación.")
                refreshData()
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
                title = { Text("Información del Partido", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (partido?.creatorId == currentUid && !currentUid.isNullOrBlank()) {
                        IconButton(onClick = { showQR = true }) {
                            Icon(Icons.Default.QrCode2, "QR")
                        }
                        IconButton(onClick = { partido?.let { compartirPartido(context, it) } }) {
                            Icon(Icons.Default.Share, "Compartir")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (isLoading && partido == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (partido == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No se pudo cargar el partido.")
            }
        } else {
            val p = partido!!
            val esParticipante = p.participantesIds.contains(currentUid)
            val esSolicitante = p.solicitudesIds.contains(currentUid)
            val esOrganizador = p.creatorId == currentUid && !currentUid.isNullOrBlank()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Tarjeta de Equipos
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TeamInfo(nombre = p.nombreLocal, label = "LOCAL", color = MaterialTheme.colorScheme.primary)
                            Text("VS", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                            TeamInfo(nombre = p.nombreVisitante, label = "VISITANTE", color = MaterialTheme.colorScheme.secondary)
                        }
                        Spacer(Modifier.height(16.dp))
                        EstadoPartidoBadge(estado = p.estado)
                    }
                }

                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    // Detalles de Lugar y Precio
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        border = CardDefaults.outlinedCardBorder(),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            DetailRow(icon = Icons.Default.Stadium, title = p.cancha.nombre, subtitle = "${p.cancha.direccion}, ${p.cancha.ciudad}")
                            DetailRow(icon = Icons.Default.Event, title = p.fecha.format(DateTimeFormatter.ofPattern("EEEE dd 'de' MMMM")), subtitle = p.fecha.format(DateTimeFormatter.ofPattern("HH:mm 'hs'")))
                            DetailRow(icon = Icons.Default.MonetizationOn, title = "$${p.precioPorPersona.toInt()} por persona", subtitle = "Se abona en el complejo")
                        }
                    }

                    // Lista de Jugadores
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Jugadores confirmados (${p.jugadoresActuales}/${p.jugadoresMaximos})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                        participantes.forEach { user ->
                            PlayerRow(user = user, onClick = { onViewProfile(user.uid) })
                        }

                        // Slots libres
                        repeat(p.jugadoresMaximos - p.jugadoresActuales) {
                            EmptyRow()
                        }
                    }

                    // Botón de Acción al final del contenido scrollable
                    if (!esOrganizador) {
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.enviarSolicitud(p.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = !esParticipante && !esSolicitante && p.estado == EstadoPartido.ABIERTO,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            val texto = when {
                                esParticipante -> "Ya estás en el equipo"
                                esSolicitante -> "Solicitud en revisión..."
                                p.estado == EstadoPartido.LLENO -> "Partido completo"
                                else -> "Quiero jugar"
                            }
                            Text(texto, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }

    if (showQR && partido != null) {
        QRInvitationDialog(url = "https://futboltnt.app/partido/${partido!!.id}", onDismiss = { showQR = false })
    }
}

@Composable
private fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
        Column {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PlayerRow(user: User, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AsyncImage(
            model = user.photoUrl,
            contentDescription = null,
            modifier = Modifier.size(44.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop
        )
        Column(Modifier.weight(1f)) {
            Text(user.displayName ?: "Jugador", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text(user.posicion?.name ?: "Sin posición", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        }
        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline)
    }
}

@Composable
private fun EmptyRow() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(Modifier.size(44.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        }
        Text("Lugar disponible", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
private fun TeamInfo(nombre: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(nombre, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = color, textAlign = TextAlign.Center)
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
    }
}

@Composable
private fun QRInvitationDialog(url: String, onDismiss: () -> Unit) {
    val qrBitmap = remember(url) { QRHelper.generateQRCode(url) }
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(28.dp)) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Invitación QR", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                Box(Modifier.size(240.dp).background(Color.White, RoundedCornerShape(12.dp)).padding(16.dp)) {
                    qrBitmap?.let { Image(bitmap = it.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxSize()) }
                }
                Spacer(Modifier.height(24.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Cerrar") }
            }
        }
    }
}

private fun compartirPartido(context: Context, partido: Partido) {
    val deepLink = "https://futboltnt.app/partido/${partido.id}"
    val texto = """
        ⚽ *¡Hay partido en Futbol TNT!*
        🏟️ *Lugar:* ${partido.cancha.nombre}
        📅 *Fecha:* ${partido.fecha.format(DateTimeFormatter.ofPattern("EEEE dd/MM HH:mm"))}hs
        👥 *Faltan:* ${partido.jugadoresMaximos - partido.jugadoresActuales} jugadores
        👇 *Unite acá:* $deepLink
    """.trimIndent()
    val sendIntent = Intent().apply { action = Intent.ACTION_SEND; putExtra(Intent.EXTRA_TEXT, texto); type = "text/plain" }
    context.startActivity(Intent.createChooser(sendIntent, "Compartir vía"))
}
