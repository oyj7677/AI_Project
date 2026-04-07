package com.mystarnow.shared.data.remote

import com.mystarnow.shared.core.model.ApiError
import com.mystarnow.shared.core.model.Freshness
import com.mystarnow.shared.core.model.ResponseMeta
import com.mystarnow.shared.core.model.SectionError
import com.mystarnow.shared.core.model.SectionModel
import com.mystarnow.shared.core.model.SectionStatus
import com.mystarnow.shared.data.remote.dto.ActivityItemDto
import com.mystarnow.shared.data.remote.dto.ActivitySectionDto
import com.mystarnow.shared.data.remote.dto.ApiEnvelopeDto
import com.mystarnow.shared.data.remote.dto.AppConfigPayloadDto
import com.mystarnow.shared.data.remote.dto.ChannelDto
import com.mystarnow.shared.data.remote.dto.ChannelSectionDto
import com.mystarnow.shared.data.remote.dto.DetailMetaDto
import com.mystarnow.shared.data.remote.dto.FeatureFlagsDto
import com.mystarnow.shared.data.remote.dto.FeaturedSectionDto
import com.mystarnow.shared.data.remote.dto.FilterOptionDto
import com.mystarnow.shared.data.remote.dto.FilterOptionsDto
import com.mystarnow.shared.data.remote.dto.HomePayloadDto
import com.mystarnow.shared.data.remote.dto.InfluencerDetailPayloadDto
import com.mystarnow.shared.data.remote.dto.InfluencerListPayloadDto
import com.mystarnow.shared.data.remote.dto.InfluencerProfileDto
import com.mystarnow.shared.data.remote.dto.InfluencerResultsDto
import com.mystarnow.shared.data.remote.dto.InfluencerSummaryDto
import com.mystarnow.shared.data.remote.dto.LiveNowCardDto
import com.mystarnow.shared.data.remote.dto.LiveNowSectionDto
import com.mystarnow.shared.data.remote.dto.LiveStatusDto
import com.mystarnow.shared.data.remote.dto.RuntimeConfigDto
import com.mystarnow.shared.data.remote.dto.ScheduleItemDto
import com.mystarnow.shared.data.remote.dto.ScheduleSectionDto
import com.mystarnow.shared.data.remote.dto.SectionDto
import com.mystarnow.shared.domain.model.ActivityItem
import com.mystarnow.shared.domain.model.ActivitySection
import com.mystarnow.shared.domain.model.AppliedFilters
import com.mystarnow.shared.domain.model.AppConfig
import com.mystarnow.shared.domain.model.Channel
import com.mystarnow.shared.domain.model.DetailMeta
import com.mystarnow.shared.domain.model.FeatureFlags
import com.mystarnow.shared.domain.model.FeaturedSection
import com.mystarnow.shared.domain.model.FilterOption
import com.mystarnow.shared.domain.model.FilterOptions
import com.mystarnow.shared.domain.model.HomeFeed
import com.mystarnow.shared.domain.model.InfluencerDetail
import com.mystarnow.shared.domain.model.InfluencerListPage
import com.mystarnow.shared.domain.model.InfluencerProfile
import com.mystarnow.shared.domain.model.InfluencerResults
import com.mystarnow.shared.domain.model.InfluencerSummary
import com.mystarnow.shared.domain.model.LiveNowCard
import com.mystarnow.shared.domain.model.LiveNowSection
import com.mystarnow.shared.domain.model.LiveStatus
import com.mystarnow.shared.domain.model.PageInfo
import com.mystarnow.shared.domain.model.Platform
import com.mystarnow.shared.domain.model.RuntimeConfig
import com.mystarnow.shared.domain.model.ScheduleItem
import com.mystarnow.shared.domain.model.ScheduleSection
import com.mystarnow.shared.domain.model.SupportedPlatform

private fun ResponseMeta.toMeta() = this

