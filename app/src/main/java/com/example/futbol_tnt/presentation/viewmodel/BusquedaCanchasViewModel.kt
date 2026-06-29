package com.example.futbol_tnt.presentation.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futbol_tnt.data.model.Cancha
import com.example.futbol_tnt.data.repository.CanchaRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BusquedaCanchasViewModel(
    private val repository: CanchaRepository = CanchaRepository()
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _userLocation = MutableStateFlow<Location?>(null)
    val userLocation: StateFlow<Location?> = _userLocation.asStateFlow()

    private val _canchas = MutableStateFlow<List<Cancha>>(emptyList())

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val canchasFiltradas: StateFlow<List<Cancha>> = combine(_query, _canchas, _userLocation) { query, canchas, location ->
        val filtered = if (query.isBlank()) {
            canchas
        } else {
            canchas.filter {
                it.ciudad.contains(query, ignoreCase = true) ||
                it.nombre.contains(query, ignoreCase = true)
            }
        }

        if (location != null) {
            filtered.sortedBy { cancha ->
                val results = FloatArray(1)
                Location.distanceBetween(location.latitude, location.longitude, cancha.lat, cancha.lng, results)
                results[0]
            }
        } else {
            filtered
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            try {
                _error.value = null
                val result = repository.getCanchas()
                if (result.isEmpty()) {
                    _error.value = "No se encontraron canchas en la base de datos."
                }
                _canchas.value = result
            } catch (e: Exception) {
                _error.value = "Error de conexión: ${e.message}"
            }
        }
    }

    fun setUserLocation(location: Location?) {
        _userLocation.value = location
    }

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }
}
