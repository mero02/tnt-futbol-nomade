package com.example.futbol_tnt.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.futbol_tnt.data.model.TipoReporte
import com.example.futbol_tnt.presentation.viewmodel.ReporteUiState
import com.example.futbol_tnt.presentation.viewmodel.ReporteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReporteScreen(
    viewModel: ReporteViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var tipoSeleccionadoName by rememberSaveable { mutableStateOf(TipoReporte.ERROR.name) }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var expanded by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is ReporteUiState.Success) {
            // El snackbar lo manejamos en el Scaffold o simplemente volvemos
            onBack()
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reportar Problema") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Tu feedback nos ayuda a mejorar. Contanos qué pasó o qué sugerencia tenés.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Selector de Tipo
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = tipoSeleccionadoName.let { TipoReporte.valueOf(it) }.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Motivo") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    TipoReporte.values().forEach { tipo ->
                        DropdownMenuItem(
                            text = { Text(tipo.displayName) },
                            onClick = {
                                tipoSeleccionadoName = tipo.name
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de descripción
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                placeholder = { Text("Escribí aquí los detalles...") },
                modifier = Modifier.fillMaxWidth().weight(1f),
                minLines = 5
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState is ReporteUiState.Error) {
                Text(
                    text = (uiState as ReporteUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = { viewModel.enviarReporte(TipoReporte.valueOf(tipoSeleccionadoName), descripcion) },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is ReporteUiState.Loading,
                shape = MaterialTheme.shapes.medium
            ) {
                if (uiState is ReporteUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enviar Reporte")
                }
            }
        }
    }
}
