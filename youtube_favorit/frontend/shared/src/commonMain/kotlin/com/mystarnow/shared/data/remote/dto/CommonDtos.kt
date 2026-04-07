package com.mystarnow.shared.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiEnvelopeDto<T>(
    val meta: ResponseMetaDto,
    val data: T? = null,
    val errors: List<ApiErrorDto> = emptyList(),
)

@Serializable
data class ResponseMetaDto(
    val requestId: String,
    val apiVersion: String,
    val generatedAt: String,
    val partialFailure: Boolean,
)

@Serializable
data class ApiErrorDto(
    val scope: String,
    val section: String? = null,
    val code: String,
    val message: String,
)

@Serializable
data class SectionDto<T>(
    val status: String,
    val freshness: String,
    val generatedAt: String,
    val staleAt: String? = null,
    val data: T,
    val error: SectionErrorDto? = null,
)

@Serializable
data class SectionErrorDto(
    val code: String,
    val message: String,
    val retryable: Boolean = false,
    val source: String? = null,
)

@Serializable
data class EmptyObjectDto(
    @SerialName("_")
    val placeholder: String? = null,
)
