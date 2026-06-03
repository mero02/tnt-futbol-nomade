package com.example.futbol_tnt.presentation.ui.screens.home

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.futbol_tnt.presentation.ui.screens.home.tabs.CanchasTab
import com.example.futbol_tnt.presentation.ui.screens.home.tabs.MisReservasTab
import com.example.futbol_tnt.presentation.ui.screens.home.tabs.PartidosTab
import com.example.futbol_tnt.presentation.ui.screens.home.tabs.PerfilTab
import com.example.futbol_tnt.presentation.viewmodel.NotificacionViewModel
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

private data class BottomNavItem(val title: String, val icon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSignOut: () -> Unit,
    onNavigateToAcercaDe: () -> Unit,
    onNavigateToReporte: () -> Unit,
    onNavigateToTarjetas: () -> Unit,
    onNavigateToNotificaciones: () -> Unit,
    onCrearPartido: (String?) -> Unit,
    onNavigateToCanchaDetail: (String) -> Unit,
    onNavigateToProfile: (String?) -> Unit,
    partidoViewModel: PartidoViewModel,
    reservaViewModel: ReservaViewModel,
    profileViewModel: ProfileViewModel,
    notificacionViewModel: NotificacionViewModel,
    modifier: Modifier = Modifier,
) {
    val navItems = listOf(
        BottomNavItem("Inicio", Icons.Default.Home),
        BottomNavItem("Reservas", Icons.Default.DateRange),
        BottomNavItem("Partidos", Icons.Default.SportsSoccer),
        BottomNavItem("Perfil", Icons.Default.Person)
    )

    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val unreadNotifs by notificacionViewModel.unreadCount.collectAsState()

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
                    icon = { Icon(Icons.Default.Home, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Mis Reservas") },
                    selected = selectedIndex == 1,
                    onClick = {
                        scope.launch { drawerState.close() }
                        selectedIndex = 1
                    },
                    icon = { Icon(Icons.Default.DateRange, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Partidos") },
                    selected = selectedIndex == 2,
                    onClick = {
                        scope.launch { drawerState.close() }
                        selectedIndex = 2
                    },
                    icon = { Icon(Icons.Default.SportsSoccer, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

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
                    icon = { Icon(Icons.Default.Person, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Tus tarjetas") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToTarjetas()
                    },
                    icon = { Icon(Icons.Default.CreditCard, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

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
                    icon = { Icon(Icons.Default.Star, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Reportar problema") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToReporte()
                    },
                    icon = { Icon(Icons.Default.BugReport, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Acerca de") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToAcercaDe()
                    },
                    icon = { Icon(Icons.Default.Info, null) },
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
                    icon = { Icon(Icons.Default.ExitToApp, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    ) {
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
                            Icon(Icons.Default.Menu, "Abrir menú")
                        }
                    },
                    actions = {
                        IconButton(onClick = onNavigateToNotificaciones) {
                            BadgedBox(
                                badge = {
                                    if (unreadNotifs > 0) {
                                        Badge { Text(unreadNotifs.toString()) }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (unreadNotifs > 0) Icons.Default.Notifications else Icons.Default.NotificationsNone,
                                    contentDescription = "Notificaciones"
                                )
                            }
                        }
                        IconButton(onClick = onNavigateToAcercaDe) {
                            Icon(Icons.Default.Info, "Acerca De")
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
                    navItems.forEachIndexed { index, item ->
                        NavigationBarItem(
                            selected = selectedIndex == index,
                            onClick = { selectedIndex = index },
                            icon = { Icon(item.icon, item.title) },
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
                    0 -> CanchasTab(onNavigateToCanchaDetail = onNavigateToCanchaDetail)
                    1 -> MisReservasTab(
                        viewModel = reservaViewModel,
                        onOrganizarPartido = { reservaId -> onCrearPartido(reservaId) }
                    )
                    2 -> PartidosTab(
                        viewModel = partidoViewModel,
                        onCrearPartido = { onCrearPartido(null) },
                        onNavigateToProfile = { uid -> onNavigateToProfile(uid) }
                    )
                    3 -> PerfilTab(
                        onSignOut = onSignOut,
                        viewModel = profileViewModel,
                        onEditProfile = { onNavigateToProfile(null) }
                    )
                }
            }
        }
    }
}
