package com.mystarnow.backend.meta.service

data class AppConfigResponseData(
    val runtime: com.mystarnow.backend.common.api.SectionState<RuntimeConfigSectionData>,
    val featureFlags: com.mystarnow.backend.common.api.SectionState<FeatureFlagsSectionData>,
)

data class RuntimeConfigSectionData(
    val minimumSupportedAppVersion: String,
    val defaultPageSize: Int,
    val maxPageSize: Int,
    val supportedPlatforms: List<SupportedPlatformItem>,
)

data class SupportedPlatformItem(
    val platform: String,
    val enabled: Boolean,
    val supportMode: String,
)

data class FeatureFlagsSectionData(
    val showSchedules: Boolean,
    val showRecentActivities: Boolean,
    val enableLiveNow: Boolean,
    val showMemberDetail: Boolean,
    val showFeaturedGroups: Boolean,
    val showVideoFeed: Boolean,
)
