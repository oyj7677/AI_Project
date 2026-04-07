package com.mystarnow.backend.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "idol_members")
class IdolMemberEntity(
    @Id
    @Column(name = "id", nullable = false)
    var id: UUID,

    @Column(name = "group_id", nullable = false)
    var groupId: UUID,

    @Column(name = "slug", nullable = false, unique = true, length = 120)
    var slug: String,

    @Column(name = "display_name", nullable = false, length = 120)
    var displayName: String,

    @Column(name = "normalized_name", nullable = false, length = 120)
    var normalizedName: String,

    @Column(name = "profile_image_url")
    var profileImageUrl: String? = null,

    @Column(name = "sort_order", nullable = false)
    var sortOrder: Int = 0,

    @Column(name = "status", nullable = false, length = 16)
    var status: String = "active",

    @Column(name = "deleted_at")
    var deletedAt: OffsetDateTime? = null,
) : AuditedEntity()

