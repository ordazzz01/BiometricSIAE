package com.siae.biometricsiae.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.util.UUID

class EnrollmentRepository {
    private val db = FirebaseFirestore.getInstance()
    private val orgId = "default"

    suspend fun findPersonByRfc(rfc: String): Person? {
        return try {
            val snapshot = db.collection("organizations")
                .document(orgId)
                .collection("persons")
                .whereEqualTo("rfc", rfc.uppercase())
                .limit(1)
                .get()
                .await()

            snapshot.documents.firstOrNull()?.toObject(Person::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createPerson(person: Person): String {
        val docId = UUID.randomUUID().toString()
        val personWithId = person.copy(id = docId)

        db.collection("organizations")
            .document(orgId)
            .collection("persons")
            .document(docId)
            .set(personWithId)
            .await()

        return docId
    }

    suspend fun updatePerson(personId: String, updates: Map<String, Any>) {
        db.collection("organizations")
            .document(orgId)
            .collection("persons")
            .document(personId)
            .update(updates)
            .await()
    }

    suspend fun upsertPersonByRfc(
        fullName: String,
        rfc: String,
        deviceId: String,
        appVersion: String
    ): Pair<String, String> {
        val normalizedRfc = rfc.uppercase().trim()
        val existingPerson = findPersonByRfc(normalizedRfc)

        val eventType: String
        val personId: String

        if (existingPerson != null) {
            // Person exists - update
            eventType = if (existingPerson.fullName != fullName) "update" else "enroll"

            val updates = mapOf(
                "fullName" to fullName,
                "biometricEnabled" to true,
                "updatedAt" to Timestamp.now(),
                "lastUpdatedByDeviceId" to deviceId
            )

            updatePerson(existingPerson.id, updates)
            personId = existingPerson.id
        } else {
            // New person - create
            eventType = "create"

            val newPerson = Person(
                fullName = fullName,
                rfc = normalizedRfc,
                biometricEnabled = true,
                status = "active",
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now(),
                createdByDeviceId = deviceId,
                lastUpdatedByDeviceId = deviceId
            )

            personId = createPerson(newPerson)
        }

        // Create device enrollment
        createDeviceEnrollment(
            personId = personId,
            deviceId = deviceId,
            appVersion = appVersion
        )

        // Create audit event
        createEnrollmentEvent(
            personId = personId,
            rfc = normalizedRfc,
            fullNameSnapshot = fullName,
            deviceId = deviceId,
            eventType = eventType,
            authResult = "success",
            appVersion = appVersion
        )

        return Pair(personId, eventType)
    }

    private suspend fun createDeviceEnrollment(
        personId: String,
        deviceId: String,
        appVersion: String
    ) {
        val enrollmentId = UUID.randomUUID().toString()
        val enrollment = DeviceEnrollment(
            id = enrollmentId,
            deviceId = deviceId,
            enrolledAt = Timestamp.now(),
            authMethod = "biometric",
            status = "active",
            appVersion = appVersion
        )

        db.collection("organizations")
            .document(orgId)
            .collection("persons")
            .document(personId)
            .collection("device_enrollments")
            .document(enrollmentId)
            .set(enrollment)
            .await()
    }

    private suspend fun createEnrollmentEvent(
        personId: String,
        rfc: String,
        fullNameSnapshot: String,
        deviceId: String,
        eventType: String,
        authResult: String,
        appVersion: String
    ) {
        val eventId = UUID.randomUUID().toString()
        val event = EnrollmentEvent(
            id = eventId,
            personId = personId,
            rfc = rfc,
            fullNameSnapshot = fullNameSnapshot,
            deviceId = deviceId,
            eventType = eventType,
            authResult = authResult,
            timestamp = Timestamp.now(),
            appVersion = appVersion
        )

        db.collection("organizations")
            .document(orgId)
            .collection("enrollment_events")
            .document(eventId)
            .set(event)
            .await()
    }

    suspend fun getAllPersons(): List<Person> {
        return try {
            val snapshot = db.collection("organizations")
                .document(orgId)
                .collection("persons")
                .orderBy("fullName")
                .get()
                .await()

            snapshot.documents.mapNotNull { it.toObject(Person::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun updateDeviceLastSeen(deviceId: String) {
        try {
            db.collection("organizations")
                .document(orgId)
                .collection("devices")
                .document(deviceId)
                .set(
                    mapOf(
                        "lastSeenAt" to Timestamp.now(),
                        "active" to true
                    ),
                    SetOptions.merge()
                )
                .await()
        } catch (e: Exception) {
            // Device will be created on first enrollment
        }
    }
}
