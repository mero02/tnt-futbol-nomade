package com.example.futbol_tnt.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futbol_tnt.data.model.Notificacion
import com.example.futbol_tnt.data.repository.NotificacionRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NotificacionViewModel(
    private val repository: NotificacionRepository = NotificacionRepository()
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    val notificaciones: StateFlow<List<Notificacion>> = auth.currentUser?.uid?.let { uid ->
        repository.getNotificaciones(uid)
            .catch { emit(emptyList()) } // Evita que la app se cierre si falla la consulta
            .map { list -> list.sortedByDescending { it.fecha } } // Ordenamos en memoria para no requerir índice
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    } ?: MutableStateFlow(emptyList())

    val unreadCount: StateFlow<Int> = notificaciones.map { list ->
        list.count { !it.leido }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun marcarComoLeida(notifId: String) {
        viewModelScope.launch {
            repository.marcarComoLeida(notifId)
        }
    }
}
