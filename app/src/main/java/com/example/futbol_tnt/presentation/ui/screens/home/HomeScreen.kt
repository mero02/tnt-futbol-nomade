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

private data class BottomNavItem(val title: String, val icon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSignOut: () -> Unit,
    onNavigateToAcercaDe: () -> Unit,
    onCrearPartido: () -> Unit,
    partidoViewModel: PartidoViewModel,
    modifier: Modifier = Modifier
) {
    val navItems = listOf(
        BottomNavItem("Canchas", Icons.Default.SportsSoccer),
        BottomNavItem("Reservas", Icons.Default.DateRange),
        BottomNavItem("Partidos", Icons.Default.Home),
        BottomNavItem("Perfil", Icons.Default.Person)
    )

    var selectedIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fútbol TNT") },
                actions = {
                    IconButton(onClick = onNavigateToAcercaDe) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Acerca De"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedIndex) {
                0 -> CanchasTab()
                1 -> MisReservasTab()
                2 -> PartidosTab(viewModel = partidoViewModel, onCrearPartido = onCrearPartido)
                3 -> PerfilTab(onSignOut = onSignOut)
            }
        }
    }
}
