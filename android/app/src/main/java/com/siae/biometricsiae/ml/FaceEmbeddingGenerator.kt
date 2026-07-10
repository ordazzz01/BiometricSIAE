package com.siae.biometricsiae.ml

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sqrt

/**
 * FaceEmbeddingGenerator - Genera embeddings faciales usando MobileFaceNet.
 *
 * El modelo TFLite toma una imagen de 112x112 y produce un vector de 192 floats.
 * Este vector representa las características faciales de la persona.
 *
 * IMPORTANTE: El embedding es una representación numérica del rostro,
 * NO es la imagen ni la huella digital. Se usa para comparar rostros
 * por similitud de coseno.
 */
class FaceEmbeddingGenerator(private val context: Context) {

    private val interpreter: Interpreter
    private val inputSize = 112
    private val embeddingSize = 192

    init {
        val modelFile = FileUtil.loadMappedFile(context, "mobile_face_net.tflite")
        interpreter = Interpreter(modelFile)
    }

    /**
     * Genera un embedding facial a partir de un bitmap del rostro.
     *
     * @param faceBitmap Bitmap del rostro recortado (debe ser cuadrado)
     * @return Vector de 192 floats normalizado
     */
    fun generateEmbedding(faceBitmap: Bitmap): FloatArray {
        // 1. Resize a 112x112
        val resizedBitmap = Bitmap.createScaledBitmap(faceBitmap, inputSize, inputSize, true)

        // 2. Convertir a ByteBuffer
        val inputBuffer = ByteBuffer.allocateDirect(1 * inputSize * inputSize * 3 * 4)
        inputBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(inputSize * inputSize)
        resizedBitmap.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize)

        for (pixel in pixels) {
            // Normalizar a [-1, 1]
            inputBuffer.putFloat(((pixel shr 16 and 0xFF) - 127.5f) / 128f)
            inputBuffer.putFloat(((pixel shr 8 and 0xFF) - 127.5f) / 128f)
            inputBuffer.putFloat(((pixel and 0xFF) - 127.5f) / 128f)
        }

        // 3. Preparar output buffer
        val outputBuffer = Array(1) { FloatArray(embeddingSize) }

        // 4. Ejecutar inferencia
        interpreter.run(inputBuffer, outputBuffer)

        // 5. Normalizar vector
        return normalizeVector(outputBuffer[0])
    }

    /**
     * Normaliza un vector para que tenga magnitud 1.
     * Esto es necesario para que la similitud de coseno funcione correctamente.
     */
    private fun normalizeVector(vector: FloatArray): FloatArray {
        var norm = 0f
        for (value in vector) {
            norm += value * value
        }
        norm = sqrt(norm)

        return if (norm > 0) {
            FloatArray(vector.size) { vector[it] / norm }
        } else {
            vector
        }
    }

    /**
     * Calcula la similitud de coseno entre dos vectores.
     * Retorna un valor entre -1 y 1, donde 1 es idéntico.
     */
    fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
        if (a.size != b.size) return 0f

        var dotProduct = 0.0
        var normA = 0.0
        var normB = 0.0

        for (i in a.indices) {
            dotProduct += a[i] * b[i]
            normA += a[i] * a[i]
            normB += b[i] * b[i]
        }

        return (dotProduct / (sqrt(normA) * sqrt(normB))).toFloat()
    }

    fun close() {
        interpreter.close()
    }
}
