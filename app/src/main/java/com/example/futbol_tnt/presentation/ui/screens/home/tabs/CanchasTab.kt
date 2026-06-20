package com.example.futbol_tnt.presentation.ui.screens.home.tabs

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.futbol_tnt.core.util.LocationHelper
import com.example.futbol_tnt.data.model.Cancha
import com.example.futbol_tnt.presentation.viewmodel.BusquedaCanchasViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun CanchasTab(
    onNavigateToCanchaDetail: (String) -> Unit,
    viewModel: BusquedaCanchasViewModel = viewModel()
) {
    val query by viewModel.query.collectAsState()
    val canchas by viewModel.canchasFiltradas.collectAsState()
    val error by viewModel.error.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val locationHelper = remember { LocationHelper(context) }

    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    // Al iniciar, si tenemos permiso, obtenemos la ubicación
    LaunchedEffect(locationPermissionState.status.isGranted) {
        if (locationPermissionState.status.isGranted) {
            val location = locationHelper.getCurrentLocation()
            viewModel.setUserLocation(location)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = { viewModel.onQueryChange(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Buscar ciudad (Trelew, Madryn...)") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                Row {
                    if (userLocation == null) {
                        IconButton(onClick = { locationPermissionState.launchPermissionRequest() }) {
                            Icon(Icons.Default.MyLocation, contentDescription = "Mi ubicación", tint = if (locationPermissionState.status.isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                        }
                    }
                    if (error != null) {
                        IconButton(onClick = { viewModel.refreshData() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reintentar")
                        }
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
                        userLocation = userLocation,
                        hasLocationPermission = locationPermissionState.status.isGranted,
                        onCanchaClick = onNavigateToCanchaDetail
                    )
                } else {
                    CanchasListView(
                        canchas = canchas,
                        userLocation = userLocation,
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
    userLocation: android.location.Location?,
    hasLocationPermission: Boolean,
    onCanchaClick: (String) -> Unit
) {
    // Si hay ubicación de usuario, centrar ahí, sino en Trelew
    val initialPos = remember(userLocation) {
        if (userLocation != null) LatLng(userLocation.latitude, userLocation.longitude)
        else LatLng(-43.2489, -65.3051)
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPos, 12f)
    }

    // Efecto para mover la cámara cuando se filtran canchas o cambia la ubicación
    LaunchedEffect(canchas, userLocation) {
        if (canchas.isNotEmpty()) {
            val focus = canchas.first()
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(LatLng(focus.lat, focus.lng), 13f)
            )
        } else if (userLocation != null) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(LatLng(userLocation.latitude, userLocation.longitude), 13f)
            )
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = hasLocationPermission
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = true,
            myLocationButtonEnabled = hasLocationPermission
        )
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
    userLocation: android.location.Location?,
    onCanchaClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(canchas, key = { it.id }) { cancha ->
            CanchaCard(
                cancha = cancha,
                userLocation = userLocation,
                onClick = { onCanchaClick(cancha.id) }
            )
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
private fun CanchaCard(
    cancha: Cancha,
    userLocation: android.location.Location?,
    onClick: () -> Unit
) {
    val distanceText = remember(userLocation, cancha) {
        if (userLocation != null) {
            val results = FloatArray(1)
            android.location.Location.distanceBetween(
                userLocation.latitude, userLocation.longitude,
                cancha.lat, cancha.lng,
                results
            )
            val distanceKm = results[0] / 1000
            "a ${String.format("%.1f", distanceKm)} km"
        } else null
    }

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
                Text(
                    text = "${cancha.direccion}, ${cancha.ciudad}${if (distanceText != null) " ($distanceText)" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
