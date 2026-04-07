package com.mystarnow.shared.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class HomePayloadDto(
    val liveNow: SectionDto<LiveNowSectionDto>,
    val latestUpdates: SectionDto<ActivitySectionDto>,
    val todaySchedules: SectionDto<ScheduleSectionDto>,
    val featuredInfluencers: SectionDto<FeaturedSectionDto>,
)

@Serializable
data class LiveNowSectionDto(
    val items: List<LiveNowCardDto>,
    val total: Int,
)

@Serializable
data class LiveNowCardDto(
    val influencerId: String,
    val slug: String,
    val name: String,
    val profileImageUrl: String? = null,
    val platform: String,
    val liveTitle: String,
    val startedAt: String? = null,
    val watchUrl: String? = null,
)

@Serializable
data class FeaturedSectionDto(
    val items: List<InfluencerSummaryDto>,
)
