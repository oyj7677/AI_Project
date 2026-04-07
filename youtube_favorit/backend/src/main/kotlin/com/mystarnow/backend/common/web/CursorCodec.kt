package com.mystarnow.backend.common.web

import com.mystarnow.backend.common.error.BadRequestException
import java.nio.charset.StandardCharsets
import java.util.Base64

object CursorCodec {
    fun encodeOffset(offset: Int): String =
        Base64.getUrlEncoder().withoutPadding().encodeToString(offset.toString().toByteArray(StandardCharsets.UTF_8))

    fun decodeOffset(cursor: String?): Int {
        if (cursor.isNullOrBlank()) {
            return 0
        }

        return try {
            val decoded = String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8)
            decoded.toInt().coerceAtLeast(0)
        } catch (_: Exception) {
            throw BadRequestException("Invalid cursor")
        }
    }
}
