package com.mystarnow.backend.persistence.repository

import com.mystarnow.backend.persistence.entity.IdolMemberEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface IdolMemberRepository : JpaRepository<IdolMemberEntity, UUID> {
    fun findAllByGroupIdAndStatusAndDeletedAtIsNullOrderBySortOrderAsc(
        groupId: UUID,
        status: String,
    ): List<IdolMemberEntity>

    fun findAllByGroupIdInAndDeletedAtIsNull(groupIds: Collection<UUID>): List<IdolMemberEntity>

    fun findBySlugAndDeletedAtIsNull(slug: String): IdolMemberEntity?

    fun findBySlugAndStatusAndDeletedAtIsNull(slug: String, status: String): IdolMemberEntity?
}

