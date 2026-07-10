package com.siae.biometricsiae.ml

import kotlin.math.sqrt

/**
 * SimilarityMatcher - Compara embeddings faciales por similitud de coseno.
 *
 * El umbral de 0.70-0.75 es un buen punto de partida para MobileFaceNet.
 * Ajustar según pruebas reales en el dispositivo.
 *
 * UNIDAD DE MEDIDA:
 * - Similitud de coseno: -1 (opuestos) a 1 (idénticos)
 * - Umbral recomendado: 0.70-0.75 para identificación confiable
 * - Si la diferencia entre el mejor y segundo mejor es < 0.05,
 *   la identificación es ambigua y se debe pedir confirmación manual.
 */
class SimilarityMatcher {

    companion object {
        // Umbral de similitud para considerar una coincidencia válida
        const val DEFAULT_THRESHOLD = 0.70f

        // Umbral para considerar ambigüedad (diferencia mínima entre mejor y segundo mejor)
        const val AMBIGUITY_THRESHOLD = 0.05f
    }

    /**
     * Calcula la similitud de coseno entre dos vectores.
     *
     * @param a Primer vector (embedding capturado)
     * @param b Segundo vector (embedding del catálogo)
     * @return Similitud entre -1 y 1
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

        val denominator = sqrt(normA) * sqrt(normB)
        return if (denominator > 0) {
            (dotProduct / denominator).toFloat()
        } else {
            0f
        }
    }

    /**
     * Busca la mejor coincidencia en el catálogo de embeddings.
     *
     * @param queryEmbedding Embedding capturado del rostro
     * @param catalog Lista de embeddings almacenados
     * @param threshold Umbral mínimo de similitud
     * @return MatchResult si hay coincidencia, null si no
     */
    fun findBestMatch(
        queryEmbedding: FloatArray,
        catalog: List<StoredEmbedding>,
        threshold: Float = DEFAULT_THRESHOLD
    ): MatchResult? {
        if (catalog.isEmpty()) return null

        var bestScore = 0f
        var bestMatch: StoredEmbedding? = null
        var secondBestScore = 0f

        for (embedding in catalog) {
            val score = cosineSimilarity(queryEmbedding, embedding.vector)
            if (score > bestScore) {
                secondBestScore = bestScore
                bestScore = score
                bestMatch = embedding
            } else if (score > secondBestScore) {
                secondBestScore = score
            }
        }

        return if (bestScore >= threshold && bestMatch != null) {
            val ambiguous = (bestScore - secondBestScore) < AMBIGUITY_THRESHOLD
            MatchResult(
                embedding = bestMatch,
                score = bestScore,
                ambiguous = ambiguous
            )
        } else {
            null
        }
    }

    /**
     * Busca todas las coincidencias por encima del umbral.
     */
    fun findAllMatches(
        queryEmbedding: FloatArray,
        catalog: List<StoredEmbedding>,
        threshold: Float = DEFAULT_THRESHOLD
    ): List<MatchResult> {
        return catalog.mapNotNull { embedding ->
            val score = cosineSimilarity(queryEmbedding, embedding.vector)
            if (score >= threshold) {
                MatchResult(
                    embedding = embedding,
                    score = score,
                    ambiguous = false
                )
            } else null
        }.sortedByDescending { it.score }
    }
}

data class StoredEmbedding(
    val personId: String,
    val rfc: String,
    val fullName: String,
    val vector: FloatArray,
    val quality: Float,
    val deviceId: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StoredEmbedding) return false
        return personId == other.personId && deviceId == other.deviceId
    }

    override fun hashCode(): Int {
        return personId.hashCode() * 31 + deviceId.hashCode()
    }
}

data class MatchResult(
    val embedding: StoredEmbedding,
    val score: Float,
    val ambiguous: Boolean
)
