package com.siae.biometricsiae.domain.usecase

import com.siae.biometricsiae.domain.model.Schedule
import com.siae.biometricsiae.domain.model.ScheduleRule
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject

class ValidateScheduleUseCase @Inject constructor() {

    data class ScheduleValidationResult(
        val isValid: Boolean,
        val message: String,
        val toleranceMinutes: Int
    )

    operator fun invoke(
        schedule: Schedule?,
        checkinTime: ZonedDateTime = ZonedDateTime.now(),
        checkinType: String
    ): ScheduleValidationResult {
        if (schedule == null) {
            return ScheduleValidationResult(
                isValid = true,
                message = "Sin horario asignado",
                toleranceMinutes = 0
            )
        }

        val dayOfWeek = checkinTime.dayOfWeek.value
        val rule = schedule.rules.find { it.day == dayOfWeek }

        if (rule == null) {
            return ScheduleValidationResult(
                isValid = false,
                message = "No hay horario para este día",
                toleranceMinutes = 0
            )
        }

        val currentTime = checkinTime.toLocalTime()
        val tolerance = schedule.toleranceMinutes

        return when (checkinType) {
            "ENTRY" -> validateEntry(currentTime, rule, tolerance)
            "EXIT" -> validateExit(currentTime, rule, tolerance)
            "BREAK" -> validateBreak(currentTime, rule, tolerance)
            "BREAK_RETURN" -> validateBreakReturn(currentTime, rule, tolerance)
            else -> ScheduleValidationResult(
                isValid = true,
                message = "Tipo de registro no válido",
                toleranceMinutes = 0
            )
        }
    }

    private fun validateEntry(
        currentTime: LocalTime,
        rule: ScheduleRule,
        tolerance: Int
    ): ScheduleValidationResult {
        val entryTime = LocalTime.parse(rule.entryTime)
        val latestEntry = entryTime.plusMinutes(tolerance.toLong())

        return when {
            currentTime.isBefore(entryTime.minusMinutes(tolerance.toLong())) -> {
                ScheduleValidationResult(
                    isValid = false,
                    message = "Muy temprano para registrar entrada",
                    toleranceMinutes = tolerance
                )
            }
            currentTime.isAfter(latestEntry) -> {
                ScheduleValidationResult(
                    isValid = true,
                    message = "Tardanza registrada",
                    toleranceMinutes = tolerance
                )
            }
            else -> {
                ScheduleValidationResult(
                    isValid = true,
                    message = "Entrada registrada exitosamente",
                    toleranceMinutes = tolerance
                )
            }
        }
    }

    private fun validateExit(
        currentTime: LocalTime,
        rule: ScheduleRule,
        tolerance: Int
    ): ScheduleValidationResult {
        val exitTime = LocalTime.parse(rule.exitTime)
        val earliestExit = exitTime.minusMinutes(tolerance.toLong())

        return when {
            currentTime.isBefore(earliestExit) -> {
                ScheduleValidationResult(
                    isValid = false,
                    message = "Muy temprano para registrar salida",
                    toleranceMinutes = tolerance
                )
            }
            else -> {
                ScheduleValidationResult(
                    isValid = true,
                    message = "Salida registrada exitosamente",
                    toleranceMinutes = tolerance
                )
            }
        }
    }

    private fun validateBreak(
        currentTime: LocalTime,
        rule: ScheduleRule,
        tolerance: Int
    ): ScheduleValidationResult {
        val breakStartTime = rule.breakStartTime?.let { LocalTime.parse(it) }
            ?: return ScheduleValidationResult(
                isValid = false,
                message = "No hay hora de descanso configurada",
                toleranceMinutes = 0
            )

        return when {
            currentTime.isBefore(breakStartTime.minusMinutes(tolerance.toLong())) -> {
                ScheduleValidationResult(
                    isValid = false,
                    message = "Muy temprano para iniciar descanso",
                    toleranceMinutes = tolerance
                )
            }
            else -> {
                ScheduleValidationResult(
                    isValid = true,
                    message = "Descanso iniciado",
                    toleranceMinutes = tolerance
                )
            }
        }
    }

    private fun validateBreakReturn(
        currentTime: LocalTime,
        rule: ScheduleRule,
        tolerance: Int
    ): ScheduleValidationResult {
        val breakEndTime = rule.breakEndTime?.let { LocalTime.parse(it) }
            ?: return ScheduleValidationResult(
                isValid = false,
                message = "No hay hora de regreso configurada",
                toleranceMinutes = 0
            )

        return when {
            currentTime.isAfter(breakEndTime.plusMinutes(tolerance.toLong())) -> {
                ScheduleValidationResult(
                    isValid = false,
                    message = "Tardanza en regreso de descanso",
                    toleranceMinutes = tolerance
                )
            }
            else -> {
                ScheduleValidationResult(
                    isValid = true,
                    message = "Regreso de descanso registrado",
                    toleranceMinutes = tolerance
                )
            }
        }
    }
}
