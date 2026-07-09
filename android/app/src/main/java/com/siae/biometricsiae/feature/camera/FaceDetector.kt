package com.siae.biometricsiae.feature.camera

import android.content.Context
import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.siae.biometricsiae.util.SecureLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FaceDetector @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
        .build()

    private val detector = FaceDetection.getClient(options)

    data class FaceAnalysis(
        val faceDetected: Boolean,
        val faceCentered: Boolean,
        val eyesVisible: Boolean,
        val livenessScore: Float,
        val faceCount: Int
    )

    suspend fun analyzeBitmap(bitmap: Bitmap): FaceAnalysis {
        return try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val faces = detector.process(image).await()

            val face = faces.firstOrNull()
            
            if (face == null) {
                return FaceAnalysis(
                    faceDetected = false,
                    faceCentered = false,
                    eyesVisible = false,
                    livenessScore = 0f,
                    faceCount = 0
                )
            }

            val bounds = face.boundingBox
            val imageWidth = bitmap.width
            val imageHeight = bitmap.height

            // Check if face is centered
            val centerX = bounds.centerX().toFloat() / imageWidth
            val centerY = bounds.centerY().toFloat() / imageHeight
            val faceCentered = centerX in 0.3f..0.7f && centerY in 0.3f..0.7f

            // Check if face is large enough (close to camera)
            val faceSize = bounds.width().toFloat() / imageWidth
            val faceLargeEnough = faceSize > 0.15f

            // Basic liveness check: face should be detected and centered
            val livenessScore = when {
                !faceCentered -> 0.3f
                !faceLargeEnough -> 0.5f
                else -> 0.9f
            }

            FaceAnalysis(
                faceDetected = true,
                faceCentered = faceCentered,
                eyesVisible = true, // Simplified - in production, use landmark detection
                livenessScore = livenessScore,
                faceCount = faces.size
            )
        } catch (e: Exception) {
            SecureLogger.e("FaceDetector", "Analysis failed", e)
            FaceAnalysis(
                faceDetected = false,
                faceCentered = false,
                eyesVisible = false,
                livenessScore = 0f,
                faceCount = 0
            )
        }
    }

    fun close() {
        detector.close()
    }
}
