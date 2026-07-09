package com.siae.biometricsiae.data.repository

import com.siae.biometricsiae.data.local.dao.EmployeeDao
import com.siae.biometricsiae.data.local.entity.EmployeeEntity
import com.siae.biometricsiae.data.remote.api.AsistenciasApi
import com.siae.biometricsiae.data.remote.firebase.FirestoreManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmployeeRepository @Inject constructor(
    private val employeeDao: EmployeeDao,
    private val firestoreManager: FirestoreManager,
    private val api: AsistenciasApi
) {
    fun getActiveEmployees(): Flow<List<EmployeeEntity>> {
        return employeeDao.getAllActiveEmployees()
    }

    fun getEmployeesByBranch(branchId: String): Flow<List<EmployeeEntity>> {
        return employeeDao.getEmployeesByBranch(branchId)
    }

    suspend fun getEmployeeById(id: String): EmployeeEntity? {
        return employeeDao.getEmployeeById(id)
    }

    suspend fun getEmployeeByCode(code: String): EmployeeEntity? {
        return employeeDao.getEmployeeByCode(code)
    }

    fun searchEmployees(query: String): Flow<List<EmployeeEntity>> {
        return employeeDao.searchEmployees(query)
    }

    suspend fun syncEmployeesFromServer(orgId: String, branchId: String) {
        try {
            val remoteEmployees = api.getEmployeesByBranch(branchId).body() ?: return
            
            val entities = remoteEmployees.map { dto ->
                EmployeeEntity(
                    id = dto.id,
                    tenantId = orgId,
                    code = dto.code,
                    name = dto.name,
                    department = dto.department,
                    position = dto.position,
                    photoUrl = dto.photoUrl,
                    branchIds = dto.branchIds,
                    scheduleId = dto.scheduleId,
                    biometricEnrolled = dto.biometricEnrolled,
                    faceRegistered = dto.faceRegistered,
                    pin = null,
                    active = dto.active,
                    createdAt = dto.createdAt,
                    updatedAt = dto.updatedAt
                )
            }

            employeeDao.insertEmployees(entities)
        } catch (e: Exception) {
            // Log error but don't crash
        }
    }

    suspend fun insertEmployee(employee: EmployeeEntity) {
        employeeDao.insertEmployee(employee)
    }

    suspend fun updateEmployee(employee: EmployeeEntity) {
        employeeDao.updateEmployee(employee)
    }

    suspend fun deleteAllEmployees() {
        employeeDao.deleteAllEmployees()
    }
}
