package com.mystarnow.shared.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class InfluencerListPayloadDto(
    val results: SectionDto<InfluencerResultsDto>,
    val filters: SectionDto<FilterOptionsDto>,
)

@Serializable
data class InfluencerResultsDto(
    val items: List<InfluencerSummaryDto>,
    val pageInfo: PageInfoDto,
    val appliedFilters: AppliedFiltersDto,
)

@Serializable
data class InfluencerSummaryDto(
    val influencerId: String,
    val slug: String,
    val name: String,
    val summary: String,
    val profileImageUrl: String? = null,
    val categories: List<String> = emptyList(),
    val platforms: List<String> = emptyList(),
    val liveStatus: String = "offline",
    val recentActivityAt: String? = null,
    val badges: List<String> = emptyList(),
)

@Serializable
data class PageInfoDto(
    val limit: Int,
    val nextCursor: String? = null,
    val hasNext: Boolean,
)

@Serializable
data class AppliedFiltersDto(
    val q: String? = null,
    val category: String? = null,
    val platform: List<String> = emptyList(),
    val sort: String = "featured",
)

@Serializable
data class FilterOptionsDto(
    val categories: List<FilterOptionDto>,
    val platforms: List<FilterOptionDto>,
    val sortOptions: List<FilterOptionDto>,
)

@Serializable
data class FilterOptionDto(
    val value: String,
    val label: String,
    val count: Int? = null,
    val enabled: Boolean = true,
)
