package com.mystarnow.backend.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "platforms")
class PlatformEntity(
    @Id
    @Column(name = "platform_code", nullable = false, length = 32)
    var platformCode: String,

    @Column(name = "display_name", nullable = false, length = 64)
    var displayName: String,

    @Column(name = "support_mode", nullable = false, length = 16)
    var supportMode: String,

    @Column(name = "is_enabled", nullable = false)
    var enabled: Boolean,
) : AuditedEntity()

