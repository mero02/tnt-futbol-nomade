package com.example.futbol_tnt.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futbol_tnt.data.model.Reserva
import com.example.futbol_tnt.data.repository.IReservaRepository
import com.example.futbol_tnt.data.repository.ReservaRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ReservaViewModel(
    repository: IReservaRepository = ReservaRepository(),
) : ViewModel() {

    val reservas: StateFlow<List<Reserva>> = repository.reservas
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
