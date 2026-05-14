package com.example.futbol_tnt.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futbol_tnt.data.model.NivelJuego
import com.example.futbol_tnt.data.model.Posicion
import com.example.futbol_tnt.data.model.User
import com.example.futbol_tnt.data.repository.IUserRepository
import com.example.futbol_tnt.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ProfileUiState {
    data object Loading : ProfileUiState()
    data class Success(val user: User) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

class ProfileViewModel(
    private val userRepository: IUserRepository = UserRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadProfile(uid: String) {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            try {
                val user = userRepository.getUser(uid)
                if (user != null) {
                    _uiState.value = ProfileUiState.Success(user)
                } else {
                    _uiState.value = ProfileUiState.Error("Usuario no encontrado")
                }
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.message ?: "Error al cargar perfil")
            }
        }
    }

    fun updateProfile(
        displayName: String,
        posicion: Posicion?,
        nivel: NivelJuego?,
        biografia: String?
    ) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is ProfileUiState.Success) {
                val updatedUser = currentState.user.copy(
                    displayName = displayName,
                    posicion = posicion,
                    nivel = nivel,
                    biografia = biografia
                )
                try {
                    userRepository.updateUser(updatedUser)
                    _uiState.value = ProfileUiState.Success(updatedUser)
                    // Emitimos un evento de éxito si fuera necesario,
                    // o simplemente confiamos en que el estado Success se mantiene.
                } catch (e: Exception) {
                    _uiState.value = ProfileUiState.Error(e.message ?: "Error al actualizar perfil")
                }
            }
        }
    }

    fun clearProfile() {
        _uiState.value = ProfileUiState.Loading
    }
}
