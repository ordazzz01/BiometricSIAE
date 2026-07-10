package com.siae.biometricsiae.ml

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import com.siae.biometricsiae.data.FaceEnrollmentRepository
import com.siae.biometricsiae.data.local.dao.FaceEmbeddingDao
import com.siae.biometricsiae.data.local.entity.FaceEmbeddingEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray

/**
 * FaceRecognitionManager - Orquesta enrolamiento e identificación facial.
 *
 * FLUJO DE ENROLAMIENTO:
 * 1. Capturar imagen con CameraX
 * 2. Detectar rostro con ML Kit
 * 3. Recortar rostro (FaceCropper)
 * 4. Generar embedding (FaceEmbeddingGenerator)
 * 5. Guardar en Firestore + Room cache
 *
 * FLUJO DE IDENTIFICACIÓN:
 * 1. Capturar imagen en vivo
 * 2. Detectar rostro con ML Kit
 * 3. Recortar rostro
 * 4. Generar embedding
 * 5. Comparar con cache local (cosine similarity)
 * 6. Si coincidencia > umbral → identificar persona
 * 7. Si no → "No identificado"
 */
class FaceRecognitionManager(
    private val context: Context,
    private val enrollmentRepository: FaceEnrollmentRepository
) {
    private val embeddingGenerator = FaceEmbeddingGenerator(context)
    private val faceCropper = FaceCropper()
    private val similarityMatcher = SimilarityMatcher()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _state = MutableStateFlow<RecognitionState>(RecognitionState.Idle)
    val state: StateFlow<RecognitionState> = _state.asStateFlow()

    // Cache local de embeddings
    private var localEmbeddings: List<StoredEmbedding> = emptyList()

    sealed class RecognitionState {
        data object Idle : RecognitionState()
        data object DetectingFace : RecognitionState()
        data object GeneratingEmbedding : RecognitionState()
        data object Matching : RecognitionState()
        data class Identified(
            val personId: String,
            val fullName: String,
            val rfc: String,
            val confidenceScore: Float
        ) : RecognitionState()
        data class Ambiguous(
            val candidates: List<MatchResult>
        ) : RecognitionState()
        data object NotIdentified : RecognitionState()
        data class Error(val message: String) : RecognitionState()
        data object Enrolling : RecognitionState()
        data class EnrollSuccess(
            val personId: String,
            val credentialId: String
        ) : RecognitionState()
    }

    init {
        // Cargar embeddings del cache local al iniciar
        scope.launch {
            localEmbeddings = enrollmentRepository.getLocalEmbeddings()
        }
    }

    /**
     * Identifica un rostro a partir de un bitmap.
     *
     * @param bitmap Imagen completa capturada por CameraX
     * @param threshold Umbral de similitud (0.70-0.75 recomendado)
     * @return Resultado de identificación
     */
    suspend fun identifyFace(
        bitmap: Bitmap,
        threshold: Float = 0.70f
    ): RecognitionState {
        _state.value = RecognitionState.DetectingFace

        return try {
            // 1. Detectar rostro con ML Kit (simplificado - en producción usar ML Kit)
            // Por ahora, asumimos que el rostro ya fue detectado y recortado

            // 2. Generar embedding
            _state.value = RecognitionState.GeneratingEmbedding
            val embedding = embeddingGenerator.generateEmbedding(bitmap)

            // 3. Comparar con cache local
            _state.value = RecognitionState.Matching
            val result = similarityMatcher.findBestMatch(embedding, localEmbeddings, threshold)

            if (result != null) {
                if (result.ambiguous) {
                    _state.value = RecognitionState.Ambiguous(listOf(result))
                } else {
                    val identified = RecognitionState.Identified(
                        personId = result.embedding.personId,
                        fullName = result.embedding.fullName,
                        rfc = result.embedding.rfc,
                        confidenceScore = result.score
                    )
                    _state.value = identified
                    identified
                }
            } else {
                _state.value = RecognitionState.NotIdentified
                RecognitionState.NotIdentified
            }
        } catch (e: Exception) {
            val error = RecognitionState.Error("Error: ${e.message}")
            _state.value = error
            error
        }
    }

    /**
     * Registra una persona con su embedding facial.
     */
    suspend fun enrollPerson(
        fullName: String,
        rfc: String,
        faceBitmap: Bitmap
    ): RecognitionState {
        _state.value = RecognitionState.Enrolling

        return try {
            // 1. Generar embedding
            val embedding = embeddingGenerator.generateEmbedding(faceBitmap)

            // 2. Calcular calidad del rostro
            val quality = 0.85f // Simplificado - en producción usar FaceCropper

            // 3. Guardar en Firestore + Room
            val (personId, eventType) = enrollmentRepository.enrollPerson(
                fullName = fullName,
                rfc = rfc,
                embedding = embedding,
                quality = quality,
                deviceId = Build.SERIAL ?: Build.MODEL
            )

            // 4. Actualizar cache local
            localEmbeddings = enrollmentRepository.getLocalEmbeddings()

            val success = RecognitionState.EnrollSuccess(
                personId = personId,
                credentialId = embedding.take(8).joinToString("") { "%02x".format((it * 255).toInt()) }
            )
            _state.value = success
            success
        } catch (e: Exception) {
            val error = RecognitionState.Error("Error al enrollar: ${e.message}")
            _state.value = error
            error
        }
    }

    /**
     * Recarga el cache local desde Firestore.
     */
    suspend fun refreshCache() {
        try {
            enrollmentRepository.syncFromFirestore()
            localEmbeddings = enrollmentRepository.getLocalEmbeddings()
        } catch (e: Exception) {
            // Log error
        }
    }

    fun getEmbeddingCount(): Int = localEmbeddings.size

    fun close() {
        embeddingGenerator.close()
    }
}