private fun com.mystarnow.shared.data.remote.dto.ResponseMetaDto.toDomain(): ResponseMeta = ResponseMeta(
    requestId = requestId,
    apiVersion = apiVersion,
    generatedAt = generatedAt,
    partialFailure = partialFailure,
)

private fun com.mystarnow.shared.data.remote.dto.ApiErrorDto.toDomain(): ApiError = ApiError(
    scope = scope,
    section = section,
    code = code,
    message = message,
)

private fun com.mystarnow.shared.data.remote.dto.SectionErrorDto.toDomain(): SectionError = SectionError(
    code = code,
    message = message,
    retryable = retryable,
    source = source,
)

private fun <T, R> SectionDto<T>.toDomain(mapper: (T) -> R): SectionModel<R> = SectionModel(
    status = SectionStatus.fromApi(status),
    freshness = Freshness.fromApi(freshness),
    generatedAt = generatedAt,
    staleAt = staleAt,
    data = mapper(data),
    error = error?.toDomain(),
)

private fun LiveNowCardDto.toDomain(): LiveNowCard = LiveNowCard(
    influencerId = influencerId,
    slug = slug,
    name = name,
    profileImageUrl = profileImageUrl,
    platform = Platform.fromApi(platform),
    liveTitle = liveTitle,
    startedAt = startedAt,
    watchUrl = watchUrl,
)

private fun LiveNowSectionDto.toDomain(): LiveNowSection = LiveNowSection(
    items = items.map { it.toDomain() },
    total = total,
)

private fun InfluencerSummaryDto.toDomain(): InfluencerSummary = InfluencerSummary(
    influencerId = influencerId,
    slug = slug,
    name = name,
    summary = summary,
    profileImageUrl = profileImageUrl,
    categories = categories,
    platforms = platforms.map { Platform.fromApi(it) },
    liveStatus = liveStatus,
    recentActivityAt = recentActivityAt,
    badges = badges,
)

private fun FeaturedSectionDto.toDomain(): FeaturedSection = FeaturedSection(
    items = items.map { it.toDomain() },
)

private fun ActivityItemDto.toDomain(): ActivityItem = ActivityItem(
    activityId = activityId,
    influencerId = influencerId,
    platform = Platform.fromApi(platform),
    contentType = contentType,
    title = title,
    publishedAt = publishedAt,
    thumbnailUrl = thumbnailUrl,
    externalUrl = externalUrl,
    summary = summary,
)

private fun ActivitySectionDto.toDomain(): ActivitySection = ActivitySection(
    items = items.map { it.toDomain() },
    pageInfo = pageInfo?.toDomain(),
)

private fun ScheduleItemDto.toDomain(): ScheduleItem = ScheduleItem(
    scheduleId = scheduleId,
    title = title,
    scheduledAt = scheduledAt,
    platform = platform?.let { Platform.fromApi(it) },
    note = note,
)

private fun ScheduleSectionDto.toDomain(): ScheduleSection = ScheduleSection(
    timezone = timezone,
    date = date,
    items = items.map { it.toDomain() },
)

private fun com.mystarnow.shared.data.remote.dto.PageInfoDto.toDomain(): PageInfo = PageInfo(
    limit = limit,
    nextCursor = nextCursor,
    hasNext = hasNext,
)

private fun com.mystarnow.shared.data.remote.dto.AppliedFiltersDto.toDomain(): AppliedFilters = AppliedFilters(
    query = q,
    category = category,
    platforms = platform.map { Platform.fromApi(it) },
    sort = sort,
)

private fun InfluencerResultsDto.toDomain(): InfluencerResults = InfluencerResults(
    items = items.map { it.toDomain() },
    pageInfo = pageInfo.toDomain(),
    appliedFilters = appliedFilters.toDomain(),
)

private fun FilterOptionDto.toDomain(): FilterOption = FilterOption(
    value = value,
    label = label,
    count = count,
    enabled = enabled,
)

