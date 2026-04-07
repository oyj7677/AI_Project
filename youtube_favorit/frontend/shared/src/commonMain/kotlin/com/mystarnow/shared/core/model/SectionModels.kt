package com.mystarnow.shared.core.model

enum class SectionStatus {
    SUCCESS,
    PARTIAL,
    FAILED,
    EMPTY;

    companion object {
        fun fromApi(value: String): SectionStatus = when (value.lowercase()) {
            "success" -> SUCCESS
            "partial" -> PARTIAL
            "failed" -> FAILED
            else -> EMPTY
        }
    }
}

enum class Freshness {
    FRESH,
    STALE,
    MANUAL,
    UNKNOWN;

    companion object {
        fun fromApi(value: String): Freshness = when (value.lowercase()) {
            "fresh" -> FRESH
            "stale" -> STALE
            "manual" -> MANUAL
            else -> UNKNOWN
        }
    }
}

data class ResponseMeta(
    val requestId: String,
    val apiVersion: String,
    val generatedAt: String,
    val partialFailure: Boolean,
)

data class ApiError(
    val scope: String,
    val section: String?,
    val code: String,
    val message: String,
)

data class SectionError(
    val code: String,
    val message: String,
    val retryable: Boolean,
    val source: String?,
)

data class SectionModel<T>(
    val status: SectionStatus,
    val freshness: Freshness,
    val generatedAt: String,
    val staleAt: String?,
    val data: T,
    val error: SectionError?,
) {
    fun <R> map(transform: (T) -> R): SectionModel<R> = SectionModel(
        status = status,
        freshness = freshness,
        generatedAt = generatedAt,
        staleAt = staleAt,
        data = transform(data),
        error = error,
    )
}
