package com.example.futbol_tnt.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.futbol_tnt.data.model.Notificacion
import com.example.futbol_tnt.data.model.TipoNotificacion
import com.example.futbol_tnt.presentation.viewmodel.NotificacionViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacionesScreen(
    viewModel: NotificacionViewModel,
    onBack: () -> Unit,
    onNavigateToMatch: (String) -> Unit
) {
    val notificaciones by viewModel.notificaciones.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificaciones") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (unreadCount > 0) {
                        TextButton(onClick = { /* Opcional: Implementar marcar todas como leídas */ }) {
                            Text("Limpiar", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (notificaciones.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.NotificationsNone, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No tenés notificaciones", color = MaterialTheme.colorScheme.outline)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(notificaciones) { notif ->
                    NotificacionItem(
                        notif = notif,
                        onClick = {
                            viewModel.marcarComoLeida(notif.id)
                            notif.partidoId?.let { onNavigateToMatch(it) }
                        }
                    )
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

@Composable
fun NotificacionItem(notif: Notificacion, onClick: () -> Unit) {
    val backgroundColor = if (notif.leido) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.05f)
    val sdf = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())

    val (icon, tint) = when (notif.tipo) {
        TipoNotificacion.SOLICITUD_RECIBIDA -> Icons.Default.GroupAdd to MaterialTheme.colorScheme.primary
        TipoNotificacion.SOLICITUD_APROBADA -> Icons.Default.CheckCircle to Color(0xFF22C55E)
        TipoNotificacion.SOLICITUD_RECHAZADA -> Icons.Default.Cancel to MaterialTheme.colorScheme.error
        TipoNotificacion.CANCELACION -> Icons.Default.EventBusy to MaterialTheme.colorScheme.error
        TipoNotificacion.PARTIDO_CERCANO -> Icons.Default.LocationOn to MaterialTheme.colorScheme.tertiary
        TipoNotificacion.SOLICITUD_URGENTE -> Icons.Default.FlashOn to MaterialTheme.colorScheme.error
        TipoNotificacion.INFO -> Icons.Default.Info to MaterialTheme.colorScheme.secondary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(backgroundColor)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(tint.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = notif.titulo,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (notif.leido) FontWeight.Normal else FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = sdf.format(notif.fecha.toDate()),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Text(
                text = notif.mensaje,
                style = MaterialTheme.typography.bodyMedium,
                color = if (notif.leido) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (!notif.leido) {
            Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
        }
    }
}
