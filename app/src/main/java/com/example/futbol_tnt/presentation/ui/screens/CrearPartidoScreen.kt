package com.example.futbol_tnt.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.futbol_tnt.data.model.Cancha
import com.example.futbol_tnt.data.model.EstadoPartido
import com.example.futbol_tnt.data.model.MockData
import com.example.futbol_tnt.data.model.Partido
import com.example.futbol_tnt.presentation.viewmodel.PartidoEvento
import com.example.futbol_tnt.presentation.viewmodel.PartidoViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.UUID


private val crearPartidoFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

data class FormPartido(
    val nombreLocal: String,
    val nombreVisitante: String,
    val cancha: com.example.futbol_tnt.data.model.Cancha?,
    val fecha: String,
    val hora: String,
    val jugadoresMax: String,
    val precio: String
)

fun validarFormPartido(form: FormPartido): String? {
    if (form.nombreLocal.isBlank()) return "Ingresá el nombre del equipo local"
    if (form.nombreVisitante.isBlank()) return "Ingresá el nombre del equipo visitante"
    if (form.nombreLocal.trim() == form.nombreVisitante.trim()) return "Los equipos deben tener nombres distintos"
    if (form.cancha == null) return "Seleccioná una cancha"
    if (form.fecha.isBlank() || form.hora.isBlank()) return "Ingresá fecha y hora"
    val jugadores = form.jugadoresMax.toIntOrNull()
    if (jugadores == null || jugadores < 5 || jugadores > 22) return "Jugadores: entre 5 y 22"
    val precio = form.precio.toDoubleOrNull()
    if (precio == null || precio <= 0) return "Ingresá un precio válido"
    return try {
        val fechaHora = LocalDateTime.parse("${form.fecha} ${form.hora}", crearPartidoFormatter)
        if (fechaHora.isBefore(LocalDateTime.now())) "La fecha debe ser futura" else null
    } catch (e: DateTimeParseException) {
        "Formato incorrecto. Usá dd/MM/yyyy y HH:mm"
    }
}

fun buildPartido(form: FormPartido): Partido {
    val fechaHora = LocalDateTime.parse("${form.fecha} ${form.hora}", crearPartidoFormatter)
    return Partido(
        id = UUID.randomUUID().toString(),
        nombreLocal = form.nombreLocal.trim(),
        nombreVisitante = form.nombreVisitante.trim(),
        fecha = fechaHora,
        cancha = form.cancha!!,
        precioPorPersona = form.precio.toDouble(),
        jugadoresActuales = 0,
        jugadoresMaximos = form.jugadoresMax.toInt(),
        estado = EstadoPartido.ABIERTO,
        nombreOrganizador = "Yo"
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearPartidoScreen(
    viewModel: PartidoViewModel,
    onBack: () -> Unit
) {
    var nombreLocal by remember { mutableStateOf("") }
    var nombreVisitante by remember { mutableStateOf("") }
    var canchaSeleccionada by remember { mutableStateOf<Cancha?>(null) }
    var showCanchaMenu by remember { mutableStateOf(false) }
    var fecha by remember { mutableStateOf("") }
    var hora by remember { mutableStateOf("") }
    var jugadoresMax by remember { mutableStateOf("10") }
    var precio by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val evento by viewModel.evento.collectAsState()

    LaunchedEffect(evento) {
        if (evento is PartidoEvento.PartidoCreadoExito) {
            viewModel.limpiarEvento()
            onBack()
        }
    }

    fun validarYCrear() {
        val form = FormPartido(nombreLocal, nombreVisitante, canchaSeleccionada, fecha, hora, jugadoresMax, precio)
        val error = validarFormPartido(form)
        if (error != null) { errorMsg = error; return }
        viewModel.crearPartido(buildPartido(form))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Partido") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                Text(
                    text = "Datos del partido",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            item {
                OutlinedTextField(
                    value = nombreLocal,
                    onValueChange = { nombreLocal = it },
                    label = { Text("Equipo local *") },
                    placeholder = { Text("Ej: Los Chetos FC") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            item {
                OutlinedTextField(
                    value = nombreVisitante,
                    onValueChange = { nombreVisitante = it },
                    label = { Text("Equipo visitante *") },
                    placeholder = { Text("Ej: Los Boludos") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            item {
                ExposedDropdownMenuBox(
                    expanded = showCanchaMenu,
                    onExpandedChange = { showCanchaMenu = it }
                ) {
                    OutlinedTextField(
                        value = canchaSeleccionada?.nombre ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Cancha *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCanchaMenu) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = showCanchaMenu,
                        onDismissRequest = { showCanchaMenu = false }
                    ) {
                        MockData.canchas.forEach { cancha ->
                            DropdownMenuItem(
                                text = { Text("${cancha.nombre} — ${cancha.tipo.name.replace("_", " ")}") },
                                onClick = {
                                    canchaSeleccionada = cancha
                                    showCanchaMenu = false
                                }
                            )
                        }
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = fecha,
                        onValueChange = { fecha = it },
                        label = { Text("Fecha *") },
                        placeholder = { Text("dd/MM/yyyy") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = hora,
                        onValueChange = { hora = it },
                        label = { Text("Hora *") },
                        placeholder = { Text("HH:mm") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
            item {
                OutlinedTextField(
                    value = jugadoresMax,
                    onValueChange = { jugadoresMax = it },
                    label = { Text("Jugadores máximos *") },
                    placeholder = { Text("Entre 5 y 22") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            item {
                OutlinedTextField(
                    value = precio,
                    onValueChange = { precio = it },
                    label = { Text("Precio por persona *") },
                    placeholder = { Text("Ej: 2500") },
                    prefix = { Text("$") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
            item {
                errorMsg?.let { msg ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = msg,
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = { validarYCrear() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.SportsSoccer, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Crear Partido")
                }
            }
        }
    }
}
