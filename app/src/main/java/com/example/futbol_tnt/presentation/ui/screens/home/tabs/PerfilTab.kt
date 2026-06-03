package com.example.futbol_tnt.presentation.ui.screens.home.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.futbol_tnt.presentation.ui.screens.home.components.HeaderSection
import com.example.futbol_tnt.presentation.ui.screens.profile.ReadOnlyProfileContent
import com.example.futbol_tnt.presentation.ui.screens.profile.ReputacionContent
import com.example.futbol_tnt.presentation.viewmodel.ProfileUiState
import com.example.futbol_tnt.presentation.viewmodel.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
internal fun PerfilTab(
    onSignOut: () -> Unit,
    onEditProfile: () -> Unit,
    viewModel: ProfileViewModel,
    modifier: Modifier = Modifier,
) {
    val firebaseUser = remember { FirebaseAuth.getInstance().currentUser }
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(firebaseUser?.uid) {
        firebaseUser?.uid?.let { viewModel.loadProfile(it) }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        HeaderSection(
            titulo = "Mi Perfil",
            subtitulo = "Gestiona tu carrera deportiva",
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        when (val state = uiState) {
            is ProfileUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is ProfileUiState.Success -> {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Ficha") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Reputación") }
                    )
                }

                if (selectedTab == 0) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        ReadOnlyProfileContent(
                            user = state.user,
                            modifier = Modifier.fillMaxSize(),
                            footer = {
                                Spacer(modifier = Modifier.height(24.dp))
                                OutlinedButton(
                                    onClick = onSignOut,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ExitToApp, null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Cerrar Sesión")
                                }
                            }
                        )

                        // Botón Editar flotante para el dueño
                        FloatingActionButton(
                            onClick = onEditProfile,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar")
                        }
                    }
                } else {
                    ReputacionContent(
                        calificaciones = state.calificaciones,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            is ProfileUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
