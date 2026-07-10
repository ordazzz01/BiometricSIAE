package com.siae.biometricsiae.ml

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.YuvImage
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import java.io.ByteArrayOutputStream

/**
 * FaceCropper - Recorta y pre-procesa rostros detectados.
 *
 * Toma una imagen completa (de CameraX) y un Face detectado por ML Kit,
 * y produce un bitmap recortado del rostro listo para FaceNet (112x112).
 */
class FaceCropper {

    private val targetSize = 112

    /**
     * Recorta un rostro de la imagen completa.
     *
     * @param fullImage Bitmap de la imagen completa
     * @param face Objeto Face detectado por ML Kit
     * @param padding Porcentaje de padding alrededor del rostro (0.0 - 0.5)
     * @return Bitmap del rostro recortado y redimensionado a 112x112
     */
    fun cropFace(fullImage: Bitmap, face: Face, padding: Float = 0.2f): Bitmap {
        val bounds = face.boundingBox

        // Calcular dimensiones con padding
        val width = bounds.width()
        val height = bounds.height()
        val paddingX = (width * padding).toInt()
        val paddingY = (height * padding).toInt()

        // Calcular rectángulo de recorte con padding
        val left = maxOf(0, bounds.left - paddingX)
        val top = maxOf(0, bounds.top - paddingY)
        val right = minOf(fullImage.width, bounds.right + paddingX)
        val bottom = minOf(fullImage.height, bounds.bottom + paddingY)

        // Asegurar que sea cuadrado
        val size = maxOf(right - left, bottom - top)
        val adjustedRight = minOf(fullImage.width, left + size)
        val adjustedBottom = minOf(fullImage.height, top + size)

        // Recortar
        val cropped = Bitmap.createBitmap(
            fullImage,
            left,
            top,
            adjustedRight - left,
            adjustedBottom - top
        )

        // Redimensionar a 112x112
        return Bitmap.createScaledBitmap(cropped, targetSize, targetSize, true)
    }

    /**
     * Convierte ImageProxy de CameraX a Bitmap.
     */
    fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap {
        val buffer = imageProxy.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)

        val yuvImage = YuvImage(
            bytes,
            ImageFormat.NV21,
            imageProxy.width,
            imageProxy.height,
            null
        )

        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, imageProxy.width, imageProxy.height), 90, out)
        val imageBytes = out.toByteArray()

        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    /**
     * Valida que el rostro sea adecuado para procesamiento.
     */
    fun isFaceValid(face: Face, imageWidth: Int, imageHeight: Int): FaceQuality {
        val bounds = face.boundingBox
        val faceWidth = bounds.width().toFloat() / imageWidth
        val faceHeight = bounds.height().toFloat() / imageHeight

        // Verificar tamaño mínimo (15% de la imagen)
        val isLargeEnough = faceWidth > 0.15f && faceHeight > 0.15f

        // Verificar que esté centrado (30%-70% de la imagen)
        val centerX = bounds.centerX().toFloat() / imageWidth
        val centerY = bounds.centerY().toFloat() / imageHeight
        val isCentered = centerX in 0.3f..0.7f && centerY in 0.3f..0.7f

        // Verificar ángulo (no muy inclinado)
        val eulerY = face.headEulerAngleY
        val eulerZ = face.headEulerAngleZ
        val isStraight = kotlin.math.abs(eulerY) < 20f && kotlin.math.abs(eulerZ) < 20f

        // Calcular calidad general
        val quality = when {
            !isLargeEnough -> 0.2f
            !isCentered -> 0.4f
            !isStraight -> 0.6f
            else -> 0.9f
        }

        return FaceQuality(
            isLargeEnough = isLargeEnough,
            isCentered = isCentered,
            isStraight = isStraight,
            quality = quality,
            isValid = isLargeEnough && isCentered && isStraight
        )
    }
}

data class FaceQuality(
    val isLargeEnough: Boolean,
    val isCentered: Boolean,
    val isStraight: Boolean,
    val quality: Float,
    val isValid: Boolean
)
