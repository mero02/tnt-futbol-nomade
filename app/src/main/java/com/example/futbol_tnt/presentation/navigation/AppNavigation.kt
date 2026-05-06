package com.example.futbol_tnt.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.futbol_tnt.data.auth.GoogleAuthClient
import com.example.futbol_tnt.presentation.ui.screens.AcercaDe
import com.example.futbol_tnt.presentation.ui.screens.CrearPartidoScreen
import com.example.futbol_tnt.presentation.ui.screens.home.HomeScreen
import com.example.futbol_tnt.presentation.ui.screens.LoginScreen
import com.example.futbol_tnt.presentation.viewmodel.AuthViewModel
import com.example.futbol_tnt.presentation.viewmodel.PartidoViewModel

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Home : Screen("home")
    data object AcercaDe : Screen("acerca_de")
    data object CrearPartido : Screen("crear_partido")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val googleAuthClient = remember { GoogleAuthClient(context) }
    val authViewModel = remember { AuthViewModel(googleAuthClient) }
    val partidoViewModel = remember { PartidoViewModel() }

    val startDestination = if (googleAuthClient.isLoggedIn()) {
        Screen.Home.route
    } else {
        Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                onSignOut = {
                    googleAuthClient.signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToAcercaDe = {
                    navController.navigate(Screen.AcercaDe.route)
                },
                onCrearPartido = {
                    navController.navigate(Screen.CrearPartido.route)
                },
                partidoViewModel = partidoViewModel
            )
        }
        composable(Screen.AcercaDe.route) {
            AcercaDe(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.CrearPartido.route) {
            CrearPartidoScreen(
                viewModel = partidoViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
