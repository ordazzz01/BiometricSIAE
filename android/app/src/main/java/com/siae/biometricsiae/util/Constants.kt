package com.siae.biometricsiae.util

object Constants {
    // App info
    const val APP_VERSION = "1.0.0"
    const val APP_NAME = "Checador Biométrico"

    // Sync
    const val SYNC_WORK_NAME = "sync_work"
    const val SYNC_INTERVAL_MINUTES = 15L
    const val SYNC_BACKOFF_DELAY_SECONDS = 30L
    const val MAX_SYNC_RETRIES = 5

    // Duplicate detection
    const val DUPLICATE_WINDOW_MINUTES = 5

    // Biometric
    const val BIOMETRIC_TITLE = "Confirmar asistencia"
    const val BIOMETRIC_SUBTITLE = "Validar identidad"
    const val BIOMETRIC_CANCEL = "Cancelar"

    // Offline
    const val OFFLINE_MAX_QUEUE_SIZE = 1000

    // Storage
    const val EVIDENCE_MAX_SIZE_BYTES = 5 * 1024 * 1024L // 5MB
    const val EVIDENCE_QUALITY = 85

    // Security
    const val ROOT_CHECK_ENABLED = true
    const val TAMPER_CHECK_ENABLED = true

    // UI
    const val RESULT_DISPLAY_DURATION_MS = 3000L
    const val SEARCH_DEBOUNCE_MS = 300L

    // Dates
    const val DATE_FORMAT_DISPLAY = "dd/MM/yyyy"
    const val DATE_FORMAT_API = "yyyy-MM-dd"
    const val DATETIME_FORMAT_DISPLAY = "dd/MM/yyyy HH:mm:ss"
    const val DATETIME_FORMAT_API = "yyyy-MM-dd'T'HH:mm:ss'Z'"
}
