package com.mystarnow.backend.common.web

import com.mystarnow.backend.common.api.ApiError
import com.mystarnow.backend.common.api.ApiResponse
import com.mystarnow.backend.common.api.ResponseMeta
import com.mystarnow.backend.common.error.BadRequestException
import com.mystarnow.backend.common.error.ResourceNotFoundException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(BadRequestException::class, IllegalArgumentException::class)
    fun handleBadRequest(ex: RuntimeException, request: HttpServletRequest): ResponseEntity<ApiResponse<Nothing>> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            errorResponse(
                request = request,
                code = "INVALID_REQUEST",
                message = ex.message ?: "Invalid request",
            ),
        )

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleNotFound(ex: ResourceNotFoundException, request: HttpServletRequest): ResponseEntity<ApiResponse<Nothing>> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            errorResponse(
                request = request,
                code = "RESOURCE_NOT_FOUND",
                message = ex.message ?: "Resource not found",
            ),
        )

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(ex: Exception, request: HttpServletRequest): ResponseEntity<ApiResponse<Nothing>> =
        ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
            errorResponse(
                request = request,
                code = "SERVICE_UNAVAILABLE",
                message = ex.message ?: "The requested resource is temporarily unavailable.",
            ),
        )

    private fun errorResponse(
        request: HttpServletRequest,
        code: String,
        message: String,
    ): ApiResponse<Nothing> = ApiResponse(
        meta = ResponseMeta(
            requestId = request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE)?.toString() ?: UUID.randomUUID().toString(),
            generatedAt = OffsetDateTime.now(ZoneOffset.UTC),
            partialFailure = false,
        ),
        data = null,
        errors = listOf(
            ApiError(
                scope = "request",
                section = null,
                code = code,
                message = message,
            ),
        ),
    )
}

