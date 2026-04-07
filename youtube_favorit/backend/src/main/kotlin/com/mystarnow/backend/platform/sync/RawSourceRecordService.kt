package com.mystarnow.backend.platform.sync

import com.fasterxml.jackson.databind.ObjectMapper
import com.mystarnow.backend.persistence.entity.RawSourceRecordEntity
import com.mystarnow.backend.persistence.repository.RawSourceRecordRepository
import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.time.Clock
import java.time.OffsetDateTime
import java.util.UUID

@Service
class RawSourceRecordService(
    private val repository: RawSourceRecordRepository,
    private val objectMapper: ObjectMapper,
    private val clock: Clock,
) {
    fun record(
        platformCode: String,
        resourceScope: String,
        externalObjectId: String,
        payload: Any,
        channelId: UUID?,
        influencerId: UUID?,
        httpStatus: Int?,
        requestTraceId: String?,
    ): RawSourceRecordEntity {
        val payloadJson = objectMapper.writeValueAsString(payload)
        val checksum = sha256(payloadJson)
        return repository.save(
            RawSourceRecordEntity(
                id = UUID.randomUUID(),
                platformCode = platformCode,
                resourceScope = resourceScope,
                externalObjectId = externalObjectId,
                channelId = channelId,
                influencerId = influencerId,
                httpStatus = httpStatus,
                payloadChecksum = checksum,
                payloadJson = payloadJson,
                fetchedAt = OffsetDateTime.now(clock),
                requestTraceId = requestTraceId,
                createdAt = OffsetDateTime.now(clock),
            ),
        )
    }

    private fun sha256(value: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray())
            .joinToString("") { "%02x".format(it) }
}
