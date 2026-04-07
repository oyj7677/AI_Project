package com.mystarnow.backend.common.api

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.OffsetDateTime

data class ApiResponse<T>(
    val meta: ResponseMeta,
    val data: T?,
    val errors: List<ApiError> = emptyList(),
)

data class ResponseMeta(
    val requestId: String,
    val apiVersion: String = "v1",
    val generatedAt: OffsetDateTime,
    val partialFailure: Boolean,
)

data class ApiError(
    val scope: String,
    val section: String?,
    val code: String,
    val message: String,
)

enum class SectionStatus {
    success,
    partial,
    failed,
    empty,
}

enum class FreshnessStatus {
    fresh,
    stale,
    manual,
    unknown,
}

data class SectionError(
    val code: String,
    val message: String,
    val retryable: Boolean,
    val source: String?,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class SectionState<T>(
    val status: SectionStatus,
    val freshness: FreshnessStatus,
    val generatedAt: OffsetDateTime,
    val staleAt: OffsetDateTime? = null,
    val data: T,
    val error: SectionError? = null,
)

object SectionStates {
    fun <T> success(
        data: T,
        generatedAt: OffsetDateTime,
        freshness: FreshnessStatus = FreshnessStatus.fresh,
    ): SectionState<T> = SectionState(
        status = SectionStatus.success,
        freshness = freshness,
        generatedAt = generatedAt,
        data = data,
    )

    fun <T> empty(
        data: T,
        generatedAt: OffsetDateTime,
        freshness: FreshnessStatus = FreshnessStatus.unknown,
    ): SectionState<T> = SectionState(
        status = SectionStatus.empty,
        freshness = freshness,
        generatedAt = generatedAt,
        data = data,
    )

    fun <T> partial(
        data: T,
        generatedAt: OffsetDateTime,
        freshness: FreshnessStatus,
        error: SectionError,
        staleAt: OffsetDateTime? = null,
    ): SectionState<T> = SectionState(
        status = SectionStatus.partial,
        freshness = freshness,
        generatedAt = generatedAt,
        staleAt = staleAt,
        data = data,
        error = error,
    )

    fun <T> failed(
        data: T,
        generatedAt: OffsetDateTime,
        error: SectionError,
    ): SectionState<T> = SectionState(
        status = SectionStatus.failed,
        freshness = FreshnessStatus.unknown,
        generatedAt = generatedAt,
        data = data,
        error = error,
    )
}

fun collectSectionErrors(vararg sections: Pair<String, SectionState<*>>): List<ApiError> =
    sections.mapNotNull { (name, state) ->
        state.error?.let {
            ApiError(
                scope = "section",
                section = name,
                code = it.code,
                message = it.message,
            )
        }
    }

fun hasPartialFailure(vararg states: SectionState<*>): Boolean =
    states.any { it.status == SectionStatus.partial || it.status == SectionStatus.failed }
