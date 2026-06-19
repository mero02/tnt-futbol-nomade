package com.example.futbol_tnt.presentation.viewmodel

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

    private val _canchas = MutableStateFlow<List<Cancha>>(emptyList())

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val canchasFiltradas: StateFlow<List<Cancha>> = combine(_query, _canchas) { query, canchas ->
        if (query.isBlank()) {
            canchas
        } else {
            canchas.filter {
                it.ciudad.contains(query, ignoreCase = true) ||
                it.nombre.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            try {
                _error.value = null
                repository.seedCanchas()
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

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }
}
