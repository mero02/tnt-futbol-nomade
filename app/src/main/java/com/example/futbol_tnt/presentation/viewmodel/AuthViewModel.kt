package com.example.futbol_tnt.presentation.viewmodel

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futbol_tnt.data.auth.AuthResult
import com.example.futbol_tnt.data.auth.GoogleAuthClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    data object Loading : AuthUiState()
    data object Idle : AuthUiState()
    data class Success(val userId: String, val displayName: String?) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(
    private val googleAuthClient: GoogleAuthClient
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _pendingSignInIntent = MutableStateFlow<Intent?>(null)
    val pendingSignInIntent: StateFlow<Intent?> = _pendingSignInIntent.asStateFlow()

    fun onGoogleSignInClick() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val result = googleAuthClient.signIn()) {
                is AuthResult.Success -> {
                    _uiState.value = AuthUiState.Success(
                        userId = result.userId,
                        displayName = result.displayName
                    )
                }
                is AuthResult.Error -> {
                    _uiState.value = AuthUiState.Error(result.message)
                }
                is AuthResult.Pending -> {
                    _pendingSignInIntent.value = result.signInIntent
                    _uiState.value = AuthUiState.Idle
                }
            }
        }
    }

    fun clearPendingIntent() {
        _pendingSignInIntent.value = null
    }

    fun onSignInResult(intent: Intent?) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val result = googleAuthClient.handleSignInResult(intent)) {
                is AuthResult.Success -> {
                    _uiState.value = AuthUiState.Success(
                        userId = result.userId,
                        displayName = result.displayName
                    )
                }
                is AuthResult.Error -> {
                    _uiState.value = AuthUiState.Error(result.message)
                }
                is AuthResult.Pending -> {
                    // No debería happen en handleSignInResult
                    _uiState.value = AuthUiState.Error("Unexpected state")
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = AuthUiState.Idle
    }
}