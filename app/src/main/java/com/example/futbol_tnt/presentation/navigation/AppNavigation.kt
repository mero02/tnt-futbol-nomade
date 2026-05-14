package com.example.futbol_tnt.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
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
import com.example.futbol_tnt.presentation.viewmodel.PartidoViewModel
import com.example.futbol_tnt.presentation.viewmodel.ProfileViewModel
import com.example.futbol_tnt.presentation.viewmodel.ReservaViewModel

// Cada pantalla tiene una ruta string única. El NavController usa estas rutas para navegar.
sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Home : Screen("home")
    data object AcercaDe : Screen("acerca_de")
    data object Perfil : Screen("perfil")
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

// AppNavigation es el punto de entrada de toda la navegación de la app.
// Crea el NavController (el "router"), los ViewModels compartidos, y define qué
// pantalla renderizar para cada ruta.
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // GoogleAuthClient y los ViewModels se crean una sola vez con remember{}
    // para que sobrevivan recomposiciones sin perder su estado.
    val googleAuthClient = remember { GoogleAuthClient(context) }
    val userRepository = remember { UserRepository() }
    val authViewModel = remember { AuthViewModel(googleAuthClient, userRepository) }
    val partidoViewModel = remember { PartidoViewModel() }
    val reservaRepository = remember { ReservaRepository() }
    val canchaViewModel = remember { CanchaViewModel(reservaRepository) }
    val reservaViewModel = remember { ReservaViewModel(reservaRepository) }
    val profileViewModel = remember { ProfileViewModel(userRepository) }
    val checkoutViewModel = remember { com.example.futbol_tnt.presentation.viewmodel.CheckoutViewModel(reservaRepository) }

    // Si el usuario ya tiene sesión activa en Firebase, arranca en Home directamente.
    // Así no vuelve a ver el login al reabrir la app.
    val startDestination = if (googleAuthClient.isLoggedIn()) {
        // Si ya está logueado, sincronizamos sus datos en segundo plano por si es la primera vez
        // que entra con la persistencia nueva o si cambiaron sus datos de Google.
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
        // Pantalla de login: cuando el login es exitoso, navega a Home
        // y elimina Login del back stack (popUpTo inclusive) para que
        // el botón "atrás" del celular no vuelva al login.
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

        // Pantalla principal con tabs. Recibe lambdas de navegación en lugar de
        // tener acceso directo al navController — esto mantiene HomeScreen
        // desacoplada del sistema de navegación (más fácil de testear y reutilizar).
        composable(Screen.Home.route) {
            HomeScreen(
                // Al cerrar sesión: limpia Firebase + Google, resetea el estado del ViewModel
                // y vuelve al login eliminando Home del back stack.
                onSignOut = {
                    googleAuthClient.signOut()
                    authViewModel.resetState()
                    profileViewModel.clearProfile()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToAcercaDe = {
                    navController.navigate(Screen.AcercaDe.route)
                },
                onCrearPartido = { reservaId ->
                    navController.navigate(Screen.CrearPartido.createRoute(reservaId))
                },
                onNavigateToCanchaDetail = { canchaId ->
                    navController.navigate(Screen.CanchaDetail.createRoute(canchaId))
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Perfil.route)
                },
                // PartidoViewModel se pasa desde acá para que PartidosTab y
                // CrearPartidoScreen compartan el mismo estado de partidos.
                partidoViewModel = partidoViewModel,
                reservaViewModel = reservaViewModel,
                profileViewModel = profileViewModel
            )
        }
        composable(Screen.AcercaDe.route) {
            AcercaDe {
                // popBackStack() vuelve a la pantalla anterior (Home)
                navController.popBackStack()
            }
        }
        composable(Screen.Perfil.route) {
            val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return@composable
            com.example.futbol_tnt.presentation.ui.screens.profile.ProfileScreen(
                uid = uid,
                viewModel = profileViewModel,
            ) {
                navController.popBackStack()
            }
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
            ) {
                navController.popBackStack()
            }
        }
    }
}
