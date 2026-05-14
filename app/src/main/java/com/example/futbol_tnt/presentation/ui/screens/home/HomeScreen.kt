package com.example.futbol_tnt.presentation.ui.screens.home

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.futbol_tnt.presentation.ui.screens.home.tabs.CanchasTab
import com.example.futbol_tnt.presentation.ui.screens.home.tabs.MisReservasTab
import com.example.futbol_tnt.presentation.ui.screens.home.tabs.PartidosTab
import com.example.futbol_tnt.presentation.ui.screens.home.tabs.PerfilTab
import com.example.futbol_tnt.presentation.viewmodel.PartidoViewModel
import com.example.futbol_tnt.presentation.viewmodel.ProfileViewModel
import com.example.futbol_tnt.presentation.viewmodel.ReservaViewModel
import kotlinx.coroutines.launch

private fun shareApp(context: Context) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(
            Intent.EXTRA_TEXT,
            "¡Descarga 'Entra a la cancha' y empezá a organizar tus partidos! Unite a nuestra comunidad: https://entraalacancha.example.com/download"
        )
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Compartir app")
    context.startActivity(shareIntent)
}

// Modelo interno para describir cada ítem del bottom navigation bar
private data class BottomNavItem(val title: String, val icon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSignOut: () -> Unit,
    onNavigateToAcercaDe: () -> Unit,
    onNavigateToReporte: () -> Unit,
    onNavigateToTarjetas: () -> Unit,
    onCrearPartido: (String?) -> Unit,
    onNavigateToCanchaDetail: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    partidoViewModel: PartidoViewModel,
    reservaViewModel: ReservaViewModel,
    profileViewModel: ProfileViewModel,
    modifier: Modifier = Modifier,
) {
    // Definición de las 4 tabs del bottom nav.
    // El índice de cada ítem corresponde al case en el when{} de abajo.
    val navItems = listOf(
        BottomNavItem("Inicio", Icons.Default.Home),            // índice 0
        BottomNavItem("Reservas", Icons.Default.DateRange),     // índice 1
        BottomNavItem("Partidos", Icons.Default.SportsSoccer),  // índice 2
        BottomNavItem("Perfil", Icons.Default.Person)           // índice 3
    )

    // selectedIndex guarda qué tab está activa. rememberSaveable asegura que se mantenga
    // incluso si se navega a otra pantalla y se vuelve.
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Manejo del botón atrás: si no estamos en la pestaña 0, el primer "atrás" nos lleva a Inicio.
    BackHandler(enabled = selectedIndex != 0) {
        selectedIndex = 0
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Entra a la cancha",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
                HorizontalDivider()

                // SECCIÓN: INICIO
                Text(
                    text = "Secciones",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )
                NavigationDrawerItem(
                    label = { Text("Inicio") },
                    selected = selectedIndex == 0,
                    onClick = {
                        scope.launch { drawerState.close() }
                        selectedIndex = 0
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Mis Reservas") },
                    selected = selectedIndex == 1,
                    onClick = {
                        scope.launch { drawerState.close() }
                        selectedIndex = 1
                    },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Partidos") },
                    selected = selectedIndex == 2,
                    onClick = {
                        scope.launch { drawerState.close() }
                        selectedIndex = 2
                    },
                    icon = { Icon(Icons.Default.SportsSoccer, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // SECCIÓN: CONFIGURACIONES
                Text(
                    text = "Configuraciones",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                )
                NavigationDrawerItem(
                    label = { Text("Mi Perfil") },
                    selected = selectedIndex == 3,
                    onClick = {
                        scope.launch { drawerState.close() }
                        selectedIndex = 3
                    },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Tus tarjetas") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToTarjetas()
                    },
                    icon = { Icon(Icons.Default.CreditCard, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // SECCIÓN: CONTACTANOS
                Text(
                    text = "Contáctanos",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                )
                NavigationDrawerItem(
                    label = { Text("Recomendanos") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        shareApp(context)
                    },
                    icon = { Icon(Icons.Default.Star, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Reportar problema") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToReporte()
                    },
                    icon = { Icon(Icons.Default.BugReport, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Acerca de") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToAcercaDe()
                    },
                    icon = { Icon(Icons.Default.Info, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text("Cerrar Sesión") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onSignOut()
                    },
                    icon = { Icon(Icons.Default.ExitToApp, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    ) {
        // Scaffold provee la estructura visual: topBar + bottomBar + contenido central.
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            when (selectedIndex) {
                                0 -> "Entra a la cancha"
                                1 -> "Mis Reservas"
                                2 -> "Partidos Organizados"
                                3 -> "Mi Perfil"
                                else -> "Entra a la cancha"
                            }
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Abrir menú"
                            )
                        }
                    },
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
            androidx.compose.foundation.layout.Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (selectedIndex) {
                    0 -> CanchasTab(onNavigateToCanchaDetail = onNavigateToCanchaDetail)
                    1 -> MisReservasTab(
                        viewModel = reservaViewModel,
                        onOrganizarPartido = { reservaId ->
                            onCrearPartido(reservaId)
                        }
                    )
                    2 -> PartidosTab(viewModel = partidoViewModel, onCrearPartido = { onCrearPartido(null) })
                    3 -> PerfilTab(
                        onSignOut = onSignOut,
                        viewModel = profileViewModel,
                        onEditProfile = onNavigateToProfile
                    )
                }
            }
        }
    }
}
