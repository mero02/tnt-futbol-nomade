package com.example.futbol_tnt.presentation.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.futbol_tnt.core.geofence.GeofenceManager
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

/**
 * Efecto que sincroniza las geocercas activas cuando el usuario ya concedio los
 * permisos de ubicacion (foreground + background). Se coloca en un punto con
 * sesion iniciada (ej. el grafo de navegacion principal).
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GeofenceSyncEffect() {
    val context = LocalContext.current
    val fineState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    LaunchedEffect(fineState.status.isGranted) {
        if (fineState.status.isGranted && tieneBackgroundLocation(context)) {
            GeofenceManager(context).sincronizarGeocercas()
        }
    }
}

/**
 * Tarjeta de Ajustes que explica y solicita el permiso "Permitir todo el tiempo"
 * necesario para la deteccion automatica de llegada a la cancha (HU-37).
 *
 * Flujo en dos pasos: primero ACCESS_FINE_LOCATION (foreground) y luego, por
 * separado, ACCESS_BACKGROUND_LOCATION (Android 10+). En Android 11+ el sistema
 * lleva al usuario a Ajustes para elegir "Permitir todo el tiempo".
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GeofencePermisoCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val fineState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    // El permiso de background solo existe/es requerido en Android 10+ (API 29).
    val backgroundNecesario = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    val backgroundState = if (backgroundNecesario) {
        rememberPermissionState(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    } else null

    var backgroundConcedido by remember { mutableStateOf(tieneBackgroundLocation(context)) }

    // Reevaluamos el estado del permiso de background al recomponer.
    LaunchedEffect(fineState.status.isGranted, backgroundState?.status?.isGranted) {
        backgroundConcedido = tieneBackgroundLocation(context)
        if (fineState.status.isGranted && backgroundConcedido) {
            GeofenceManager(context).sincronizarGeocercas()
        }
    }

    val todoConcedido = fineState.status.isGranted && backgroundConcedido

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = "Detección automática de llegada",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Marca tu asistencia sola al llegar a la cancha, incluso con la app cerrada. " +
                            "Para eso necesitamos el permiso de ubicación en modo \"Permitir todo el tiempo\".",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            when {
                todoConcedido -> {
                    Text(
                        text = "✓ Detección automática activada",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                !fineState.status.isGranted -> {
                    Button(
                        onClick = { fineState.launchPermissionRequest() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Permitir ubicación")
                    }
                }
                else -> {
                    // Fine concedido, falta el background: se pide por separado.
                    Button(
                        onClick = { backgroundState?.launchPermissionRequest() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Permitir todo el tiempo")
                    }
                }
            }
        }
    }
}

private fun tieneBackgroundLocation(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return true
    return ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_BACKGROUND_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}
