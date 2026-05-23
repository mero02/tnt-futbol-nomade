package com.example.futbol_tnt.presentation.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.futbol_tnt.data.model.MarcaTarjeta
import com.example.futbol_tnt.data.model.Tarjeta
import com.example.futbol_tnt.presentation.viewmodel.TarjetaUiState
import com.example.futbol_tnt.presentation.viewmodel.TarjetaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisTarjetasScreen(
    viewModel: TarjetaViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadTarjetas()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Tarjetas") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Tarjeta")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is TarjetaUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is TarjetaUiState.Success -> {
                    if (state.tarjetas.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.CreditCard,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No tenés tarjetas guardadas", color = MaterialTheme.colorScheme.outline)
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(state.tarjetas) { tarjeta ->
                                CardItem(
                                    tarjeta = tarjeta,
                                    onDelete = { viewModel.eliminarTarjeta(tarjeta.id) }
                                )
                            }
                        }
                    }
                }
                is TarjetaUiState.Error -> {
                    Text(
                        state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddTarjetaDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { nuevaTarjeta ->
                viewModel.agregarTarjeta(nuevaTarjeta)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun CardItem(tarjeta: Tarjeta, onDelete: () -> Unit) {
    val gradient = when (tarjeta.marca) {
        MarcaTarjeta.VISA -> Brush.linearGradient(listOf(Color(0xFF1A237E), Color(0xFF3949AB)))
        MarcaTarjeta.MASTERCARD -> Brush.linearGradient(listOf(Color(0xFF37474F), Color(0xFF546E7A)))
        else -> Brush.linearGradient(listOf(Color(0xFF212121), Color(0xFF424242)))
    }

    Card(
        modifier = Modifier.fillMaxWidth().height(200.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(gradient).padding(24.dp)) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = tarjeta.marca.displayName,
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.White.copy(alpha = 0.7f))
                    }
                }

                Text(
                    text = "**** **** **** ${tarjeta.last4}",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    letterSpacing = 2.sp
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("TITULAR", color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall)
                        Text(tarjeta.nombreTitular.uppercase(), color = Color.White, fontWeight = FontWeight.Medium)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("VENCE", color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall)
                        Text(tarjeta.vencimiento, color = Color.White, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTarjetaDialog(onDismiss: () -> Unit, onConfirm: (Tarjeta) -> Unit) {
    var titular by rememberSaveable { mutableStateOf("") }
    var numero by rememberSaveable { mutableStateOf("") }
    var vencimiento by rememberSaveable { mutableStateOf("") }
    var marcaName by rememberSaveable { mutableStateOf(MarcaTarjeta.VISA.name) }
    var expanded by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Tarjeta") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = titular,
                    onValueChange = { titular = it },
                    label = { Text("Nombre en la tarjeta") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = numero,
                    onValueChange = { if (it.length <= 16) numero = it },
                    label = { Text("Número de tarjeta") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = vencimiento,
                        onValueChange = { if (it.length <= 5) vencimiento = it },
                        label = { Text("Vence (MM/AA)") },
                        modifier = Modifier.weight(1f)
                    )
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = MarcaTarjeta.valueOf(marcaName).displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Marca") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            MarcaTarjeta.values().forEach { m ->
                                DropdownMenuItem(
                                    text = { Text(m.displayName) },
                                    onClick = {
                                        marcaName = m.name
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (titular.isNotBlank() && numero.length >= 4 && vencimiento.length == 5) {
                        onConfirm(
                            Tarjeta(
                                nombreTitular = titular,
                                last4 = numero.takeLast(4),
                                vencimiento = vencimiento,
                                marca = MarcaTarjeta.valueOf(marcaName)
                            )
                        )
                    }
                }
            ) {
                Text("Agregar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
