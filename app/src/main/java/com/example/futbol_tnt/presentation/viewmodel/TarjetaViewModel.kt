package com.example.futbol_tnt.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futbol_tnt.data.model.Tarjeta
import com.example.futbol_tnt.data.repository.TarjetaRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class TarjetaUiState {
    data object Loading : TarjetaUiState()
    data class Success(val tarjetas: List<Tarjeta>) : TarjetaUiState()
    data class Error(val message: String) : TarjetaUiState()
}

class TarjetaViewModel(
    private val repository: TarjetaRepository = TarjetaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<TarjetaUiState>(TarjetaUiState.Loading)
    val uiState: StateFlow<TarjetaUiState> = _uiState.asStateFlow()

    private val auth = FirebaseAuth.getInstance()

    fun loadTarjetas() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _uiState.value = TarjetaUiState.Loading
            try {
                val tarjetas = repository.getTarjetas(userId)
                _uiState.value = TarjetaUiState.Success(tarjetas)
            } catch (e: Exception) {
                _uiState.value = TarjetaUiState.Error(e.message ?: "Error al cargar tarjetas")
            }
        }
    }

    fun agregarTarjeta(tarjeta: Tarjeta) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                repository.agregarTarjeta(userId, tarjeta)
                loadTarjetas()
            } catch (e: Exception) {
                _uiState.value = TarjetaUiState.Error(e.message ?: "Error al agregar tarjeta")
            }
        }
    }

    fun eliminarTarjeta(tarjetaId: String) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                repository.eliminarTarjeta(userId, tarjetaId)
                loadTarjetas()
            } catch (e: Exception) {
                _uiState.value = TarjetaUiState.Error(e.message ?: "Error al eliminar tarjeta")
            }
        }
    }
}
