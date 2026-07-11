package com.example.futbol_tnt.presentation.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futbol_tnt.data.model.Partido
import com.example.futbol_tnt.data.repository.IPartidoRepository
import com.example.futbol_tnt.data.repository.PartidoRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class PartidoEvento {
    data object Idle : PartidoEvento()
    data object SolicitudEnviada : PartidoEvento()
    data object SolicitudGestionada : PartidoEvento()
    data object AbandonarExito : PartidoEvento()
    data object PartidoLleno : PartidoEvento()
    data object PartidoCreadoExito : PartidoEvento()
    data object UrgenciaActualizada : PartidoEvento()
    data class Error(val mensaje: String) : PartidoEvento()
}

class PartidoViewModel(
    private val repository: IPartidoRepository = PartidoRepository(),
    private val userRepository: com.example.futbol_tnt.data.repository.IUserRepository = com.example.futbol_tnt.data.repository.UserRepository(),
) : ViewModel() {

    private val _userLocation = MutableStateFlow<Location?>(null)
    val userLocation: StateFlow<Location?> = _userLocation.asStateFlow()

    init {
        // Cada vez que cambia la ubicación, la guardamos en el perfil del usuario en Firestore
        // para habilitar las notificaciones de cercanía desde el backend.
        userLocation
            .filterNotNull()
            .onEach { location ->
                val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                if (uid != null) {
                    val user = userRepository.getUser(uid)
                    user?.let {
                        userRepository.updateUser(it.copy(
                            lastLat = location.latitude,
                            lastLng = location.longitude
                        ))
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    val partidos: StateFlow<List<Partido>> = repository.partidos
        .catch { error ->
            if (error.message?.contains("PERMISSION_DENIED") == false) {
                _evento.value = PartidoEvento.Error(
                    error.message ?: "No se pudieron cargar los partidos",
                )
            }
            emit(emptyList())
        }
        .combine(_userLocation) { list, location ->
            if (location != null) {
                list.sortedBy { partido ->
                    val results = FloatArray(1)
                    Location.distanceBetween(
                        location.latitude, location.longitude,
                        partido.cancha.lat, partido.cancha.lng,
                        results
                    )
                    results[0]
                }
            } else {
                list
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = emptyList(),
        )

    private val _evento = MutableStateFlow<PartidoEvento>(PartidoEvento.Idle)
    val evento: StateFlow<PartidoEvento> = _evento.asStateFlow()

    fun setUserLocation(location: Location?) {
        _userLocation.value = location
    }

    fun enviarSolicitud(partidoId: String) {
        viewModelScope.launch {
            _evento.value = runCatching {
                val exito = repository.enviarSolicitud(partidoId)
                if (exito) PartidoEvento.SolicitudEnviada else PartidoEvento.PartidoLleno
            }.getOrElse { error ->
                PartidoEvento.Error(error.message ?: "Error al enviar solicitud")
            }
        }
    }

    fun gestionarSolicitud(partidoId: String, applicantId: String, aceptar: Boolean) {
        viewModelScope.launch {
            _evento.value = runCatching {
                val exito = repository.gestionarSolicitud(partidoId, applicantId, aceptar)
                if (exito) PartidoEvento.SolicitudGestionada else PartidoEvento.PartidoLleno
            }.getOrElse { error ->
                PartidoEvento.Error(error.message ?: "Error al gestionar solicitud")
            }
        }
    }

    fun abandonarPartido(partidoId: String) {
        viewModelScope.launch {
            _evento.value = runCatching {
                val exito = repository.abandonarPartido(partidoId)
                if (exito) PartidoEvento.AbandonarExito else PartidoEvento.Error("No se pudo abandonar el partido")
            }.getOrElse { error ->
                PartidoEvento.Error(error.message ?: "Error al abandonar el partido")
            }
        }
    }

    fun crearPartido(partido: Partido) {
        viewModelScope.launch {
            _evento.value = runCatching {
                repository.crearPartido(partido)
                PartidoEvento.PartidoCreadoExito
            }.getOrElse { error ->
                PartidoEvento.Error(error.message ?: "Error al crear el partido")
            }
        }
    }

    fun toggleUrgencia(partidoId: String, currentUrgencia: Boolean) {
        viewModelScope.launch {
            val exito = repository.setUrgencia(partidoId, !currentUrgencia)
            if (exito) {
                _evento.value = PartidoEvento.UrgenciaActualizada
            } else {
                _evento.value = PartidoEvento.Error("No se pudo actualizar la urgencia")
            }
        }
    }

    suspend fun getPartidoByReservaId(reservaId: String): Partido? {
        return try {
            repository.getPartidoByReservaId(reservaId)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun getPartidoById(id: String): Partido? {
        return try {
            repository.getPartidoById(id)
        } catch (_: Exception) {
            null
        }
    }

    fun limpiarEvento() {
        _evento.value = PartidoEvento.Idle
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
