package com.mystarnow.backend.operator.api

import java.time.OffsetDateTime

data class GroupCreateRequest(
    val slug: String,
    val displayName: String,
    val description: String? = null,
    val coverImageUrl: String? = null,
    val featured: Boolean = false,
    val note: String? = null,
)

data class GroupUpdateRequest(
    val displayName: String? = null,
    val description: String? = null,
    val coverImageUrl: String? = null,
    val featured: Boolean? = null,
    val status: String? = null,
    val note: String? = null,
)

data class MemberCreateRequest(
    val groupId: String,
    val slug: String,
    val displayName: String,
    val profileImageUrl: String? = null,
    val sortOrder: Int? = null,
    val note: String? = null,
)

data class MemberUpdateRequest(
    val displayName: String? = null,
    val profileImageUrl: String? = null,
    val sortOrder: Int? = null,
    val status: String? = null,
    val note: String? = null,
)

data class ChannelCreateRequest(
    val platformCode: String,
    val externalChannelId: String,
    val handle: String? = null,
    val channelUrl: String,
    val displayLabel: String? = null,
    val channelType: String,
    val ownerType: String,
    val ownerGroupId: String? = null,
    val ownerMemberId: String? = null,
    val isOfficial: Boolean = true,
    val isPrimary: Boolean = false,
    val note: String? = null,
)

data class ChannelUpdateRequest(
    val handle: String? = null,
    val channelUrl: String? = null,
    val displayLabel: String? = null,
    val channelType: String? = null,
    val ownerType: String? = null,
    val ownerGroupId: String? = null,
    val ownerMemberId: String? = null,
    val isOfficial: Boolean? = null,
    val isPrimary: Boolean? = null,
    val status: String? = null,
    val note: String? = null,
)

data class VideoCreateRequest(
    val channelId: String,
    val externalVideoId: String? = null,
    val title: String,
    val description: String? = null,
    val thumbnailUrl: String? = null,
    val publishedAt: OffsetDateTime,
    val videoUrl: String,
    val contentType: String? = null,
    val pinned: Boolean = false,
    val note: String? = null,
)

data class VideoUpdateRequest(
    val title: String? = null,
    val description: String? = null,
    val thumbnailUrl: String? = null,
    val publishedAt: OffsetDateTime? = null,
    val videoUrl: String? = null,
    val contentType: String? = null,
    val pinned: Boolean? = null,
    val note: String? = null,
)

data class OperatorMutationResponse(
    val status: String,
    val entityId: String,
)
