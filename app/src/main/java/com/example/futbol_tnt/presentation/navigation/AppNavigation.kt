package com.example.futbol_tnt.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.futbol_tnt.data.auth.GoogleAuthClient
import com.example.futbol_tnt.data.repository.ReservaRepository
import com.example.futbol_tnt.data.repository.UserRepository
import com.example.futbol_tnt.presentation.ui.screens.AcercaDe
import com.example.futbol_tnt.presentation.ui.screens.CanchaDetailScreen
import com.example.futbol_tnt.presentation.ui.screens.CrearPartidoScreen
import com.example.futbol_tnt.presentation.ui.screens.home.HomeScreen
import com.example.futbol_tnt.presentation.ui.screens.LoginScreen
import com.example.futbol_tnt.presentation.viewmodel.AuthViewModel
import com.example.futbol_tnt.presentation.viewmodel.CanchaViewModel
import com.example.futbol_tnt.presentation.viewmodel.CheckoutViewModel
import com.example.futbol_tnt.presentation.viewmodel.PartidoViewModel
import com.example.futbol_tnt.presentation.viewmodel.ProfileViewModel
import com.example.futbol_tnt.presentation.viewmodel.ReporteViewModel
import com.example.futbol_tnt.presentation.viewmodel.ReservaViewModel
import com.example.futbol_tnt.presentation.viewmodel.TarjetaViewModel

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Home : Screen("home")
    data object AcercaDe : Screen("acerca_de")
    data object Perfil : Screen("perfil?uid={uid}") {
        fun createRoute(uid: String? = null) = if (uid != null) "perfil?uid=$uid" else "perfil"
    }
    data object Reporte : Screen("reporte")
    data object Tarjetas : Screen("tarjetas")
    data object Notificaciones : Screen("notificaciones")
    data object CalificarJugadores : Screen("calificar_jugadores/{partidoId}") {
        fun createRoute(partidoId: String) = "calificar_jugadores/$partidoId"
    }
    data object CrearPartido : Screen("crear_partido?reservaId={reservaId}") {
        fun createRoute(reservaId: String? = null) =
            if (reservaId != null) "crear_partido?reservaId=$reservaId" else "crear_partido"
    }
    data object CanchaDetail : Screen("cancha_detail/{canchaId}") {
        fun createRoute(canchaId: String) = "cancha_detail/$canchaId"
    }
    data object Checkout : Screen("checkout/{canchaId}/{fechaHora}/{duracion}") {
        fun createRoute(canchaId: String, fechaHora: String, duracion: Double) =
            "checkout/$canchaId/$fechaHora/$duracion"
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    val googleAuthClient = remember { GoogleAuthClient(context) }
    val userRepository = remember { UserRepository() }
    val reservaRepository = remember { ReservaRepository() }

    val authViewModel: AuthViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                AuthViewModel(googleAuthClient, userRepository) as T
        }
    )
    val partidoViewModel: PartidoViewModel = viewModel()
    val canchaViewModel: CanchaViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                CanchaViewModel(reservaRepository) as T
        }
    )
    val reservaViewModel: ReservaViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ReservaViewModel(reservaRepository) as T
        }
    )
    val profileViewModel: ProfileViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ProfileViewModel(userRepository) as T
        }
    )
    val checkoutViewModel: CheckoutViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                CheckoutViewModel(reservaRepository) as T
        }
    )
    val reporteViewModel: ReporteViewModel = viewModel()
    val tarjetaViewModel: TarjetaViewModel = viewModel()
    val notificacionViewModel: com.example.futbol_tnt.presentation.viewmodel.NotificacionViewModel = viewModel()
    val calificacionViewModel: com.example.futbol_tnt.presentation.viewmodel.CalificacionViewModel = viewModel()

    val startDestination = if (googleAuthClient.isLoggedIn()) {
        LaunchedEffect(Unit) {
            val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            currentUser?.let { firebaseUser ->
                val user = com.example.futbol_tnt.data.model.User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email,
                    displayName = firebaseUser.displayName,
                    photoUrl = firebaseUser.photoUrl?.toString(),
                )
                try {
                    userRepository.syncUser(user)
                } catch (e: Exception) {
                    println("Error sincronizando usuario existente: ${e.message}")
                }
            }
        }
        Screen.Home.route
    } else {
        Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onSignOut = {
                    googleAuthClient.signOut()
                    authViewModel.resetState()
                    profileViewModel.clearProfile()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToAcercaDe = { navController.navigate(Screen.AcercaDe.route) },
                onNavigateToReporte = { navController.navigate(Screen.Reporte.route) },
                onNavigateToTarjetas = { navController.navigate(Screen.Tarjetas.route) },
                onNavigateToNotificaciones = { navController.navigate(Screen.Notificaciones.route) },
                onNavigateToCalificar = { partidoId -> navController.navigate(Screen.CalificarJugadores.createRoute(partidoId)) },
                onCrearPartido = { reservaId -> navController.navigate(Screen.CrearPartido.createRoute(reservaId)) },
                onNavigateToCanchaDetail = { canchaId -> navController.navigate(Screen.CanchaDetail.createRoute(canchaId)) },
                onNavigateToProfile = { uid -> navController.navigate(Screen.Perfil.createRoute(uid)) },
                partidoViewModel = partidoViewModel,
                reservaViewModel = reservaViewModel,
                profileViewModel = profileViewModel,
                notificacionViewModel = notificacionViewModel
            )
        }
        composable(Screen.AcercaDe.route) {
            AcercaDe { navController.popBackStack() }
        }
        composable(Screen.Perfil.route) { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("uid")
                ?: com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                ?: return@composable
            com.example.futbol_tnt.presentation.ui.screens.profile.ProfileScreen(
                uid = uid,
                viewModel = profileViewModel,
            ) { navController.popBackStack() }
        }
        composable(Screen.CanchaDetail.route) { backStackEntry ->
            val canchaId = backStackEntry.arguments?.getString("canchaId") ?: return@composable
            CanchaDetailScreen(
                canchaId = canchaId,
                viewModel = canchaViewModel,
                onBack = { navController.popBackStack() },
                onReservaSuccess = { reservaId ->
                    navController.navigate(Screen.CrearPartido.createRoute(reservaId)) {
                        popUpTo(Screen.CanchaDetail.route) { inclusive = true }
                    }
                },
                onNavigateToCheckout = { cId, f, h, d ->
                    val combined = java.time.LocalDateTime.of(f, h).toString()
                    navController.navigate(Screen.Checkout.createRoute(cId, combined, d))
                }
            )
        }
        composable(Screen.Checkout.route) { backStackEntry ->
            val canchaId = backStackEntry.arguments?.getString("canchaId") ?: ""
            val fechaHora = backStackEntry.arguments?.getString("fechaHora") ?: ""
            val duracion = backStackEntry.arguments?.getString("duracion")?.toDoubleOrNull() ?: 1.0

            com.example.futbol_tnt.presentation.ui.screens.CheckoutScreen(
                canchaId = canchaId,
                fechaHoraStr = fechaHora,
                duracion = duracion,
                viewModel = checkoutViewModel,
                onBack = { navController.popBackStack() },
                onSuccess = { reservaId ->
                    navController.navigate(Screen.CrearPartido.createRoute(reservaId)) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                }
            )
        }
        composable(Screen.CrearPartido.route) { backStackEntry ->
            val reservaId = backStackEntry.arguments?.getString("reservaId")
            CrearPartidoScreen(
                viewModel = partidoViewModel,
                reservaId = reservaId,
                reservaRepository = reservaRepository,
            ) { navController.popBackStack() }
        }
        composable(Screen.Reporte.route) {
            com.example.futbol_tnt.presentation.ui.screens.ReporteScreen(
                viewModel = reporteViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Tarjetas.route) {
            com.example.futbol_tnt.presentation.ui.screens.profile.MisTarjetasScreen(
                viewModel = tarjetaViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Notificaciones.route) {
            com.example.futbol_tnt.presentation.ui.screens.NotificacionesScreen(
                viewModel = notificacionViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.CalificarJugadores.route) { backStackEntry ->
            val partidoId = backStackEntry.arguments?.getString("partidoId") ?: return@composable
            val partido = partidoViewModel.partidos.value.find { it.id == partidoId } ?: return@composable
            com.example.futbol_tnt.presentation.ui.screens.CalificarJugadoresScreen(
                partido = partido,
                viewModel = calificacionViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
