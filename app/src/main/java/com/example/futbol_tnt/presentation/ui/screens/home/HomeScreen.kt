package com.example.futbol_tnt.presentation.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.futbol_tnt.presentation.ui.screens.home.tabs.CanchasTab
import com.example.futbol_tnt.presentation.ui.screens.home.tabs.MisReservasTab
import com.example.futbol_tnt.presentation.ui.screens.home.tabs.PartidosTab
import com.example.futbol_tnt.presentation.ui.screens.home.tabs.PerfilTab
import com.example.futbol_tnt.presentation.viewmodel.PartidoViewModel

// Modelo interno para describir cada ítem del bottom navigation bar
private data class BottomNavItem(val title: String, val icon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSignOut: () -> Unit,
    onNavigateToAcercaDe: () -> Unit,
    onCrearPartido: () -> Unit,
    onNavigateToCanchaDetail: (String) -> Unit,
    partidoViewModel: PartidoViewModel,
    modifier: Modifier = Modifier
) {
    // Definición de las 4 tabs del bottom nav.
    // El índice de cada ítem corresponde al case en el when{} de abajo.
    val navItems = listOf(
        BottomNavItem("Canchas", Icons.Default.SportsSoccer),   // índice 0
        BottomNavItem("Reservas", Icons.Default.DateRange),     // índice 1
        BottomNavItem("Partidos", Icons.Default.Home),          // índice 2
        BottomNavItem("Perfil", Icons.Default.Person)           // índice 3
    )

    // selectedIndex guarda qué tab está activa. remember{} lo mantiene vivo
    // entre recomposiciones sin reiniciarse.
    var selectedIndex by remember { mutableIntStateOf(0) }

    // Scaffold provee la estructura visual: topBar + bottomBar + contenido central.
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Entra a la cancha") },
                actions = {
                    // Botón ⓘ en la esquina superior derecha → navega a AcercaDe
                    IconButton(onClick = onNavigateToAcercaDe) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Acerca De"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                // Se genera un ítem por cada tab. Al tocar uno, selectedIndex cambia
                // y Compose re-renderiza el contenido central con la tab correcta.
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        // El contenido central cambia según la tab seleccionada.
        // paddingValues asegura que el contenido no quede debajo del topBar/bottomBar.
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedIndex) {
                0 -> CanchasTab(onNavigateToCanchaDetail = onNavigateToCanchaDetail)
                1 -> MisReservasTab()
                2 -> PartidosTab(viewModel = partidoViewModel, onCrearPartido = onCrearPartido)
                3 -> PerfilTab(onSignOut = onSignOut)
            }
        }
    }
}
