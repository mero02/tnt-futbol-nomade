package com.example.futbol_tnt.presentation.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.futbol_tnt.R
import com.example.futbol_tnt.presentation.viewmodel.AuthUiState
import com.example.futbol_tnt.presentation.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Observa el estado del ViewModel como State para que Compose recomponga
    // automáticamente cuando cambie (Loading, Success, Error, Idle).
    val uiState by authViewModel.uiState.collectAsState()

    // pendingSignInIntent es el Intent de Google que hay que lanzar como Activity.
    // El ViewModel lo emite cuando Google responde con el selector de cuentas.
    val pendingIntent by authViewModel.pendingSignInIntent.collectAsState()
    val context = LocalContext.current

    // Launcher para la Activity de Google Sign-In.
    // rememberLauncherForActivityResult registra el callback que recibe el resultado
    // cuando el usuario elige (o cancela) su cuenta de Google.
    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // El usuario eligió una cuenta → pasamos el Intent al ViewModel para
            // que extraiga el token y lo valide contra Firebase.
            authViewModel.onSignInResult(result.data)
        } else {
            // El usuario cerró la pantalla sin elegir cuenta → limpiamos el error.
            authViewModel.clearError()
        }
    }

    // Cuando el estado llega a Success, llamamos onLoginSuccess() para navegar a Home.
    // LaunchedEffect evita ejecutarlo en cada recomposición; solo corre cuando uiState cambia.
    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onLoginSuccess()
        }
    }

    // Cuando el ViewModel emite el Intent de Google, lo lanzamos con el launcher
    // y luego lo limpiamos para que no se vuelva a lanzar en una recomposición.
    LaunchedEffect(pendingIntent) {
        pendingIntent?.let { intent ->
            signInLauncher.launch(intent)
            authViewModel.clearPendingIntent()
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.SportsSoccer,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Entra a la cancha",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Jugá donde quieras,\ncuando quieras",
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(48.dp))

            // La UI cambia según el estado actual del ViewModel:
            // - Loading: muestra spinner mientras Firebase procesa el token
            // - Error: muestra el mensaje y vuelve a mostrar el botón para reintentar
            // - Idle/otros: muestra el botón normalmente
            when (val state = uiState) {
                is AuthUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is AuthUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    GoogleSignInButton(
                        onClick = { authViewModel.onGoogleSignInClick() }
                    )
                }
                else -> {
                    GoogleSignInButton(
                        onClick = { authViewModel.onGoogleSignInClick() }
                    )
                }
            }
        }
    }
}

@Composable
private fun GoogleSignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Text(
            text = "Continuar con Google",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
