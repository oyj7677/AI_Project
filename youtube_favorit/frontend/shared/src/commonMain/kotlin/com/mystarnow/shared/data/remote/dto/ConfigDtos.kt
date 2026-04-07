package com.mystarnow.shared.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AppConfigPayloadDto(
    val runtime: SectionDto<RuntimeConfigDto>,
    val featureFlags: SectionDto<FeatureFlagsDto>,
)

@Serializable
data class RuntimeConfigDto(
    val minimumSupportedAppVersion: String,
    val defaultPageSize: Int,
    val maxPageSize: Int,
    val supportedPlatforms: List<SupportedPlatformDto>,
)

@Serializable
data class SupportedPlatformDto(
    val platform: String,
    val enabled: Boolean,
    val supportMode: String,
)

@Serializable
data class FeatureFlagsDto(
    val showSchedules: Boolean = true,
    val showRecentActivities: Boolean = true,
    val enableLiveNow: Boolean = true,
)
