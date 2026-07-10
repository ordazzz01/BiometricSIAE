package com.siae.biometricsiae.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()
    private val orgId = "default"

    suspend fun getEmployeeByFingerprintId(fingerprintId: String): Employee? {
        return try {
            val snapshot = db.collection("organizations")
                .document(orgId)
                .collection("employees")
                .whereEqualTo("fingerprintId", fingerprintId)
                .whereEqualTo("active", true)
                .limit(1)
                .get()
                .await()

            snapshot.documents.firstOrNull()?.toObject(Employee::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getEmployeeById(employeeId: String): Employee? {
        return try {
            val doc = db.collection("organizations")
                .document(orgId)
                .collection("employees")
                .document(employeeId)
                .get()
                .await()

            doc.toObject(Employee::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getEmployees(): List<Employee> {
        return try {
            val snapshot = db.collection("organizations")
                .document(orgId)
                .collection("employees")
                .whereEqualTo("active", true)
                .get()
                .await()

            snapshot.documents.mapNotNull { it.toObject(Employee::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveAttendance(record: AttendanceRecord): Boolean {
        return try {
            val docId = UUID.randomUUID().toString()
            val recordWithId = record.copy(id = docId)

            db.collection("organizations")
                .document(orgId)
                .collection("attendance_records")
                .document(docId)
                .set(recordWithId)
                .await()

            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateEmployeeFingerprint(employeeId: String, fingerprintId: String): Boolean {
        return try {
            db.collection("organizations")
                .document(orgId)
                .collection("employees")
                .document(employeeId)
                .update("fingerprintId", fingerprintId)
                .await()

            true
        } catch (e: Exception) {
            false
        }
    }
}
