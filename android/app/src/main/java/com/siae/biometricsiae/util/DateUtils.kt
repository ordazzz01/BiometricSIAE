package com.siae.biometricsiae.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {

    private val isoFormatter = DateTimeFormatter.ISO_INSTANT
    private val displayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss", Locale("es", "MX"))
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("es", "MX"))
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss", Locale("es", "MX"))

    fun nowIso(): String {
        return isoFormatter.format(Instant.now())
    }

    fun formatForDisplay(isoTimestamp: String): String {
        return try {
            val instant = Instant.parse(isoTimestamp)
            val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
            displayFormatter.format(localDateTime)
        } catch (e: Exception) {
            isoTimestamp
        }
    }

    fun formatDate(isoTimestamp: String): String {
        return try {
            val instant = Instant.parse(isoTimestamp)
            val localDate = LocalDate.ofInstant(instant, ZoneId.systemDefault())
            dateFormatter.format(localDate)
        } catch (e: Exception) {
            isoTimestamp
        }
    }

    fun formatTime(isoTimestamp: String): String {
        return try {
            val instant = Instant.parse(isoTimestamp)
            val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
            timeFormatter.format(localDateTime)
        } catch (e: Exception) {
            isoTimestamp
        }
    }

    fun getStartOfDay(): String {
        val today = LocalDate.now()
        val startOfDay = today.atStartOfDay(ZoneId.systemDefault())
        return isoFormatter.format(startOfDay.toInstant())
    }

    fun getEndOfDay(): String {
        val today = LocalDate.now()
        val endOfDay = today.atTime(23, 59, 59).atZone(ZoneId.systemDefault())
        return isoFormatter.format(endOfDay.toInstant())
    }

    fun getStartOfMonth(): String {
        val today = LocalDate.now()
        val startOfMonth = today.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault())
        return isoFormatter.format(startOfMonth.toInstant())
    }

    fun getEndOfMonth(): String {
        val today = LocalDate.now()
        val endOfMonth = today.withDayOfMonth(today.lengthOfMonth())
            .atTime(23, 59, 59)
            .atZone(ZoneId.systemDefault())
        return isoFormatter.format(endOfMonth.toInstant())
    }

    fun getTimestampForSync(minutesAgo: Int): String {
        val past = Instant.now().minusSeconds(minutesAgo * 60L)
        return isoFormatter.format(past)
    }
}
