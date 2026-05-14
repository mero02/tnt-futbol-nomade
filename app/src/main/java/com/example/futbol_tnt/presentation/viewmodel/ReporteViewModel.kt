package com.example.futbol_tnt.presentation.viewmodel

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futbol_tnt.data.model.Reporte
import com.example.futbol_tnt.data.model.TipoReporte
import com.example.futbol_tnt.data.repository.ReporteRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ReporteUiState {
    data object Idle : ReporteUiState()
    data object Loading : ReporteUiState()
    data object Success : ReporteUiState()
    data class Error(val message: String) : ReporteUiState()
}

class ReporteViewModel(
    private val repository: ReporteRepository = ReporteRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReporteUiState>(ReporteUiState.Idle)
    val uiState: StateFlow<ReporteUiState> = _uiState.asStateFlow()

    fun enviarReporte(tipo: TipoReporte, descripcion: String) {
        if (descripcion.isBlank()) {
            _uiState.value = ReporteUiState.Error("La descripción no puede estar vacía")
            return
        }

        viewModelScope.launch {
            _uiState.value = ReporteUiState.Loading
            try {
                val user = FirebaseAuth.getInstance().currentUser
                val reporte = Reporte(
                    userId = user?.uid ?: "Anónimo",
                    userEmail = user?.email ?: "Sin email",
                    tipo = tipo,
                    descripcion = descripcion,
                    dispositivo = "${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE})"
                )
                repository.enviarReporte(reporte)
                _uiState.value = ReporteUiState.Success
            } catch (e: Exception) {
                _uiState.value = ReporteUiState.Error(e.message ?: "Error al enviar el reporte")
            }
        }
    }

    fun resetState() {
        _uiState.value = ReporteUiState.Idle
    }
}
