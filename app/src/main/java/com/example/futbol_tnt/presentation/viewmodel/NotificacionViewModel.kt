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

    private val _currentUserUid = MutableStateFlow(FirebaseAuth.getInstance().currentUser?.uid)

    init {
        // Escuchamos cambios de auth para actualizar el UID
        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            _currentUserUid.value = auth.currentUser?.uid
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val notificaciones: StateFlow<List<Notificacion>> = _currentUserUid
        .flatMapLatest { uid ->
            if (uid == null) flowOf(emptyList())
            else repository.getNotificaciones(uid)
                .catch { emit(emptyList()) }
                .map { list -> list.sortedByDescending { it.fecha } }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadCount: StateFlow<Int> = notificaciones.map { list ->
        list.count { !it.leido }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun marcarComoLeida(notifId: String) {
        viewModelScope.launch {
            repository.marcarComoLeida(notifId)
        }
    }
}
