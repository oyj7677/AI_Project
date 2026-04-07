package com.mystarnow.backend.persistence.entity

import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "raw_source_records")
class RawSourceRecordEntity(
    @Id
    @Column(name = "id", nullable = false)
    var id: UUID,

    @Column(name = "platform_code", nullable = false, length = 32)
    var platformCode: String,

    @Column(name = "resource_scope", nullable = false, length = 32)
    var resourceScope: String,

    @Column(name = "external_object_id", nullable = false, length = 255)
    var externalObjectId: String,

    @Column(name = "channel_id")
    var channelId: UUID? = null,

    @Column(name = "influencer_id")
    var influencerId: UUID? = null,

    @Column(name = "http_status")
    var httpStatus: Int? = null,

    @Column(name = "payload_checksum", nullable = false, length = 128)
    var payloadChecksum: String,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload_json", nullable = false, columnDefinition = "jsonb")
    var payloadJson: String,

    @Column(name = "fetched_at", nullable = false)
    var fetchedAt: OffsetDateTime,

    @Column(name = "normalized_at")
    var normalizedAt: OffsetDateTime? = null,

    @Column(name = "request_trace_id", length = 255)
    var requestTraceId: String? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime,
)
