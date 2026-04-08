package com.oyj.habit.backend.common

import com.oyj.habit.backend.habit.HabitNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@RestControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(HabitNotFoundException::class)
    fun handleHabitNotFound(error: HabitNotFoundException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiErrorResponse(error.message ?: "요청한 습관을 찾을 수 없습니다."))
    }

    @ExceptionHandler(
        IllegalArgumentException::class,
        HttpMessageNotReadableException::class,
        MethodArgumentTypeMismatchException::class,
    )
    fun handleBadRequest(error: Exception): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity
            .badRequest()
            .body(ApiErrorResponse(error.message ?: "잘못된 요청입니다."))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(error: MethodArgumentNotValidException): ResponseEntity<ApiErrorResponse> {
        val message = error.bindingResult
            .allErrors
            .firstOrNull()
            ?.let { validationError ->
                if (validationError is FieldError) {
                    validationError.defaultMessage ?: "${validationError.field} 값이 올바르지 않습니다."
                } else {
                    validationError.defaultMessage ?: "입력값이 올바르지 않습니다."
                }
            }
            ?: "입력값이 올바르지 않습니다."

        return ResponseEntity
            .badRequest()
            .body(ApiErrorResponse(message))
    }
}
