package com.siae.biometricsiae.data

data class Employee(
    val id: String = "",
    val name: String = "",
    val rfc: String = "",
    val department: String = "",
    val shift: String = "",
    val fingerprintId: String? = null,
    val active: Boolean = true
)
