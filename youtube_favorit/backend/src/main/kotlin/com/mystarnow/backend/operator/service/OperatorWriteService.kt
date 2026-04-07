package com.mystarnow.backend.operator.service

import com.mystarnow.backend.common.api.FreshnessStatus
import com.mystarnow.backend.common.error.BadRequestException
import com.mystarnow.backend.common.error.ResourceNotFoundException
import com.mystarnow.backend.idol.platform.youtube.IdolYouTubeSyncService
import com.mystarnow.backend.operator.api.ChannelCreateRequest
import com.mystarnow.backend.operator.api.ChannelUpdateRequest
import com.mystarnow.backend.operator.api.GroupCreateRequest
import com.mystarnow.backend.operator.api.GroupUpdateRequest
import com.mystarnow.backend.operator.api.MemberCreateRequest
import com.mystarnow.backend.operator.api.MemberUpdateRequest
import com.mystarnow.backend.operator.api.OperatorMutationResponse
import com.mystarnow.backend.operator.api.VideoCreateRequest
import com.mystarnow.backend.operator.api.VideoUpdateRequest
import com.mystarnow.backend.persistence.entity.IdolGroupEntity
import com.mystarnow.backend.persistence.entity.IdolMemberEntity
import com.mystarnow.backend.persistence.entity.YouTubeChannelEntity
import com.mystarnow.backend.persistence.entity.YouTubeVideoEntity
import com.mystarnow.backend.persistence.repository.IdolGroupRepository
import com.mystarnow.backend.persistence.repository.IdolMemberRepository
import com.mystarnow.backend.persistence.repository.PlatformRepository
import com.mystarnow.backend.persistence.repository.YouTubeChannelRepository
import com.mystarnow.backend.persistence.repository.YouTubeVideoRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.net.URI
import java.util.UUID

