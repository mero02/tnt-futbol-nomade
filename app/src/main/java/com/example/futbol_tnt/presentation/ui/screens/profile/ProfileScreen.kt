package com.example.futbol_tnt.presentation.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.futbol_tnt.data.model.NivelJuego
import com.example.futbol_tnt.data.model.Posicion
import com.example.futbol_tnt.data.model.User
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
                    onSave = { name, pos, level, bio ->
                        viewModel.updateProfile(name, pos, level, bio)
                        scope.launch {
                            snackbarHostState.showSnackbar("Perfil actualizado correctamente")
                            onBack() // Volvemos a la pestaña de perfil después de guardar
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
    onSave: (String, Posicion?, NivelJuego?, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var displayName by remember { mutableStateOf(user.displayName ?: "") }
    var posicion by remember { mutableStateOf(user.posicion) }
    var nivel by remember { mutableStateOf(user.nivel) }
    var biografia by remember { mutableStateOf(user.biografia ?: "") }

    var expandedPos by remember { mutableStateOf(false) }
    var expandedNivel by remember { mutableStateOf(false) }

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
                .size(120.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Nombre
        OutlinedTextField(
            value = displayName,
            onValueChange = { displayName = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Posición Dropdown
        ExposedDropdownMenuBox(
            expanded = expandedPos,
            onExpandedChange = { expandedPos = !expandedPos },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = posicion?.name?.replace("_", " ")?.lowercase()?.capitalize() ?: "No definida",
                onValueChange = {},
                readOnly = true,
                label = { Text("Posición") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPos) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedPos,
                onDismissRequest = { expandedPos = false }
            ) {
                Posicion.values().forEach { pos ->
                    DropdownMenuItem(
                        text = { Text(pos.name.replace("_", " ").lowercase().capitalize()) },
                        onClick = {
                            posicion = pos
                            expandedPos = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Nivel Dropdown
        ExposedDropdownMenuBox(
            expanded = expandedNivel,
            onExpandedChange = { expandedNivel = !expandedNivel },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = nivel?.name?.lowercase()?.capitalize() ?: "No definido",
                onValueChange = {},
                readOnly = true,
                label = { Text("Nivel de juego") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedNivel) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedNivel,
                onDismissRequest = { expandedNivel = false }
            ) {
                NivelJuego.values().forEach { n ->
                    DropdownMenuItem(
                        text = { Text(n.name.lowercase().capitalize()) },
                        onClick = {
                            nivel = n
                            expandedNivel = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Biografía
        OutlinedTextField(
            value = biografia,
            onValueChange = { biografia = it },
            label = { Text("Sobre mí") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onSave(displayName, posicion, nivel, biografia) },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Guardar Cambios")
        }
    }
}

// Extensión simple para capitalizar strings
private fun String.capitalize() = this.replaceFirstChar { it.uppercase() }
