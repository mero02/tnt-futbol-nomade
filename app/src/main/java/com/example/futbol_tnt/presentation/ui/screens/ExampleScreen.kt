package com.example.futbol_tnt.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.futbol_tnt.presentation.ui.theme.FutbolTNTTheme

/**
 * Pantalla de ejemplo que demuestra la estructura recomendada para Composables.
 *
 * Principios aplicados:
 * 1. Separación de la lógica de estado (ViewModel) de la UI (Composable)
 * 2. State Hoisting: el estado se maneja en un nivel superior
 * 3. Composables sin estado y reutilizables
 * 4. Preview para cada composable
 */

// === Estado de UI ===
/**
 * Representa el estado de la pantalla de ejemplo.
 * Usar data class inmutables para el estado de UI.
 */
data class ExampleUiState(
    val title: String = "Bienvenido a FutbolTNT",
    val counter: Int = 0,
    val isLoading: Boolean = false
)

// === Acciones de Usuario ===
/**
 * Sealed class que representa todas las acciones posibles del usuario.
 * Esto hace el flujo de eventos explícito y type-safe.
 */
sealed interface ExampleAction {
    data object IncrementCounter : ExampleAction
    data object DecrementCounter : ExampleAction
    data object Reset : ExampleAction
}

// === Composable Principal ===
/**
 * Punto de entrada de la pantalla.
 * En una app real, aquí se inyectaría el ViewModel.
 */
@Composable
fun ExampleScreen(
    modifier: Modifier = Modifier
    // viewModel: ExampleViewModel = hiltViewModel()
) {
    // En una app real, el estado vendría del ViewModel:
    // val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Para este ejemplo, manejamos el estado localmente
    var uiState by remember { mutableStateOf(ExampleUiState()) }

    ExampleScreenContent(
        uiState = uiState,
        onAction = { action ->
            // En una app real: viewModel.onAction(action)
            uiState = when (action) {
                ExampleAction.IncrementCounter -> uiState.copy(counter = uiState.counter + 1)
                ExampleAction.DecrementCounter -> uiState.copy(counter = uiState.counter - 1)
                ExampleAction.Reset -> uiState.copy(counter = 0)
            }
        },
        modifier = modifier
    )
}

// === Composable de Contenido (Sin Estado) ===
/**
 * Composable sin estado que solo renderiza UI basado en los parámetros.
 * Esto facilita el testing y la reutilización.
 */
@Composable
private fun ExampleScreenContent(
    uiState: ExampleUiState,
    onAction: (ExampleAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.title) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                CounterDisplay(count = uiState.counter)

                Spacer(modifier = Modifier.height(24.dp))

                CounterControls(
                    onIncrement = { onAction(ExampleAction.IncrementCounter) },
                    onDecrement = { onAction(ExampleAction.DecrementCounter) },
                    onReset = { onAction(ExampleAction.Reset) }
                )
            }
        }
    }
}

// === Componentes Reutilizables ===
/**
 * Componente que muestra el contador.
 * Separar en componentes pequeños mejora la legibilidad y reutilización.
 */
@Composable
private fun CounterDisplay(
    count: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Contador",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Controles del contador agrupados en un componente.
 */
@Composable
private fun CounterControls(
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(onClick = onDecrement) {
            Text("-")
        }
        Button(onClick = onReset) {
            Text("Reset")
        }
        Button(onClick = onIncrement) {
            Text("+")
        }
    }
}

// === Previews ===
/**
 * Preview del estado normal.
 * Crear múltiples previews para diferentes estados ayuda durante el desarrollo.
 */
@Preview(showBackground = true)
@Composable
private fun ExampleScreenPreview() {
    FutbolTNTTheme {
        ExampleScreenContent(
            uiState = ExampleUiState(counter = 5),
            onAction = {}
        )
    }
}

/**
 * Preview del estado de carga.
 */
@Preview(showBackground = true)
@Composable
private fun ExampleScreenLoadingPreview() {
    FutbolTNTTheme {
        ExampleScreenContent(
            uiState = ExampleUiState(isLoading = true),
            onAction = {}
        )
    }
}

/**
 * Preview en dark mode.
 */
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ExampleScreenDarkPreview() {
    FutbolTNTTheme {
        ExampleScreenContent(
            uiState = ExampleUiState(counter = 10),
            onAction = {}
        )
    }
}
