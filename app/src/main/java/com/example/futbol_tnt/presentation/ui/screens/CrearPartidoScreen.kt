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
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.futbol_tnt.data.repository.IReservaRepository
import com.example.futbol_tnt.presentation.viewmodel.PartidoEvento
import com.example.futbol_tnt.presentation.viewmodel.PartidoViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.UUID


private val crearPartidoFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

// Agrupa todos los campos del formulario en un data class para pasarlos juntos a validar y buildear.
data class FormPartido(
    val nombreLocal: String,
    val nombreVisitante: String,
    val cancha: com.example.futbol_tnt.data.model.Cancha?,
    val fecha: String,
    val hora: String,
    val jugadoresMax: String,
    val precio: String
)

// Retorna un mensaje de error si algún campo es inválido, o null si todo está OK.
// Se ejecuta antes de crear el partido para darle feedback al usuario.
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

// Construye el objeto Partido a partir del formulario validado.
// Se llama solo después de que validarFormPartido() retorna null (sin errores).
fun buildPartido(
    form: FormPartido,
    reservaId: String? = null,
    partidoId: String? = null,
    jugadoresActuales: Int = 1
): Partido {
    val fechaHora = LocalDateTime.parse("${form.fecha} ${form.hora}", crearPartidoFormatter)
    val maxJugadores = form.jugadoresMax.toInt()

    return Partido(
        id = partidoId ?: UUID.randomUUID().toString(),
        nombreLocal = form.nombreLocal.trim(),
        nombreVisitante = form.nombreVisitante.trim(),
        fecha = fechaHora,
        cancha = form.cancha!!,
        precioPorPersona = form.precio.toDouble(),
        jugadoresActuales = jugadoresActuales,
        jugadoresMaximos = maxJugadores,
        estado = if (jugadoresActuales >= maxJugadores) EstadoPartido.LLENO else EstadoPartido.ABIERTO,
        nombreOrganizador = "Yo",
        reservaId = reservaId
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearPartidoScreen(
    viewModel: PartidoViewModel,
    reservaId: String? = null,
    reservaRepository: IReservaRepository? = null,
    onBack: () -> Unit
) {
    // Estado local de cada campo del formulario. remember{} los mantiene entre recomposiciones.
    var nombreLocal by remember { mutableStateOf("") }
    var nombreVisitante by remember { mutableStateOf("") }
    var canchaSeleccionada by remember { mutableStateOf<Cancha?>(null) }
    var showCanchaMenu by remember { mutableStateOf(false) }
    var fecha by remember { mutableStateOf("") }
    var hora by remember { mutableStateOf("") }
    var jugadoresMax by remember { mutableStateOf("10") }
    var precio by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var isFromReserva by remember { mutableStateOf(false) }
    var precioTotalReserva by remember { mutableStateOf(0.0) }
    var existingMatchId by remember { mutableStateOf<String?>(null) }
    var jugadoresActuales by remember { mutableIntStateOf(1) }

    // Carga de datos de reserva si existe
    LaunchedEffect(reservaId) {
        if (reservaId != null && reservaRepository != null) {
            val reserva = reservaRepository.getReservaById(reservaId)
            reserva?.let { r ->
                canchaSeleccionada = r.cancha
                fecha = r.fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                hora = r.fecha.format(DateTimeFormatter.ofPattern("HH:mm"))
                isFromReserva = true
                precioTotalReserva = r.precioTotal

                // Buscar si ya existe un partido para esta reserva
                val existingMatch = viewModel.getPartidoByReservaId(reservaId)
                if (existingMatch != null) {
                    existingMatchId = existingMatch.id
                    nombreLocal = existingMatch.nombreLocal
                    nombreVisitante = existingMatch.nombreVisitante
                    jugadoresMax = existingMatch.jugadoresMaximos.toString()
                    precio = existingMatch.precioPorPersona.toInt().toString()
                    jugadoresActuales = existingMatch.jugadoresActuales
                } else {
                    nombreLocal = r.nombreEquipo ?: ""
                    jugadoresActuales = 1
                    // Cálculo inicial del precio por persona si no hay partido previo
                    val jug = jugadoresMax.toIntOrNull() ?: 10
                    precio = (r.precioTotal / jug).toInt().toString()
                }
            }
        }
    }

    // Escucha eventos del ViewModel. PartidoCreadoExito se emite cuando el partido
    // se guardo OK en Firestore — entonces volvemos a PartidosTab.
    // Error se emite si fallo la escritura (sin auth, sin red, reglas, etc.) — lo
    // mostramos en el card de error y limpiamos para permitir reintentar.
    val evento by viewModel.evento.collectAsState()
    LaunchedEffect(evento) {
        when (val e = evento) {
            is PartidoEvento.PartidoCreadoExito -> {
                viewModel.limpiarEvento()
                onBack()
            }
            is PartidoEvento.Error -> {
                errorMsg = e.mensaje
                viewModel.limpiarEvento()
            }
            else -> {}
        }
    }

    // Función local que agrupa validación + creación. Se llama al tocar "Crear Partido".
    fun validarYCrear() {
        val form = FormPartido(nombreLocal, nombreVisitante, canchaSeleccionada, fecha, hora, jugadoresMax, precio)
        val error = validarFormPartido(form)
        if (error != null) { errorMsg = error; return }
        viewModel.crearPartido(buildPartido(form, reservaId, existingMatchId, jugadoresActuales))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Partido") },
                navigationIcon = {
                    // Flecha ← en la TopBar — vuelve a PartidosTab sin crear nada
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                )
            )
        }
    ) { padding ->
        // LazyColumn para que el formulario sea scrolleable en pantallas pequeñas
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
            // Cada OutlinedTextField usa onValueChange para actualizar el estado local
            // en tiempo real mientras el usuario escribe.
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
                // ExposedDropdownMenuBox muestra un TextField de solo lectura con flecha.
                // Al tocarlo, showCanchaMenu se pone en true y aparece el menú con las canchas de MockData.
                ExposedDropdownMenuBox(
                    expanded = showCanchaMenu && !isFromReserva,
                    onExpandedChange = { if (!isFromReserva) showCanchaMenu = it }
                ) {
                    OutlinedTextField(
                        value = canchaSeleccionada?.nombre ?: "",
                        onValueChange = {},
                        readOnly = true,
                        enabled = !isFromReserva,
                        label = { Text("Cancha *") },
                        trailingIcon = {
                            if (!isFromReserva) ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCanchaMenu)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    if (!isFromReserva) {
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
            }
            item {
                // Fecha y hora en una sola fila con peso 1f cada una (50% ancho cada campo)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = fecha,
                        onValueChange = { fecha = it },
                        label = { Text("Fecha *") },
                        readOnly = isFromReserva,
                        placeholder = { Text("dd/MM/yyyy") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = hora,
                        onValueChange = { hora = it },
                        label = { Text("Hora *") },
                        readOnly = isFromReserva,
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
                    onValueChange = { newValue ->
                        jugadoresMax = newValue
                        // Recalcular precio por persona automáticamente si viene de una reserva
                        if (isFromReserva && precioTotalReserva > 0) {
                            val count = newValue.toIntOrNull()
                            if (count != null && count > 0) {
                                precio = (precioTotalReserva / count).toInt().toString()
                            }
                        }
                    },
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
                // El card de error solo se muestra si errorMsg != null.
                // Desaparece automáticamente cuando el usuario corrige y vuelve a intentar.
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
                // Botón principal: valida el form y si pasa, crea el partido en el ViewModel.
                // El ViewModel emite PartidoCreadoExito y LaunchedEffect llama onBack().
                Button(
                    onClick = { validarYCrear() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.SportsSoccer, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardar")
                }
            }
        }
    }
}
