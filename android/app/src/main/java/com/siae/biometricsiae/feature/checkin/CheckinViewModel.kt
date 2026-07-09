package com.siae.biometricsiae.feature.checkin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.siae.biometricsiae.data.repository.AttendanceRepository
import com.siae.biometricsiae.data.repository.EmployeeRepository
import com.siae.biometricsiae.domain.model.AttendanceType
import com.siae.biometricsiae.domain.model.AuthMethod
import com.siae.biometricsiae.domain.model.CheckinResult
import com.siae.biometricsiae.domain.model.Employee
import com.siae.biometricsiae.domain.usecase.AuthenticateBiometricUseCase
import com.siae.biometricsiae.domain.usecase.DetectDuplicateCheckinUseCase
import com.siae.biometricsiae.domain.usecase.RegisterCheckinUseCase
import com.siae.biometricsiae.security.BiometricHelper
import com.siae.biometricsiae.security.KeystoreManager
import com.siae.biometricsiae.security.RootDetector
import com.siae.biometricsiae.util.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CheckinViewModel @Inject constructor(
    application: Application,
    private val registerCheckinUseCase: RegisterCheckinUseCase,
    private val authenticateBiometricUseCase: AuthenticateBiometricUseCase,
    private val detectDuplicateCheckinUseCase: DetectDuplicateCheckinUseCase,
    private val employeeRepository: EmployeeRepository,
    private val attendanceRepository: AttendanceRepository,
    private val keyStoreManager: KeystoreManager,
    private val rootDetector: RootDetector,
    private val networkUtils: NetworkUtils,
    private val biometricHelper: BiometricHelper
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(CheckinState())
    val state: StateFlow<CheckinState> = _state.asStateFlow()

    private var selectedCheckinType: CheckinType = CheckinType.ENTRY

    init {
        loadEmployees()
        observeNetworkStatus()
        observePendingSync()
        observeBiometricResults()
        checkSecurity()
    }

    private fun loadEmployees() {
        val branchId = keyStoreManager.getBranchId() ?: return
        
        viewModelScope.launch {
            employeeRepository.getEmployeesByBranch(branchId).collectLatest { employees ->
                _state.value = _state.value.copy(
                    employees = employees.map { entity ->
                        Employee(
                            id = entity.id,
                            tenantId = entity.tenantId,
                            code = entity.code,
                            name = entity.name,
                            department = entity.department,
                            position = entity.position,
                            photoUrl = entity.photoUrl,
                            branchIds = entity.branchIds,
                            scheduleId = entity.scheduleId,
                            biometricEnrolled = entity.biometricEnrolled,
                            faceRegistered = entity.faceRegistered,
                            active = entity.active,
                            createdAt = entity.createdAt,
                            updatedAt = entity.updatedAt
                        )
                    }
                )
            }
        }
    }

    private fun observeNetworkStatus() {
        viewModelScope.launch {
            networkUtils.networkFlow.collectLatest { isOnline ->
                _state.value = _state.value.copy(isOffline = !isOnline)
            }
        }
    }

    private fun observePendingSync() {
        viewModelScope.launch {
            attendanceRepository.getPendingSyncRecords().collectLatest { pending ->
                _state.value = _state.value.copy(pendingSyncCount = pending.size)
            }
        }
    }

    private fun observeBiometricResults() {
        viewModelScope.launch {
            biometricHelper.resultFlow.collectLatest { result ->
                when (result) {
                    is BiometricHelper.BiometricResult.Success -> {
                        proceedWithCheckin()
                    }
                    is BiometricHelper.BiometricResult.UserCancelled -> {
                        _state.value = _state.value.copy(
                            lastCheckinResult = CheckinUiResult.BiometricCancelled,
                            isLoading = false
                        )
                    }
                    is BiometricHelper.BiometricResult.Error -> {
                        _state.value = _state.value.copy(
                            lastCheckinResult = CheckinUiResult.Error(result.message),
                            isLoading = false
                        )
                    }
                    is BiometricHelper.BiometricResult.NotAvailable,
                    is BiometricHelper.BiometricResult.NotEnrolled -> {
                        _state.value = _state.value.copy(
                            lastCheckinResult = CheckinUiResult.RequiresPin,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    private fun checkSecurity() {
        viewModelScope.launch {
            val status = rootDetector.getSecurityStatus()
            if (status.isTampered) {
                _state.value = _state.value.copy(
                    errorMessage = "Dispositivo comprometido. La aplicación no puede funcionar de forma segura."
                )
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        
        if (query.isNotEmpty()) {
            viewModelScope.launch {
                employeeRepository.searchEmployees(query).collectLatest { employees ->
                    _state.value = _state.value.copy(
                        employees = employees.map { entity ->
                            Employee(
                                id = entity.id,
                                tenantId = entity.tenantId,
                                code = entity.code,
                                name = entity.name,
                                department = entity.department,
                                position = entity.position,
                                photoUrl = entity.photoUrl,
                                branchIds = entity.branchIds,
                                scheduleId = entity.scheduleId,
                                biometricEnrolled = entity.biometricEnrolled,
                                faceRegistered = entity.faceRegistered,
                                active = entity.active,
                                createdAt = entity.createdAt,
                                updatedAt = entity.updatedAt
                            )
                        }
                    )
                }
            }
        }
    }

    fun selectEmployee(employee: Employee) {
        _state.value = _state.value.copy(
            selectedEmployee = employee,
            searchQuery = employee.name
        )
    }

    fun selectCheckinType(type: CheckinType) {
        selectedCheckinType = type
    }

    fun startBiometricAuthentication(activity: androidx.fragment.app.FragmentActivity) {
        val employee = _state.value.selectedEmployee ?: return
        
        _state.value = _state.value.copy(isLoading = true)

        if (!authenticateBiometricUseCase.canAuthenticate()) {
            _state.value = _state.value.copy(
                lastCheckinResult = CheckinUiResult.RequiresPin,
                isLoading = false
            )
            return
        }

        biometricHelper.authenticate(
            activity = activity,
            title = "Confirmar asistencia",
            subtitle = "Validar ${selectedCheckinType.displayName.lowercase()} para ${employee.name}"
        )
    }

    private fun proceedWithCheckin() {
        val employee = _state.value.selectedEmployee ?: return
        val isOffline = _state.value.isOffline

        viewModelScope.launch {
            // Check for duplicate
            val duplicateCheck = detectDuplicateCheckinUseCase(
                employeeId = employee.id,
                type = selectedCheckinType.name
            )

            if (duplicateCheck.isDuplicate) {
                _state.value = _state.value.copy(
                    lastCheckinResult = CheckinUiResult.Success(
                        employeeName = employee.name,
                        type = selectedCheckinType.displayName,
                        timestamp = java.time.Instant.now().toString(),
                        isDuplicate = true
                    ),
                    isLoading = false
                )
                return@launch
            }

            // Register checkin
            val result = registerCheckinUseCase(
                employeeId = employee.id,
                employeeName = employee.name,
                branchId = keyStoreManager.getBranchId() ?: "",
                branchName = _state.value.currentBranchName,
                deviceId = keyStoreManager.getDeviceId() ?: "",
                type = selectedCheckinType.name,
                method = AuthMethod.BIOMETRIC.name,
                latitude = null,
                longitude = null,
                faceEvidenceUrl = null,
                biometricAuthEventId = null,
                isOffline = isOffline
            )

            when (result) {
                is CheckinResult.Success -> {
                    _state.value = _state.value.copy(
                        lastCheckinResult = CheckinUiResult.Success(
                            employeeName = employee.name,
                            type = selectedCheckinType.displayName,
                            timestamp = result.record.timestamp,
                            isDuplicate = result.isDuplicate
                        ),
                        isLoading = false,
                        selectedEmployee = null,
                        searchQuery = ""
                    )
                }
                is CheckinResult.Duplicate -> {
                    _state.value = _state.value.copy(
                        lastCheckinResult = CheckinUiResult.Success(
                            employeeName = employee.name,
                            type = selectedCheckinType.displayName,
                            timestamp = result.existingRecord.timestamp,
                            isDuplicate = true
                        ),
                        isLoading = false
                    )
                }
                is CheckinResult.Offline -> {
                    _state.value = _state.value.copy(
                        lastCheckinResult = CheckinUiResult.Offline(
                            employeeName = employee.name,
                            type = selectedCheckinType.displayName
                        ),
                        isLoading = false,
                        selectedEmployee = null,
                        searchQuery = ""
                    )
                }
                is CheckinResult.Error -> {
                    _state.value = _state.value.copy(
                        lastCheckinResult = CheckinUiResult.Error(result.message),
                        isLoading = false
                    )
                }
                is CheckinResult.RequiresPin -> {
                    _state.value = _state.value.copy(
                        lastCheckinResult = CheckinUiResult.RequiresPin,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun clearResult() {
        _state.value = _state.value.copy(lastCheckinResult = null)
    }

    fun getBiometricType(): String {
        return authenticateBiometricUseCase.getAvailableBiometricType()
    }
}
