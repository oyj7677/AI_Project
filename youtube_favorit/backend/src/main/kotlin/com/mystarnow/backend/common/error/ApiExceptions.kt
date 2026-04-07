package com.mystarnow.backend.common.error

class ResourceNotFoundException(
    message: String,
) : RuntimeException(message)

class BadRequestException(
    message: String,
) : RuntimeException(message)

