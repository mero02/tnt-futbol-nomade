package com.example.futbol_tnt.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.futbol_tnt.presentation.viewmodel.CheckoutEvento
import com.example.futbol_tnt.presentation.viewmodel.CheckoutViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    canchaId: String,
    fechaHoraStr: String,
    duracion: Double,
    viewModel: CheckoutViewModel,
    onBack: () -> Unit,
    onSuccess: (String) -> Unit
) {
    val cancha by viewModel.cancha.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val evento by viewModel.evento.collectAsState()

    var showSuccessDialog by rememberSaveable { mutableStateOf(false) }
    var reservedId by rememberSaveable { mutableStateOf("") }

    val fechaHora = remember { LocalDateTime.parse(fechaHoraStr) }
    val total = (cancha?.precioPorHora ?: 0.0) * duracion
    val sugeridoPersona = total / 10 // Asumiendo 10 personas por defecto

    var montoSeleccionado by rememberSaveable { mutableDoubleStateOf(0.0) }
    var selectedOption by rememberSaveable { mutableIntStateOf(1) } // 0: Nada, 1: Tu parte, 2: Total
    var isInitialized by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(canchaId) {
        viewModel.cargarCancha(canchaId)
    }

    // Inicializar monto una vez que la cancha carga, pero solo una vez
    LaunchedEffect(cancha) {
        if (!isInitialized && cancha != null) {
            val total = (cancha?.precioPorHora ?: 0.0) * duracion
            montoSeleccionado = total / 10
            isInitialized = true
        }
    }

    LaunchedEffect(evento) {
        if (evento is CheckoutEvento.PagoExitoso) {
            reservedId = (evento as CheckoutEvento.PagoExitoso).reservaId
            showSuccessDialog = true
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("¡Reserva Realizada!") },
            text = {
                Text(if (selectedOption == 2)
                    "Tu cancha está confirmada y pagada."
                    else "Tu reserva quedó registrada. Recordá completar el pago para confirmarla.")
            },
            confirmButton = {
                Button(onClick = {
                    showSuccessDialog = false
                    viewModel.limpiarEvento()
                    onSuccess(reservedId)
                }) {
                    Text("Organizar Partido")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSuccessDialog = false
                    viewModel.limpiarEvento()
                    onBack() // Vuelve a la home/canchas
                }) {
                    Text("Volver al Inicio")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resumen de Reserva") },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card de Resumen
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = cancha?.nombre ?: "Cargando...",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = fechaHora.format(DateTimeFormatter.ofPattern("EEEE dd 'de' MMMM, HH:mm'hs'")),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Duración: ${duracion}h",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Divider(modifier = Modifier.padding(vertical = 12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total cancha:", fontWeight = FontWeight.Bold)
                        Text("$${total.toInt()}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Text(
                text = "¿Cuánto querés pagar ahora?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Opciones de Pago
            PaymentOption(
                title = "Solo reservar (Pagar luego)",
                subtitle = "Tu reserva quedará PENDIENTE hasta que realices un pago.",
                icon = Icons.Default.Info,
                selected = selectedOption == 0,
                onClick = {
                    selectedOption = 0
                    montoSeleccionado = 0.0
                }
            )

            PaymentOption(
                title = "Pagar mi parte ($${sugeridoPersona.toInt()})",
                subtitle = "Calculado según el costo por persona (1/10).",
                icon = Icons.Default.Payments,
                selected = selectedOption == 1,
                onClick = {
                    selectedOption = 1
                    montoSeleccionado = sugeridoPersona
                }
            )

            PaymentOption(
                title = "Pagar total ($${total.toInt()})",
                subtitle = "Asegurá la cancha pagando la totalidad ahora.",
                icon = Icons.Default.CreditCard,
                selected = selectedOption == 2,
                onClick = {
                    selectedOption = 2
                    montoSeleccionado = total
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            if (evento is CheckoutEvento.Error) {
                Text(
                    text = (evento as CheckoutEvento.Error).mensaje,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            Button(
                onClick = {
                    viewModel.confirmarReserva(
                        fechaHora = fechaHora,
                        duracion = duracion,
                        montoAPagar = montoSeleccionado,
                        esTotal = selectedOption == 2
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    val text = if (montoSeleccionado > 0) "Pagar $${montoSeleccionado.toInt()} y Reservar" else "Confirmar Reserva"
                    Text(text)
                }
            }
        }
    }
}

@Composable
fun PaymentOption(
    title: String,
    subtitle: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        border = if (selected) CardDefaults.outlinedCardBorder().copy(1.dp) else CardDefaults.outlinedCardBorder(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            RadioButton(selected = selected, onClick = onClick)
        }
    }
}
