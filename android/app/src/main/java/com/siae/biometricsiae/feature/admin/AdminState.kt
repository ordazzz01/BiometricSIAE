package com.siae.biometricsiae.feature.admin

import com.siae.biometricsiae.data.local.entity.IncidentEntity

data class AdminState(
    val incidents: List<IncidentEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedIncident: IncidentEntity? = null
)