private fun FilterOptionsDto.toDomain(): FilterOptions = FilterOptions(
    categories = categories.map { it.toDomain() },
    platforms = platforms.map { it.toDomain() },
    sortOptions = sortOptions.map { it.toDomain() },
)

private fun InfluencerProfileDto.toDomain(): InfluencerProfile = InfluencerProfile(
    influencerId = influencerId,
    slug = slug,
    name = name,
    bio = bio,
    profileImageUrl = profileImageUrl,
    categories = categories,
    isFeatured = isFeatured,
)

private fun ChannelDto.toDomain(): Channel = Channel(
    platform = Platform.fromApi(platform),
    handle = handle,
    channelUrl = channelUrl,
    isOfficial = isOfficial,
    isPrimary = isPrimary,
)

private fun ChannelSectionDto.toDomain(): List<Channel> = items.map { it.toDomain() }

private fun LiveStatusDto.toDomain(): LiveStatus = LiveStatus(
    isLive = isLive,
    platform = platform?.let { Platform.fromApi(it) },
    liveTitle = liveTitle,
    startedAt = startedAt,
    watchUrl = watchUrl,
)

private fun DetailMetaDto.toDomain(): DetailMeta = DetailMeta(
    relatedTags = relatedTags,
    supportedPlatforms = supportedPlatforms.map { Platform.fromApi(it) },
)

private fun RuntimeConfigDto.toDomain(): RuntimeConfig = RuntimeConfig(
    minimumSupportedAppVersion = minimumSupportedAppVersion,
    defaultPageSize = defaultPageSize,
    maxPageSize = maxPageSize,
    supportedPlatforms = supportedPlatforms.map {
        SupportedPlatform(
            platform = Platform.fromApi(it.platform),
            enabled = it.enabled,
            supportMode = it.supportMode,
        )
    },
)

private fun FeatureFlagsDto.toDomain(): FeatureFlags = FeatureFlags(
    showSchedules = showSchedules,
    showRecentActivities = showRecentActivities,
    enableLiveNow = enableLiveNow,
)

fun ApiEnvelopeDto<HomePayloadDto>.toDomain(): HomeFeed = HomeFeed(
    meta = meta.toDomain(),
    errors = errors.map { it.toDomain() },
    liveNow = data!!.liveNow.toDomain { it.toDomain() },
    latestUpdates = data.latestUpdates.toDomain { it.toDomain() },
    todaySchedules = data.todaySchedules.toDomain { it.toDomain() },
    featuredInfluencers = data.featuredInfluencers.toDomain { it.toDomain() },
)

fun ApiEnvelopeDto<InfluencerListPayloadDto>.toInfluencerListDomain(): InfluencerListPage = InfluencerListPage(
    meta = meta.toDomain(),
    errors = errors.map { it.toDomain() },
    results = data!!.results.toDomain { it.toDomain() },
    filters = data.filters.toDomain { it.toDomain() },
)

fun ApiEnvelopeDto<InfluencerDetailPayloadDto>.toInfluencerDetailDomain(): InfluencerDetail = InfluencerDetail(
    meta = meta.toDomain(),
    errors = errors.map { it.toDomain() },
    profile = data!!.profile.toDomain { it.toDomain() },
    channels = data.channels.toDomain { it.toDomain() },
    liveStatus = data.liveStatus.toDomain { it.toDomain() },
    recentActivities = data.recentActivities.toDomain { it.toDomain() },
    schedules = data.schedules.toDomain { it.toDomain() },
    detailMeta = data.detailMeta.toDomain { it.toDomain() },
)

fun ApiEnvelopeDto<AppConfigPayloadDto>.toAppConfigDomain(): AppConfig = AppConfig(
    meta = meta.toDomain(),
    errors = errors.map { it.toDomain() },
    runtime = data!!.runtime.toDomain { it.toDomain() },
    featureFlags = data.featureFlags.toDomain { it.toDomain() },
)
