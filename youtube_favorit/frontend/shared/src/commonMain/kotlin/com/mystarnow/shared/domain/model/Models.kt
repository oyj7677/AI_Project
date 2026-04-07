package com.mystarnow.shared.domain.model

import com.mystarnow.shared.core.model.ResponseMeta
import com.mystarnow.shared.core.model.SectionModel

enum class Platform {
    YOUTUBE,
    INSTAGRAM,
    X,
    CHZZK,
    SOOP;

    companion object {
        fun fromApi(value: String): Platform = when (value.lowercase()) {
            "youtube" -> YOUTUBE
            "instagram" -> INSTAGRAM
            "x" -> X
            "chzzk" -> CHZZK
            else -> SOOP
        }
    }
}

data class InfluencerSummary(
    val influencerId: String,
    val slug: String,
    val name: String,
    val summary: String,
    val profileImageUrl: String?,
    val categories: List<String>,
    val platforms: List<Platform>,
    val liveStatus: String,
    val recentActivityAt: String?,
    val badges: List<String>,
)

data class Channel(
    val platform: Platform,
    val handle: String?,
    val channelUrl: String,
    val isOfficial: Boolean,
    val isPrimary: Boolean,
)

data class LiveStatus(
    val isLive: Boolean,
    val platform: Platform?,
    val liveTitle: String?,
    val startedAt: String?,
    val watchUrl: String?,
)

data class ActivityItem(
    val activityId: String,
    val influencerId: String?,
    val platform: Platform,
    val contentType: String,
    val title: String,
    val publishedAt: String,
    val thumbnailUrl: String?,
    val externalUrl: String,
    val summary: String?,
)

data class ScheduleItem(
    val scheduleId: String,
    val title: String,
    val scheduledAt: String,
    val platform: Platform?,
    val note: String?,
)

data class InfluencerProfile(
    val influencerId: String,
    val slug: String,
    val name: String,
    val bio: String?,
    val profileImageUrl: String?,
    val categories: List<String>,
    val isFeatured: Boolean,
)

data class LiveNowSection(
    val items: List<LiveNowCard>,
    val total: Int,
)

data class LiveNowCard(
    val influencerId: String,
    val slug: String,
    val name: String,
    val profileImageUrl: String?,
    val platform: Platform,
    val liveTitle: String,
    val startedAt: String?,
    val watchUrl: String?,
)

data class ActivitySection(
    val items: List<ActivityItem>,
    val pageInfo: PageInfo? = null,
)

data class ScheduleSection(
    val timezone: String,
    val date: String? = null,
    val items: List<ScheduleItem>,
)

data class FeaturedSection(
    val items: List<InfluencerSummary>,
)

data class HomeFeed(
    val meta: ResponseMeta,
    val errors: List<com.mystarnow.shared.core.model.ApiError>,
    val liveNow: SectionModel<LiveNowSection>,
    val latestUpdates: SectionModel<ActivitySection>,
    val todaySchedules: SectionModel<ScheduleSection>,
    val featuredInfluencers: SectionModel<FeaturedSection>,
)

data class PageInfo(
    val limit: Int,
    val nextCursor: String?,
    val hasNext: Boolean,
)

data class AppliedFilters(
    val query: String?,
    val category: String?,
    val platforms: List<Platform>,
    val sort: String,
)

data class FilterOption(
    val value: String,
    val label: String,
    val count: Int? = null,
    val enabled: Boolean = true,
)

data class FilterOptions(
    val categories: List<FilterOption>,
    val platforms: List<FilterOption>,
    val sortOptions: List<FilterOption>,
)

data class InfluencerResults(
    val items: List<InfluencerSummary>,
    val pageInfo: PageInfo,
    val appliedFilters: AppliedFilters,
)

data class InfluencerListPage(
    val meta: ResponseMeta,
    val errors: List<com.mystarnow.shared.core.model.ApiError>,
    val results: SectionModel<InfluencerResults>,
    val filters: SectionModel<FilterOptions>,
)

data class DetailMeta(
    val relatedTags: List<String>,
    val supportedPlatforms: List<Platform>,
)

data class InfluencerDetail(
    val meta: ResponseMeta,
    val errors: List<com.mystarnow.shared.core.model.ApiError>,
    val profile: SectionModel<InfluencerProfile>,
    val channels: SectionModel<List<Channel>>,
    val liveStatus: SectionModel<LiveStatus>,
    val recentActivities: SectionModel<ActivitySection>,
    val schedules: SectionModel<ScheduleSection>,
    val detailMeta: SectionModel<DetailMeta>,
)

data class SupportedPlatform(
    val platform: Platform,
    val enabled: Boolean,
    val supportMode: String,
)

data class RuntimeConfig(
    val minimumSupportedAppVersion: String,
    val defaultPageSize: Int,
    val maxPageSize: Int,
    val supportedPlatforms: List<SupportedPlatform>,
)

data class FeatureFlags(
    val showSchedules: Boolean,
    val showRecentActivities: Boolean,
    val enableLiveNow: Boolean,
)

data class AppConfig(
    val meta: ResponseMeta,
    val errors: List<com.mystarnow.shared.core.model.ApiError>,
    val runtime: SectionModel<RuntimeConfig>,
    val featureFlags: SectionModel<FeatureFlags>,
)

data class SearchInfluencersQuery(
    val query: String = "",
    val category: String? = null,
    val platforms: List<Platform> = emptyList(),
    val sort: String = "featured",
    val cursor: String? = null,
    val limit: Int = 20,
)
