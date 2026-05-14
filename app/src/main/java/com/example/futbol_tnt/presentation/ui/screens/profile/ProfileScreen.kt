package com.example.futbol_tnt.presentation.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.futbol_tnt.data.model.*
import com.example.futbol_tnt.presentation.viewmodel.ProfileUiState
import com.example.futbol_tnt.presentation.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    uid: String,
    viewModel: ProfileViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uid) {
        viewModel.loadProfile(uid)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when (val state = uiState) {
            is ProfileUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is ProfileUiState.Success -> {
                ProfileContent(
                    user = state.user,
                    onSave = { updatedUser ->
                        viewModel.updateProfile(updatedUser)
                        scope.launch {
                            snackbarHostState.showSnackbar("Perfil actualizado correctamente")
                            onBack()
                        }
                    },
                    modifier = Modifier.padding(padding)
                )
            }
            is ProfileUiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadProfile(uid) }) {
                        Text("Reintentar")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileContent(
    user: User,
    onSave: (User) -> Unit,
    modifier: Modifier = Modifier
) {
    var displayName by remember { mutableStateOf(user.displayName ?: "") }
    var apodo by remember { mutableStateOf(user.apodo ?: "") }
    var telefono by remember { mutableStateOf(user.telefono ?: "") }
    var biografia by remember { mutableStateOf(user.biografia ?: "") }
    var fechaNacimiento by remember { mutableStateOf(user.fechaNacimiento ?: "") }
    var sexo by remember { mutableStateOf(user.sexo) }
    var posicion by remember { mutableStateOf(user.posicion) }
    var nivel by remember { mutableStateOf(user.nivel) }
    var piernaDominante by remember { mutableStateOf(user.piernaDominante) }
    var formatoPreferido by remember { mutableStateOf(user.formatoPreferido) }
    var equipo by remember { mutableStateOf(user.equipo ?: "") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Foto de Perfil
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(user.photoUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Foto de perfil",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(24.dp))

        SectionTitle("Datos de cuenta")

        OutlinedTextField(
            value = displayName,
            onValueChange = { displayName = it },
            label = { Text("Nombre Completo") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = user.email ?: "",
            onValueChange = {},
            label = { Text("Email (No editable)") },
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = telefono,
            onValueChange = { telefono = it },
            label = { Text("Teléfono") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        SectionTitle("Perfil deportivo")

        OutlinedTextField(
            value = apodo,
            onValueChange = { apodo = it },
            label = { Text("Apodo / Nickname") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = equipo,
            onValueChange = { equipo = it },
            label = { Text("Equipo") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        DatePickerField(
            label = "Fecha de nacimiento",
            value = fechaNacimiento,
            onValueChange = { fechaNacimiento = it }
        )

        Spacer(modifier = Modifier.height(8.dp))

        EnumDropdown(
            label = "Sexo",
            selectedOption = sexo,
            options = Sexo.values(),
            onOptionSelected = { sexo = it }
        )

        Spacer(modifier = Modifier.height(8.dp))

        EnumDropdown(
            label = "Posición",
            selectedOption = posicion,
            options = Posicion.values(),
            onOptionSelected = { posicion = it }
        )

        Spacer(modifier = Modifier.height(8.dp))

        EnumDropdown(
            label = "Nivel de juego",
            selectedOption = nivel,
            options = NivelJuego.values(),
            onOptionSelected = { nivel = it }
        )

        Spacer(modifier = Modifier.height(8.dp))

        EnumDropdown(
            label = "Pierna dominante",
            selectedOption = piernaDominante,
            options = PiernaDominante.values(),
            onOptionSelected = { piernaDominante = it }
        )

        Spacer(modifier = Modifier.height(8.dp))

        EnumDropdown(
            label = "Formato preferido",
            selectedOption = formatoPreferido,
            options = FormatoPreferido.values(),
            onOptionSelected = { formatoPreferido = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = biografia,
            onValueChange = { biografia = it },
            label = { Text("Sobre mí / Biografía") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val updatedUser = user.copy(
                    displayName = displayName,
                    apodo = apodo,
                    telefono = telefono,
                    biografia = biografia,
                    fechaNacimiento = fechaNacimiento,
                    sexo = sexo,
                    posicion = posicion,
                    nivel = nivel,
                    piernaDominante = piernaDominante,
                    formatoPreferido = formatoPreferido,
                    equipo = equipo
                )
                onSave(updatedUser)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Guardar Cambios")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        fontWeight = FontWeight.Bold
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    val displayDate = if (value.isNotBlank()) {
        try {
            LocalDate.parse(value).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        } catch (e: Exception) {
            value
        }
    } else {
        "Seleccionar fecha"
    }

    OutlinedTextField(
        value = displayDate,
        onValueChange = { },
        readOnly = true,
        label = { Text(label) },
        trailingIcon = {
            IconButton(onClick = { showDatePicker = true }) {
                Icon(Icons.Default.CalendarMonth, contentDescription = "Seleccionar fecha")
            }
        },
        modifier = Modifier.fillMaxWidth()
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        // DatePicker trabaja en UTC. Usamos ZoneOffset.UTC para evitar saltos de día
                        // por la diferencia horaria local.
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()
                        onValueChange(date.toString()) // Guarda en formato ISO YYYY-MM-DD
                    }
                    showDatePicker = false
                }) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> EnumDropdown(
    label: String,
    selectedOption: T?,
    options: Array<T>,
    onOptionSelected: (T) -> Unit
) where T : Enum<T>, T : HasDisplayName {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedOption?.displayName ?: "No definido",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.displayName) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun String.capitalize() = this.replaceFirstChar { it.uppercase() }
