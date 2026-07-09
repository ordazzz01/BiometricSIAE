package com.siae.biometricsiae.feature.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siae.biometricsiae.data.repository.IncidentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val incidentRepository: IncidentRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AdminState())
    val state: StateFlow<AdminState> = _state.asStateFlow()

    init {
        loadIncidents()
    }

    private fun loadIncidents() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            incidentRepository.getAllIncidents().collect { incidents ->
                _state.value = _state.value.copy(
                    incidents = incidents,
                    isLoading = false
                )
            }
        }
    }

    fun selectIncident(incident: IncidentEntity?) {
        _state.value = _state.value.copy(selectedIncident = incident)
    }
}
