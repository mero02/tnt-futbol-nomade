package com.example.futbol_tnt.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futbol_tnt.data.model.Calificacion
import com.example.futbol_tnt.data.repository.CalificacionRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CalificacionUiState {
    data object Idle : CalificacionUiState()
    data object Loading : CalificacionUiState()
    data object Success : CalificacionUiState()
    data class Error(val message: String) : CalificacionUiState()
}

class CalificacionViewModel(
    private val repository: CalificacionRepository = CalificacionRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<CalificacionUiState>(CalificacionUiState.Idle)
    val uiState: StateFlow<CalificacionUiState> = _uiState.asStateFlow()

    private val auth = FirebaseAuth.getInstance()

    fun calificarJugador(partidoId: String, calificadoId: String, estrellas: Int, comentario: String?) {
        val currentUid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _uiState.value = CalificacionUiState.Loading
            try {
                val calificacion = Calificacion(
                    partidoId = partidoId,
                    calificadorId = currentUid,
                    calificadoId = calificadoId,
                    estrellas = estrellas,
                    comentario = comentario
                )
                repository.calificarJugador(calificacion)
                _uiState.value = CalificacionUiState.Success
            } catch (e: Exception) {
                _uiState.value = CalificacionUiState.Error(e.message ?: "Error al calificar")
            }
        }
    }

    suspend fun getJugadoresYaCalificados(partidoId: String): List<String> {
        val currentUid = auth.currentUser?.uid ?: return emptyList()
        return try {
            repository.getCalificacionesPorPartido(partidoId, currentUid)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun resetState() {
        _uiState.value = CalificacionUiState.Idle
    }
}
