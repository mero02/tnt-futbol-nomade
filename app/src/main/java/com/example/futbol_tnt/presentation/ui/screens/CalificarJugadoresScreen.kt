package com.example.futbol_tnt.presentation.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.futbol_tnt.data.model.Partido
import com.example.futbol_tnt.data.model.User
import com.example.futbol_tnt.data.repository.UserRepository
import com.example.futbol_tnt.presentation.viewmodel.CalificacionViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalificarJugadoresScreen(
    partido: Partido,
    viewModel: CalificacionViewModel,
    onBack: () -> Unit
) {
    val currentUid = remember { FirebaseAuth.getInstance().currentUser?.uid }
    val participantesIds = remember { partido.participantesIds.filter { it != currentUid } }
    var yaCalificados by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(partido.id) {
        yaCalificados = viewModel.getJugadoresYaCalificados(partido.id)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calificar Jugadores") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (participantesIds.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No hay otros jugadores para calificar.", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(participantesIds) { uid ->
                    JugadorRatingItem(
                        uid = uid,
                        partidoId = partido.id,
                        yaCalificado = yaCalificados.contains(uid),
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun JugadorRatingItem(
    uid: String,
    partidoId: String,
    yaCalificado: Boolean,
    viewModel: CalificacionViewModel
) {
    val userRepository = remember { UserRepository() }
    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var rating by remember { mutableIntStateOf(0) }
    var comentario by remember { mutableStateOf("") }
    var isRatedLocal by remember { mutableStateOf(yaCalificado) }
    var showCommentField by remember { mutableStateOf(false) }

    LaunchedEffect(uid) {
        user = userRepository.getUser(uid)
        isLoading = false
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(40.dp))
                } else {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(user?.photoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.size(50.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = user?.apodo ?: user?.displayName ?: "Jugador", fontWeight = FontWeight.Bold)

                        if (isRatedLocal) {
                            Text("¡Ya calificado!", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                        } else {
                            StarRatingBar(
                                rating = rating,
                                onRatingChanged = {
                                    rating = it
                                    showCommentField = true
                                }
                            )
                        }
                    }

                    if (!isRatedLocal && rating > 0) {
                        IconButton(onClick = {
                            viewModel.calificarJugador(partidoId, uid, rating, comentario.ifBlank { null })
                            isRatedLocal = true
                        }) {
                            Icon(Icons.Default.Send, contentDescription = "Enviar", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            if (!isRatedLocal && showCommentField) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = comentario,
                    onValueChange = { comentario = it },
                    label = { Text("Comentario (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodySmall,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
fun StarRatingBar(
    rating: Int,
    onRatingChanged: (Int) -> Unit
) {
    Row {
        for (i in 1..5) {
            Icon(
                imageVector = if (i <= rating) Icons.Default.Star else Icons.Default.StarOutline,
                contentDescription = null,
                tint = if (i <= rating) Color(0xFFFFB300) else MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onRatingChanged(i) }
            )
        }
    }
}
