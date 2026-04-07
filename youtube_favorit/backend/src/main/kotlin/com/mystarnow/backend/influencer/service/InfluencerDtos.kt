package com.mystarnow.backend.influencer.service

data class InfluencerListResponseData(
    val results: com.mystarnow.backend.common.api.SectionState<InfluencerResultsSectionData>,
    val filters: com.mystarnow.backend.common.api.SectionState<InfluencerFiltersSectionData>,
)

data class InfluencerResultsSectionData(
    val items: List<InfluencerListItem>,
    val pageInfo: PageInfo,
    val appliedFilters: AppliedFilters,
)

data class InfluencerListItem(
    val influencerId: String,
    val slug: String,
    val name: String,
    val summary: String?,
    val profileImageUrl: String?,
    val categories: List<String>,
    val platforms: List<String>,
    val liveStatus: String,
    val recentActivityAt: String?,
    val badges: List<String>,
)

data class PageInfo(
    val limit: Int,
    val nextCursor: String?,
    val hasNext: Boolean,
)

data class AppliedFilters(
    val q: String?,
    val category: String?,
    val platform: List<String>,
    val sort: String,
)

data class InfluencerFiltersSectionData(
    val categories: List<FilterCategoryItem>,
    val platforms: List<FilterPlatformItem>,
    val sortOptions: List<SortOptionItem>,
)

data class FilterCategoryItem(
    val value: String,
    val label: String,
    val count: Int,
)

data class FilterPlatformItem(
    val value: String,
    val label: String,
    val enabled: Boolean,
)

data class SortOptionItem(
    val value: String,
    val label: String,
)

data class InfluencerDetailResponseData(
    val profile: com.mystarnow.backend.common.api.SectionState<ProfileSectionData>,
    val channels: com.mystarnow.backend.common.api.SectionState<ChannelsSectionData>,
    val liveStatus: com.mystarnow.backend.common.api.SectionState<LiveStatusSectionData>,
    val recentActivities: com.mystarnow.backend.common.api.SectionState<RecentActivitiesSectionData>,
    val schedules: com.mystarnow.backend.common.api.SectionState<DetailSchedulesSectionData>,
    val detailMeta: com.mystarnow.backend.common.api.SectionState<DetailMetaSectionData>,
)

data class ProfileSectionData(
    val influencerId: String,
    val slug: String,
    val name: String,
    val bio: String?,
    val profileImageUrl: String?,
    val categories: List<String>,
    val isFeatured: Boolean,
)

data class ChannelsSectionData(
    val items: List<ChannelItemView>,
)

data class ChannelItemView(
    val platform: String,
    val handle: String?,
    val channelUrl: String,
    val isOfficial: Boolean,
    val isPrimary: Boolean,
)

data class LiveStatusSectionData(
    val isLive: Boolean,
    val platform: String?,
    val liveTitle: String?,
    val startedAt: String?,
    val watchUrl: String?,
)

data class RecentActivitiesSectionData(
    val items: List<DetailActivityItem>,
    val pageInfo: PageInfo,
)

data class DetailActivityItem(
    val activityId: String,
    val platform: String,
    val contentType: String,
    val title: String,
    val publishedAt: String,
    val externalUrl: String,
)

data class DetailSchedulesSectionData(
    val timezone: String,
    val items: List<DetailScheduleItemView>,
)

data class DetailScheduleItemView(
    val scheduleId: String,
    val influencerId: String,
    val title: String,
    val scheduledAt: String,
    val platform: String?,
    val note: String?,
)

data class DetailMetaSectionData(
    val relatedTags: List<String>,
    val supportedPlatforms: List<String>,
)