@Service
class OperatorWriteService(
    private val groupRepository: IdolGroupRepository,
    private val memberRepository: IdolMemberRepository,
    private val channelRepository: YouTubeChannelRepository,
    private val videoRepository: YouTubeVideoRepository,
    private val platformRepository: PlatformRepository,
    private val idolYouTubeSyncService: IdolYouTubeSyncService,
) {
    @Transactional
    fun createGroup(request: GroupCreateRequest): OperatorMutationResponse {
        val slug = normalizeSlug(request.slug)
        if (groupRepository.findBySlugAndDeletedAtIsNull(slug) != null) {
            throw BadRequestException("Group slug '$slug' already exists")
        }
        val group = groupRepository.save(
            IdolGroupEntity(
                id = UUID.randomUUID(),
                slug = slug,
                displayName = normalizeRequiredText(request.displayName, "displayName"),
                normalizedName = normalizeName(request.displayName, slug),
                description = normalizeOptionalText(request.description),
                coverImageUrl = normalizeOptionalUrl(request.coverImageUrl, "coverImageUrl"),
                featured = request.featured,
                status = "active",
            ),
        )
        return OperatorMutationResponse("created", group.id.toString())
    }

    @Transactional
    fun updateGroup(groupSlug: String, request: GroupUpdateRequest): OperatorMutationResponse {
        val group = groupRepository.findBySlugAndDeletedAtIsNull(groupSlug)
            ?: throw ResourceNotFoundException("Group '$groupSlug' not found")
        request.displayName?.let {
            val name = normalizeRequiredText(it, "displayName")
            group.displayName = name
            group.normalizedName = normalizeName(name, group.slug)
        }
        request.description?.let { group.description = normalizeOptionalText(it) }
        request.coverImageUrl?.let { group.coverImageUrl = normalizeOptionalUrl(it, "coverImageUrl") }
        request.featured?.let { group.featured = it }
        request.status?.let { group.status = normalizeStatus(it) }
        groupRepository.save(group)
        return OperatorMutationResponse("updated", group.id.toString())
    }

    @Transactional
    fun createMember(request: MemberCreateRequest): OperatorMutationResponse {
        val groupId = parseUuid(request.groupId, "groupId")
        groupRepository.findById(groupId).orElseThrow {
            ResourceNotFoundException("Group '${request.groupId}' not found")
        }
        val slug = normalizeSlug(request.slug)
        if (memberRepository.findBySlugAndDeletedAtIsNull(slug) != null) {
            throw BadRequestException("Member slug '$slug' already exists")
        }
        val member = memberRepository.save(
            IdolMemberEntity(
                id = UUID.randomUUID(),
                groupId = groupId,
                slug = slug,
                displayName = normalizeRequiredText(request.displayName, "displayName"),
                normalizedName = normalizeName(request.displayName, slug),
                profileImageUrl = normalizeOptionalUrl(request.profileImageUrl, "profileImageUrl"),
                sortOrder = request.sortOrder ?: 0,
                status = "active",
            ),
        )
        return OperatorMutationResponse("created", member.id.toString())
    }

    @Transactional
    fun updateMember(memberSlug: String, request: MemberUpdateRequest): OperatorMutationResponse {
        val member = memberRepository.findBySlugAndDeletedAtIsNull(memberSlug)
            ?: throw ResourceNotFoundException("Member '$memberSlug' not found")
        request.displayName?.let {
            val name = normalizeRequiredText(it, "displayName")
            member.displayName = name
            member.normalizedName = normalizeName(name, member.slug)
        }
        request.profileImageUrl?.let { member.profileImageUrl = normalizeOptionalUrl(it, "profileImageUrl") }
        request.sortOrder?.let { member.sortOrder = it }
        request.status?.let { member.status = normalizeStatus(it) }
        memberRepository.save(member)
        return OperatorMutationResponse("updated", member.id.toString())
    }

    @Transactional
    fun createChannel(request: ChannelCreateRequest): OperatorMutationResponse {
        val platformCode = resolveEnabledPlatform(request.platformCode)
        val owner = resolveOwner(
            ownerType = request.ownerType,
            ownerGroupId = request.ownerGroupId,
            ownerMemberId = request.ownerMemberId,
        )
        val externalChannelId = normalizeRequiredText(request.externalChannelId, "externalChannelId")
        if (channelRepository.findByPlatformCodeAndExternalChannelIdAndDeletedAtIsNull(platformCode, externalChannelId) != null) {
            throw BadRequestException("Channel '$externalChannelId' already exists")
        }
        if (request.isPrimary) {
            demotePrimaryChannels(owner.ownerType, owner.ownerGroupId, owner.ownerMemberId, null)
        }
        val channel = channelRepository.save(
            YouTubeChannelEntity(
                id = UUID.randomUUID(),
                platformCode = platformCode,
                externalChannelId = externalChannelId,
                handle = normalizeOptionalHandle(request.handle),
                channelUrl = normalizeRequiredUrl(request.channelUrl, "channelUrl"),
                displayLabel = normalizeOptionalText(request.displayLabel),
                channelType = normalizeChannelType(request.channelType),
                ownerType = owner.ownerType,
                ownerGroupId = owner.ownerGroupId,
                ownerMemberId = owner.ownerMemberId,
                official = request.isOfficial,
                primary = request.isPrimary,
                status = "active",
            ),
        )
        if (channel.platformCode == "youtube") {
            idolYouTubeSyncService.syncChannel(channel.id)
        }
        return OperatorMutationResponse("created", channel.id.toString())
    }

    @Transactional
    fun updateChannel(channelId: String, request: ChannelUpdateRequest): OperatorMutationResponse {
        val uuid = parseUuid(channelId, "channelId")
        val channel = channelRepository.findById(uuid).orElseThrow {
            ResourceNotFoundException("Channel '$channelId' not found")
        }
        if (request.ownerType != null || request.ownerGroupId != null || request.ownerMemberId != null) {
            val owner = resolveOwner(
                ownerType = request.ownerType ?: channel.ownerType,
                ownerGroupId = request.ownerGroupId ?: channel.ownerGroupId?.toString(),
                ownerMemberId = request.ownerMemberId ?: channel.ownerMemberId?.toString(),
            )
            channel.ownerType = owner.ownerType
            channel.ownerGroupId = owner.ownerGroupId
            channel.ownerMemberId = owner.ownerMemberId
        }
        request.handle?.let { channel.handle = normalizeOptionalHandle(it) }
        request.channelUrl?.let { channel.channelUrl = normalizeRequiredUrl(it, "channelUrl") }
        request.displayLabel?.let { channel.displayLabel = normalizeOptionalText(it) }
        request.channelType?.let { channel.channelType = normalizeChannelType(it) }
        request.isOfficial?.let { channel.official = it }
        request.isPrimary?.let { newPrimary ->
            if (newPrimary) {
                demotePrimaryChannels(channel.ownerType, channel.ownerGroupId, channel.ownerMemberId, channel.id)
                channel.primary = true
            } else {
                channel.primary = false
            }
        }
        request.status?.let { channel.status = normalizeStatus(it) }
        channelRepository.save(channel)
        if (channel.platformCode == "youtube") {
            idolYouTubeSyncService.syncChannel(channel.id)
        }
        return OperatorMutationResponse("updated", channel.id.toString())
    }

    @Transactional
    fun createVideo(request: VideoCreateRequest): OperatorMutationResponse {
        val channelId = parseUuid(request.channelId, "channelId")
        channelRepository.findById(channelId).orElseThrow {
            ResourceNotFoundException("Channel '${request.channelId}' not found")
        }
        val externalVideoId = request.externalVideoId?.trim()?.ifBlank { null }
        if (externalVideoId != null && videoRepository.findByExternalVideoIdAndDeletedAtIsNull(externalVideoId) != null) {
            throw BadRequestException("Video '$externalVideoId' already exists")
        }
        val video = videoRepository.save(
            YouTubeVideoEntity(
                id = UUID.randomUUID(),
                channelId = channelId,
                externalVideoId = externalVideoId,
                title = normalizeRequiredText(request.title, "title"),
                description = normalizeOptionalText(request.description),
                thumbnailUrl = normalizeOptionalUrl(request.thumbnailUrl, "thumbnailUrl"),
                publishedAt = request.publishedAt,
                videoUrl = normalizeRequiredUrl(request.videoUrl, "videoUrl"),
                contentType = normalizeContentType(request.contentType),
                sourceType = "manual",
                freshnessStatus = FreshnessStatus.manual.name.lowercase(),
                pinned = request.pinned,
            ),
        )
        return OperatorMutationResponse("created", video.id.toString())
    }

    @Transactional
    fun updateVideo(videoId: String, request: VideoUpdateRequest): OperatorMutationResponse {
        val uuid = parseUuid(videoId, "videoId")
        val video = videoRepository.findById(uuid).orElseThrow {
            ResourceNotFoundException("Video '$videoId' not found")
        }
        request.title?.let { video.title = normalizeRequiredText(it, "title") }
        request.description?.let { video.description = normalizeOptionalText(it) }
        request.thumbnailUrl?.let { video.thumbnailUrl = normalizeOptionalUrl(it, "thumbnailUrl") }
        request.publishedAt?.let { video.publishedAt = it }
        request.videoUrl?.let { video.videoUrl = normalizeRequiredUrl(it, "videoUrl") }
        request.contentType?.let { video.contentType = normalizeContentType(it) }
        request.pinned?.let { video.pinned = it }
        videoRepository.save(video)
        return OperatorMutationResponse("updated", video.id.toString())
    }

    private fun resolveEnabledPlatform(platformCode: String): String =
        platformRepository.findById(platformCode.trim().lowercase())
            .orElseThrow { BadRequestException("Unknown platform '$platformCode'") }
            .also {
                if (!it.enabled) {
                    throw BadRequestException("Platform '${it.platformCode}' is not enabled")
                }
            }
            .platformCode

    private fun resolveOwner(
        ownerType: String,
        ownerGroupId: String?,
        ownerMemberId: String?,
    ): ChannelOwner {
        val normalizedOwnerType = normalizeOwnerType(ownerType)
        return when (normalizedOwnerType) {
            "GROUP" -> {
                val groupId = parseUuid(ownerGroupId ?: throw BadRequestException("ownerGroupId is required"), "ownerGroupId")
                groupRepository.findById(groupId).orElseThrow {
                    ResourceNotFoundException("Group '$ownerGroupId' not found")
                }
                ChannelOwner(normalizedOwnerType, groupId, null)
            }
            "MEMBER" -> {
                val memberId = parseUuid(ownerMemberId ?: throw BadRequestException("ownerMemberId is required"), "ownerMemberId")
                memberRepository.findById(memberId).orElseThrow {
                    ResourceNotFoundException("Member '$ownerMemberId' not found")
                }
                ChannelOwner(normalizedOwnerType, null, memberId)
            }
            else -> throw BadRequestException("Invalid ownerType")
        }
    }

    private fun demotePrimaryChannels(
        ownerType: String,
        ownerGroupId: UUID?,
        ownerMemberId: UUID?,
        currentChannelId: UUID?,
    ) {
        val channels = when (ownerType) {
            "GROUP" -> ownerGroupId?.let(channelRepository::findAllByOwnerGroupIdAndDeletedAtIsNull).orEmpty()
            "MEMBER" -> ownerMemberId?.let(channelRepository::findAllByOwnerMemberIdAndDeletedAtIsNull).orEmpty()
            else -> emptyList()
        }
        channels.filter { it.id != currentChannelId && it.primary }.forEach {
            it.primary = false
            channelRepository.save(it)
        }
    }

    private fun parseUuid(value: String, field: String): UUID =
        runCatching { UUID.fromString(value.trim()) }.getOrElse {
            throw BadRequestException("Invalid UUID for $field")
        }

    private fun normalizeSlug(value: String): String {
        val normalized = value.trim().lowercase().replace(Regex("[\\s_]+"), "-")
        if (!SLUG_PATTERN.matches(normalized)) {
            throw BadRequestException("slug must contain only lowercase letters, numbers, and hyphens")
        }
        return normalized
    }

    private fun normalizeName(displayName: String, slug: String): String =
        displayName.trim().lowercase().replace(NAME_NORMALIZATION_PATTERN, "").ifBlank { slug.replace("-", "") }

    private fun normalizeRequiredText(value: String, field: String): String =
        value.trim().ifBlank { throw BadRequestException("$field is required") }

    private fun normalizeOptionalText(value: String?): String? =
        value?.trim()?.ifBlank { null }

    private fun normalizeOptionalHandle(value: String?): String? {
        val normalized = value?.trim()?.trim('/')?.ifBlank { null } ?: return null
        return if (normalized.startsWith("@")) normalized else "@$normalized"
    }

    private fun normalizeRequiredUrl(value: String, field: String): String {
        val normalized = value.trim()
        if (normalized.isBlank()) {
            throw BadRequestException("$field is required")
        }
        validateUrl(normalized, field)
        return normalized
    }

    private fun normalizeOptionalUrl(value: String?, field: String): String? =
        value?.trim()?.ifBlank { null }?.also { validateUrl(it, field) }

    private fun validateUrl(value: String, field: String) {
        val uri = runCatching { URI(value) }.getOrElse {
            throw BadRequestException("$field must be a valid URL")
        }
        if (uri.scheme !in setOf("http", "https") || uri.host.isNullOrBlank()) {
            throw BadRequestException("$field must use http or https")
        }
    }

    private fun normalizeChannelType(value: String): String {
        val normalized = value.trim().uppercase()
        if (normalized !in CHANNEL_TYPES) {
            throw BadRequestException("Invalid channelType")
        }
        return normalized
    }

    private fun normalizeOwnerType(value: String): String {
        val normalized = value.trim().uppercase()
        if (normalized !in OWNER_TYPES) {
            throw BadRequestException("Invalid ownerType")
        }
        return normalized
    }

    private fun normalizeStatus(value: String): String {
        val normalized = value.trim().lowercase()
        if (normalized !in STATUSES) {
            throw BadRequestException("Invalid status")
        }
        return normalized
    }

    private fun normalizeContentType(value: String?): String {
        val normalized = value?.trim()?.lowercase()?.ifBlank { null } ?: "video"
        if (normalized !in CONTENT_TYPES) {
            throw BadRequestException("Invalid contentType")
        }
        return normalized
    }

    companion object {
        private val SLUG_PATTERN = Regex("^[\\p{L}\\p{N}][\\p{L}\\p{N}-]{0,119}$")
        private val NAME_NORMALIZATION_PATTERN = Regex("[^\\p{L}\\p{N}]")
        private val CHANNEL_TYPES = setOf("GROUP_OFFICIAL", "MEMBER_PERSONAL", "SUB_UNIT", "LABEL")
        private val OWNER_TYPES = setOf("GROUP", "MEMBER")
        private val STATUSES = setOf("active", "inactive", "hidden")
        private val CONTENT_TYPES = setOf("video", "short")
    }
}

private data class ChannelOwner(
    val ownerType: String,
    val ownerGroupId: UUID?,
    val ownerMemberId: UUID?,
)
