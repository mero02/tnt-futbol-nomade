package com.example.futbol_tnt.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.futbol_tnt.R

/**
 * Pantalla "Acerca De" que muestra información sobre la app y el equipo.
 * Usa LazyColumn para contenido scrollable y estilos consistentes con el resto de la app.
 */
@Composable
fun AcercaDe(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {} // Para navegación de vuelta
) {
    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text(stringResource(R.string.acerca_de_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", style = MaterialTheme.typography.titleLarge)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Descripción de la app
            item {
                Text(
                    text = stringResource(R.string.acerca_de_descripcion),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Justify
                )
            }

            // Misión
            item {
                SectionCard(
                    title = "Misión",
                    content = stringResource(R.string.acerca_de_mision)
                )
            }

            // Visión
            item {
                SectionCard(
                    title = "Visión",
                    content = stringResource(R.string.acerca_de_vision)
                )
            }

            // Valores
            item {
                SectionCard(
                    title = "Valores",
                    content = stringResource(R.string.acerca_de_valores)
                )
            }

            // Equipo
            item {
                SectionCard(
                    title = stringResource(R.string.acerca_de_equipo),
                    content = stringResource(R.string.acerca_de_equipo_detalle)
                )
            }

            // Versión
            item {
                Text(
                    text = stringResource(R.string.acerca_de_version),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Contacto
            item {
                Text(
                    text = stringResource(R.string.acerca_de_contacto),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Componente reutilizable para secciones en cards, siguiendo el estilo de HomeScreen.kt.
 */
@Composable
private fun SectionCard(
    title: String,
    content: String,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Justify
            )
        }
    }
}

// === Previews ===
@Preview(showBackground = true)
@Composable
private fun AcercaDePreview() {
    MaterialTheme {
        AcercaDe()
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AcercaDeDarkPreview() {
    MaterialTheme {
        AcercaDe()
    }
}