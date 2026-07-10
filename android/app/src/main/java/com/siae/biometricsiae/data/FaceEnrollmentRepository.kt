package com.siae.biometricsiae.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.siae.biometricsiae.data.local.dao.FaceEmbeddingDao
import com.siae.biometricsiae.data.local.entity.FaceEmbeddingEntity
import com.siae.biometricsiae.ml.StoredEmbedding
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import java.util.UUID

/**
 * FaceEnrollmentRepository - Maneja enrolamiento facial y sincronización con Firestore.
 *
 * FLUJO:
 * 1. Guardar embedding localmente en Room (para offline)
 * 2. Subir a Firestore (para sync multi-dispositivo)
 * 3. Cargar embeddings de Firestore al cache local
 */
class FaceEnrollmentRepository(
    private val faceEmbeddingDao: FaceEmbeddingDao
) {
    private val db = FirebaseFirestore.getInstance()
    private val orgId = "default"

    /**
     * Guarda una persona y su embedding facial.
     */
    suspend fun enrollPerson(
        fullName: String,
        rfc: String,
        embedding: FloatArray,
        quality: Float,
        deviceId: String
    ): Pair<String, String> {
        val normalizedRfc = rfc.uppercase().trim()

        // Verificar si ya existe por RFC
        val existingPerson = findPersonByRfc(normalizedRfc)

        val personId: String
        val eventType: String

        if (existingPerson != null) {
            // Persona existe - actualizar
            eventType = "update"
            personId = existingPerson.id

            updatePerson(personId, mapOf(
                "fullName" to fullName,
                "updatedAt" to Timestamp.now()
            ))
        } else {
            // Nueva persona - crear
            eventType = "create"
            personId = createPerson(fullName, normalizedRfc, deviceId)
        }

        // Guardar embedding
        val embeddingId = saveEmbedding(personId, normalizedRfc, fullName, embedding, quality, deviceId)

        // Guardar en cache local
        saveToLocalCache(personId, normalizedRfc, fullName, embedding, quality, deviceId)

        // Registrar evento
        createEnrollmentEvent(personId, normalizedRfc, deviceId, eventType)

        return Pair(personId, eventType)
    }

    /**
     * Carga todos los embeddings del cache local.
     */
    suspend fun getLocalEmbeddings(): List<StoredEmbedding> {
        return faceEmbeddingDao.getAll().map { entity ->
            StoredEmbedding(
                personId = entity.personId,
                rfc = entity.rfc,
                fullName = entity.fullName,
                vector = jsonArrayToFloatArray(entity.vector),
                quality = entity.quality,
                deviceId = entity.deviceId
            )
        }
    }

    /**
     * Sincroniza embeddings desde Firestore al cache local.
     */
    suspend fun syncFromFirestore() {
        try {
            val snapshot = db.collection("organizations")
                .document(orgId)
                .collection("persons")
                .get()
                .await()

            for (doc in snapshot.documents) {
                val personId = doc.id
                val rfc = doc.getString("rfc") ?: continue
                val fullName = doc.getString("fullName") ?: continue

                // Obtener embedding de la subcolección
                val embeddingSnapshot = db.collection("organizations")
                    .document(orgId)
                    .collection("persons")
                    .document(personId)
                    .collection("face_embeddings")
                    .limit(1)
                    .get()
                    .await()

                if (embeddingSnapshot.documents.isNotEmpty()) {
                    val embeddingDoc = embeddingSnapshot.documents[0]
                    val vector = embeddingDoc.get("vector") as? List<Double> ?: continue
                    val quality = (embeddingDoc.getDouble("quality") ?: 0.8).toFloat()
                    val deviceId = embeddingDoc.getString("deviceIdCreated") ?: ""

                    saveToLocalCache(personId, rfc, fullName, vector.map { it.toFloat() }.toFloatArray(), quality, deviceId)
                }
            }
        } catch (e: Exception) {
            // Log error
        }
    }

    private suspend fun findPersonByRfc(rfc: String): Person? {
        return try {
            val snapshot = db.collection("organizations")
                .document(orgId)
                .collection("persons")
                .whereEqualTo("rfc", rfc)
                .limit(1)
                .get()
                .await()

            snapshot.documents.firstOrNull()?.toObject(Person::class.java)
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun createPerson(fullName: String, rfc: String, deviceId: String): String {
        val docId = UUID.randomUUID().toString()
        val person = mapOf(
            "fullName" to fullName,
            "rfc" to rfc,
            "status" to "active",
            "createdAt" to Timestamp.now(),
            "updatedAt" to Timestamp.now()
        )

        db.collection("organizations")
            .document(orgId)
            .collection("persons")
            .document(docId)
            .set(person)
            .await()

        return docId
    }

    private suspend fun updatePerson(personId: String, updates: Map<String, Any>) {
        db.collection("organizations")
            .document(orgId)
            .collection("persons")
            .document(personId)
            .update(updates)
            .await()
    }

    private suspend fun saveEmbedding(
        personId: String,
        rfc: String,
        fullName: String,
        embedding: FloatArray,
        quality: Float,
        deviceId: String
    ): String {
        val embeddingId = UUID.randomUUID().toString()

        val embeddingData = mapOf(
            "vector" to embedding.toList(),
            "quality" to quality,
            "deviceIdCreated" to deviceId,
            "createdAt" to Timestamp.now(),
            "status" to "active"
        )

        db.collection("organizations")
            .document(orgId)
            .collection("persons")
            .document(personId)
            .collection("face_embeddings")
            .document(embeddingId)
            .set(embeddingData)
            .await()

        return embeddingId
    }

    private suspend fun createEnrollmentEvent(
        personId: String,
        rfc: String,
        deviceId: String,
        eventType: String
    ) {
        val eventId = UUID.randomUUID().toString()
        val event = mapOf(
            "personId" to personId,
            "rfc" to rfc,
            "deviceId" to deviceId,
            "eventType" to eventType,
            "timestamp" to Timestamp.now()
        )

        db.collection("organizations")
            .document(orgId)
            .collection("enrollment_events")
            .document(eventId)
            .set(event)
            .await()
    }

    private suspend fun saveToLocalCache(
        personId: String,
        rfc: String,
        fullName: String,
        embedding: FloatArray,
        quality: Float,
        deviceId: String
    ) {
        val entity = FaceEmbeddingEntity(
            personId = personId,
            rfc = rfc,
            fullName = fullName,
            vector = floatArrayToJson(embedding),
            quality = quality,
            deviceId = deviceId,
            enrolledAt = Timestamp.now().toDate().toString()
        )
        faceEmbeddingDao.insert(entity)
    }

    private fun floatArrayToJson(array: FloatArray): String {
        val jsonArray = JSONArray()
        for (value in array) {
            jsonArray.put(value.toDouble())
        }
        return jsonArray.toString()
    }

    private fun jsonArrayToFloatArray(json: String): FloatArray {
        val jsonArray = JSONArray(json)
        return FloatArray(jsonArray.length()) { jsonArray.getDouble(it).toFloat() }
    }
}
