package com.example.futbol_tnt.presentation.ui.screens.home.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.futbol_tnt.data.model.Cancha
import com.example.futbol_tnt.presentation.viewmodel.BusquedaCanchasViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
internal fun CanchasTab(
    onNavigateToCanchaDetail: (String) -> Unit,
    viewModel: BusquedaCanchasViewModel = viewModel()
) {
    val query by viewModel.query.collectAsState()
    val canchas by viewModel.canchasFiltradas.collectAsState()
    val error by viewModel.error.collectAsState()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) } // 0: Mapa, 1: Lista

    Column(modifier = Modifier.fillMaxSize()) {
        // Barra de Búsqueda
        OutlinedTextField(
            value = query,
            onValueChange = { viewModel.onQueryChange(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Buscar ciudad (Trelew, Madryn...)") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (error != null) {
                    IconButton(onClick = { viewModel.refreshData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reintentar")
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

        // Tabs: Mapa / Lista
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            divider = {}
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Mapa") },
                icon = { Icon(Icons.Default.Map, null) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Lista") },
                icon = { Icon(Icons.AutoMirrored.Filled.List, null) }
            )
        }

        if (error != null) {
            ErrorView(mensaje = error!!, onRetry = { viewModel.refreshData() })
        } else if (canchas.isEmpty() && query.isNotEmpty()) {
            NoResultsView()
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                if (selectedTab == 0) {
                    CanchasMapView(
                        canchas = canchas,
                        onCanchaClick = onNavigateToCanchaDetail
                    )
                } else {
                    CanchasListView(
                        canchas = canchas,
                        onCanchaClick = onNavigateToCanchaDetail
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorView(mensaje: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(mensaje, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Reintentar") }
    }
}

@Composable
private fun CanchasMapView(
    canchas: List<Cancha>,
    onCanchaClick: (String) -> Unit
) {
    val trelew = LatLng(-43.2489, -65.3051)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(trelew, 11f)
    }

    LaunchedEffect(canchas) {
        if (canchas.isNotEmpty()) {
            val focus = canchas.first()
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(LatLng(focus.lat, focus.lng), 13f)
            )
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(zoomControlsEnabled = true)
    ) {
        canchas.forEach { cancha ->
            Marker(
                state = MarkerState(position = LatLng(cancha.lat, cancha.lng)),
                title = cancha.nombre,
                snippet = "Toca para reservar",
                onInfoWindowClick = { onCanchaClick(cancha.id) }
            )
        }
    }
}

@Composable
private fun CanchasListView(
    canchas: List<Cancha>,
    onCanchaClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(canchas, key = { it.id }) { cancha ->
            CanchaCard(cancha = cancha, onClick = { onCanchaClick(cancha.id) })
        }
    }
}

@Composable
private fun NoResultsView() {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
        Spacer(modifier = Modifier.height(16.dp))
        Text("No hay canchas disponibles en esta ciudad", textAlign = TextAlign.Center)
    }
}

@Composable
private fun CanchaCard(cancha: Cancha, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Box(Modifier.fillMaxWidth().height(120.dp).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.SportsSoccer, null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(0.5f))
            }
            Column(Modifier.padding(12.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text(cancha.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                        Text(cancha.tipo.name.replace("_", " "), style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
                Text("${cancha.direccion}, ${cancha.ciudad}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text("$${cancha.precioPorHora.toInt()}/hr", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Button(onClick = onClick, modifier = Modifier.height(36.dp), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)) {
                        Text("Reservar")
                    }
                }
            }
        }
    }
}
