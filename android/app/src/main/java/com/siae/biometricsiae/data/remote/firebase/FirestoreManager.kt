package com.siae.biometricsiae.data.remote.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreManager @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val ORGANIZATIONS = "organizations"
        private const val EMPLOYEES = "employees"
        private const val ATTENDANCE = "attendance"
        private const val DEVICES = "devices"
        private const val BRANCHES = "branches"
        private const val INCIDENTS = "incidents"
        private const val AUDIT_LOGS = "audit_logs"
        private const val SYNC_LOGS = "sync_logs"
        private const val APP_SETTINGS = "app_settings"
    }

    // Organization
    suspend fun getOrganization(orgId: String): Map<String, Any>? {
        return firestore.collection(ORGANIZATIONS)
            .document(orgId)
            .get()
            .await()
            .data
    }

    // Employees
    suspend fun getEmployees(orgId: String, branchId: String? = null): List<Map<String, Any>> {
        var query = firestore.collection(ORGANIZATIONS)
            .document(orgId)
            .collection(EMPLOYEES)
            .whereEqualTo("active", true)

        if (branchId != null) {
            query = query.whereArrayContains("branchIds", branchId)
        }

        return query.get().await().documents.mapNotNull { it.data }
    }

    suspend fun getEmployee(orgId: String, employeeId: String): Map<String, Any>? {
        return firestore.collection(ORGANIZATIONS)
            .document(orgId)
            .collection(EMPLOYEES)
            .document(employeeId)
            .get()
            .await()
            .data
    }

    // Attendance
    suspend fun saveAttendance(orgId: String, record: Map<String, Any>) {
        val recordId = record["id"] as? String ?: return
        firestore.collection(ORGANIZATIONS)
            .document(orgId)
            .collection(ATTENDANCE)
            .document(recordId)
            .set(record)
            .await()
    }

    suspend fun getAttendanceByDate(
        orgId: String,
        branchId: String,
        startDate: String,
        endDate: String
    ): List<Map<String, Any>> {
        return firestore.collection(ORGANIZATIONS)
            .document(orgId)
            .collection(ATTENDANCE)
            .whereEqualTo("branchId", branchId)
            .whereGreaterThanOrEqualTo("timestamp", startDate)
            .whereLessThanOrEqualTo("timestamp", endDate)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .await()
            .documents.mapNotNull { it.data }
    }

    // Devices
    suspend fun updateDeviceStatus(orgId: String, deviceId: String, data: Map<String, Any>) {
        firestore.collection(ORGANIZATIONS)
            .document(orgId)
            .collection(DEVICES)
            .document(deviceId)
            .update(data)
            .await()
    }

    // Sync logs
    suspend fun saveSyncLog(orgId: String, log: Map<String, Any>) {
        firestore.collection(ORGANIZATIONS)
            .document(orgId)
            .collection(SYNC_LOGS)
            .add(log)
            .await()
    }

    // Audit logs
    suspend fun saveAuditLog(orgId: String, log: Map<String, Any>) {
        firestore.collection(ORGANIZATIONS)
            .document(orgId)
            .collection(AUDIT_LOGS)
            .add(log)
            .await()
    }
}
