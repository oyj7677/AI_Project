package com.mystarnow.shared.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class InfluencerDetailPayloadDto(
    val profile: SectionDto<InfluencerProfileDto>,
    val channels: SectionDto<ChannelSectionDto>,
    val liveStatus: SectionDto<LiveStatusDto>,
    val recentActivities: SectionDto<ActivitySectionDto>,
    val schedules: SectionDto<ScheduleSectionDto>,
    val detailMeta: SectionDto<DetailMetaDto>,
)

@Serializable
data class InfluencerProfileDto(
    val influencerId: String,
    val slug: String,
    val name: String,
    val bio: String? = null,
    val profileImageUrl: String? = null,
    val categories: List<String> = emptyList(),
    val isFeatured: Boolean = false,
)

@Serializable
data class ChannelSectionDto(
    val items: List<ChannelDto>,
)

@Serializable
data class ChannelDto(
    val platform: String,
    val handle: String? = null,
    val channelUrl: String,
    val isOfficial: Boolean,
    val isPrimary: Boolean,
)

@Serializable
data class LiveStatusDto(
    val isLive: Boolean,
    val platform: String? = null,
    val liveTitle: String? = null,
    val startedAt: String? = null,
    val watchUrl: String? = null,
)

@Serializable
data class ActivitySectionDto(
    val items: List<ActivityItemDto>,
    val pageInfo: PageInfoDto? = null,
)

@Serializable
data class ActivityItemDto(
    val activityId: String,
    val influencerId: String? = null,
    val platform: String,
    val contentType: String,
    val title: String,
    val publishedAt: String,
    val thumbnailUrl: String? = null,
    val externalUrl: String,
    val summary: String? = null,
)

@Serializable
data class ScheduleSectionDto(
    val timezone: String,
    val date: String? = null,
    val items: List<ScheduleItemDto>,
)

@Serializable
data class ScheduleItemDto(
    val scheduleId: String,
    val title: String,
    val scheduledAt: String,
    val platform: String? = null,
    val note: String? = null,
)

@Serializable
data class DetailMetaDto(
    val relatedTags: List<String> = emptyList(),
    val supportedPlatforms: List<String> = emptyList(),
)
