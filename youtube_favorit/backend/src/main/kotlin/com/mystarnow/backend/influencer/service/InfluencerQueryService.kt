package com.mystarnow.backend.influencer.service

import com.mystarnow.backend.common.api.ApiResponse
import com.mystarnow.backend.common.api.FreshnessStatus
import com.mystarnow.backend.common.api.ResponseMeta
import com.mystarnow.backend.common.api.SectionStates
import com.mystarnow.backend.common.api.collectSectionErrors
import com.mystarnow.backend.common.api.hasPartialFailure
import com.mystarnow.backend.common.config.AppProperties
import com.mystarnow.backend.common.error.BadRequestException
import com.mystarnow.backend.common.error.ResourceNotFoundException
import com.mystarnow.backend.common.web.CursorCodec
import com.mystarnow.backend.persistence.readmodel.InfluencerAggregate
import com.mystarnow.backend.persistence.readmodel.InfluencerReadModelRepository
import com.mystarnow.backend.platform.integration.SyncScopes
import com.mystarnow.backend.platform.sync.SectionDegradationService
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId

@Service
class InfluencerQueryService(
    private val readModelRepository: InfluencerReadModelRepository,
    private val appProperties: AppProperties,
    private val sectionDegradationService: SectionDegradationService,
    private val clock: Clock,
) {
    fun getInfluencers(
        requestId: String,
        q: String?,
        category: String?,
        platforms: List<String>?,
        sort: String,
        cursor: String?,
        limit: Int,
    ): ApiResponse<InfluencerListResponseData> {
        validateSort(sort)
        if (limit !in 1..50) {
            throw BadRequestException("The query parameter 'limit' must be between 1 and 50.")
        }
        validatePlatforms(platforms.orEmpty())

        val now = OffsetDateTime.now(clock)
        val activeInfluencers = readModelRepository.loadActiveInfluencers()
        val filtered = activeInfluencers
            .filterBySearch(q)
            .filterByCategory(category)
            .filterByPlatforms(platforms.orEmpty())
            .sortedWith(sortComparator(sort))

        val offset = CursorCodec.decodeOffset(cursor)
        val page = filtered.drop(offset).take(limit)
        val nextOffset = offset + page.size
        val nextCursor = if (nextOffset < filtered.size) CursorCodec.encodeOffset(nextOffset) else null

        val resultsSection = if (page.isEmpty()) {
            SectionStates.empty(
                data = InfluencerResultsSectionData(
                    items = emptyList(),
                    pageInfo = PageInfo(limit = limit, nextCursor = null, hasNext = false),
                    appliedFilters = AppliedFilters(
                        q = q,
                        category = category,
                        platform = platforms.orEmpty(),
                        sort = sort,
                    ),
                ),
                generatedAt = now,
            )
        } else {
            SectionStates.success(
                data = InfluencerResultsSectionData(
                    items = page.map { it.toListItem() },
                    pageInfo = PageInfo(limit = limit, nextCursor = nextCursor, hasNext = nextCursor != null),
                    appliedFilters = AppliedFilters(
                        q = q,
                        category = category,
                        platform = platforms.orEmpty(),
                        sort = sort,
                    ),
                ),
                generatedAt = now,
            )
        }

        val filterSection = SectionStates.success(
            data = buildFilterSection(activeInfluencers),
            generatedAt = now,
            freshness = FreshnessStatus.manual,
        )

        return ApiResponse(
            meta = ResponseMeta(
                requestId = requestId,
                generatedAt = now,
                partialFailure = hasPartialFailure(resultsSection, filterSection),
            ),
            data = InfluencerListResponseData(
                results = resultsSection,
                filters = filterSection,
            ),
            errors = collectSectionErrors(
                "results" to resultsSection,
                "filters" to filterSection,
            ),
        )
    }

    fun getInfluencerDetail(
        requestId: String,
        slug: String,
        timezone: String?,
        activitiesLimit: Int,
        schedulesLimit: Int,
    ): ApiResponse<InfluencerDetailResponseData> {
        if (activitiesLimit !in 1..50) {
            throw BadRequestException("The query parameter 'activitiesLimit' must be between 1 and 50.")
        }
        if (schedulesLimit !in 1..20) {
            throw BadRequestException("The query parameter 'schedulesLimit' must be between 1 and 20.")
        }

        val aggregate = readModelRepository.loadInfluencerBySlug(slug)
            ?: throw ResourceNotFoundException("No influencer exists for slug '$slug'.")
        val now = OffsetDateTime.now(clock)
        val zoneId = ZoneId.of(timezone ?: defaultTimezone())
        val youtubeChannelIds = aggregate.channels.filter { it.platform == "youtube" }.map { it.id }

        val profile = SectionStates.success(
            data = ProfileSectionData(
                influencerId = aggregate.id.toString(),
                slug = aggregate.slug,
                name = aggregate.displayName,
                bio = aggregate.bio,
                profileImageUrl = aggregate.profileImageUrl,
                categories = aggregate.categories.map { it.code },
                isFeatured = aggregate.featured,
            ),
            generatedAt = now,
            freshness = FreshnessStatus.manual,
        )
        val channels = sectionDegradationService.applyForChannels(
            sectionName = "channels",
            base = SectionStates.success(
            data = ChannelsSectionData(
                items = aggregate.channels.map {
                    ChannelItemView(
                        platform = it.platform,
                        handle = it.handle,
                        channelUrl = it.channelUrl,
                        isOfficial = it.isOfficial,
                        isPrimary = it.isPrimary,
                    )
                },
            ),
                generatedAt = now,
                freshness = FreshnessStatus.manual,
            ),
            platformCode = "youtube",
            resourceScope = SyncScopes.CHANNEL_PROFILE,
            channelIds = youtubeChannelIds,
        )
        val activeLive = aggregate.liveStatuses.firstOrNull { it.isLive }
        val liveStatus = sectionDegradationService.applyForChannels(
            sectionName = "liveStatus",
            base = SectionStates.success(
                data = LiveStatusSectionData(
                    isLive = activeLive != null,
                    platform = activeLive?.platform,
                    liveTitle = activeLive?.liveTitle,
                    startedAt = activeLive?.startedAt?.toString(),
                    watchUrl = activeLive?.watchUrl,
                ),
                generatedAt = now,
                freshness = if (activeLive == null) FreshnessStatus.unknown else activeLive.freshnessStatus.toFreshnessStatus(),
            ),
            platformCode = "youtube",
            resourceScope = SyncScopes.CHANNEL_LIVE,
            channelIds = youtubeChannelIds,
        )
        val recentActivities = aggregate.recentActivities
            .sortedByDescending { it.publishedAt }
            .take(activitiesLimit)
        val activitiesSection = sectionDegradationService.applyForChannels(
            sectionName = "recentActivities",
            base = if (recentActivities.isEmpty()) {
            SectionStates.empty(
                data = RecentActivitiesSectionData(
                    items = emptyList(),
                    pageInfo = PageInfo(limit = activitiesLimit, nextCursor = null, hasNext = false),
                ),
                generatedAt = now,
            )
        } else {
            SectionStates.success(
                data = RecentActivitiesSectionData(
                    items = recentActivities.map {
                        DetailActivityItem(
                            activityId = it.id.toString(),
                            platform = it.platform,
                            contentType = it.contentType,
                            title = it.title,
                            publishedAt = it.publishedAt.toString(),
                            externalUrl = it.externalUrl,
                        )
                    },
                    pageInfo = PageInfo(limit = activitiesLimit, nextCursor = null, hasNext = false),
                ),
                generatedAt = now,
                freshness = recentActivities.firstOrNull()?.freshnessStatus.toFreshnessStatus(),
            )
            },
            platformCode = "youtube",
            resourceScope = SyncScopes.CHANNEL_ACTIVITY,
            channelIds = youtubeChannelIds,
        )
        val futureSchedules = aggregate.schedules
            .filter { it.scheduledAt.isAfter(now.minusMinutes(1)) }
            .sortedBy { it.scheduledAt }
            .take(schedulesLimit)
        val schedulesSection = if (futureSchedules.isEmpty()) {
            SectionStates.empty(
                data = DetailSchedulesSectionData(
                    timezone = zoneId.id,
                    items = emptyList(),
                ),
                generatedAt = now,
            )
        } else {
            SectionStates.success(
                data = DetailSchedulesSectionData(
                    timezone = zoneId.id,
                    items = futureSchedules.map {
                        DetailScheduleItemView(
                            scheduleId = it.id.toString(),
                            influencerId = it.influencerId.toString(),
                            title = it.title,
                            scheduledAt = it.scheduledAt.toString(),
                            platform = it.platform,
                            note = it.note,
                        )
                    },
                ),
                generatedAt = now,
                freshness = FreshnessStatus.manual,
            )
        }
        val detailMeta = SectionStates.success(
            data = DetailMetaSectionData(
                relatedTags = aggregate.categories.map { it.code },
                supportedPlatforms = appProperties.support.platforms.filter { it.enabled }.map { it.platform },
            ),
            generatedAt = now,
            freshness = FreshnessStatus.manual,
        )

        return ApiResponse(
            meta = ResponseMeta(
                requestId = requestId,
                generatedAt = now,
                partialFailure = hasPartialFailure(profile, channels, liveStatus, activitiesSection, schedulesSection, detailMeta),
            ),
            data = InfluencerDetailResponseData(
                profile = profile,
                channels = channels,
                liveStatus = liveStatus,
                recentActivities = activitiesSection,
                schedules = schedulesSection,
                detailMeta = detailMeta,
            ),
            errors = collectSectionErrors(
                "profile" to profile,
                "channels" to channels,
                "liveStatus" to liveStatus,
                "recentActivities" to activitiesSection,
                "schedules" to schedulesSection,
                "detailMeta" to detailMeta,
            ),
        )
    }

    private fun buildFilterSection(activeInfluencers: List<InfluencerAggregate>): InfluencerFiltersSectionData {
        val categories = activeInfluencers
            .flatMap { influencer -> influencer.categories.map { it.code to it.displayName } }
            .groupBy({ it.first }, { it.second })
            .map { (code, labels) ->
                FilterCategoryItem(
                    value = code,
                    label = labels.first(),
                    count = activeInfluencers.count { influencer -> influencer.categories.any { it.code == code } },
                )
            }
            .sortedBy { it.label }

        val platforms = appProperties.support.platforms.map {
            FilterPlatformItem(
                value = it.platform,
                label = when (it.platform) {
                    "youtube" -> "YouTube"
                    "instagram" -> "Instagram"
                    "x" -> "X"
                    "chzzk" -> "CHZZK"
                    "soop" -> "SOOP"
                    else -> it.platform
                },
                enabled = it.enabled,
            )
        }

        return InfluencerFiltersSectionData(
            categories = categories,
            platforms = platforms,
            sortOptions = listOf(
                SortOptionItem("featured", "추천순"),
                SortOptionItem("recent_activity", "최근 활동순"),
                SortOptionItem("name", "이름순"),
            ),
        )
    }

    private fun validateSort(sort: String) {
        if (sort !in setOf("featured", "recent_activity", "name")) {
            throw BadRequestException("Invalid sort")
        }
    }

    private fun validatePlatforms(platforms: List<String>) {
        val allowed = setOf("youtube", "instagram", "x", "chzzk", "soop")
        if (platforms.any { it !in allowed }) {
            throw BadRequestException("Invalid platform filter")
        }
    }

    private fun List<InfluencerAggregate>.filterBySearch(q: String?): List<InfluencerAggregate> {
        if (q.isNullOrBlank()) {
            return this
        }
        val keyword = q.trim().lowercase()
        return filter {
            it.displayName.lowercase().contains(keyword) ||
                it.slug.lowercase().contains(keyword) ||
                (it.bio?.lowercase()?.contains(keyword) == true)
        }
    }

    private fun List<InfluencerAggregate>.filterByCategory(category: String?): List<InfluencerAggregate> {
        if (category.isNullOrBlank()) {
            return this
        }
        return filter { influencer -> influencer.categories.any { it.code == category } }
    }

    private fun List<InfluencerAggregate>.filterByPlatforms(platforms: List<String>): List<InfluencerAggregate> {
        if (platforms.isEmpty()) {
            return this
        }
        return filter { influencer -> influencer.channels.any { it.platform in platforms } }
    }

    private fun sortComparator(sort: String): Comparator<InfluencerAggregate> = when (sort) {
        "recent_activity" -> compareByDescending<InfluencerAggregate> { it.latestActivityAt }
            .thenBy { it.displayName }
        "name" -> compareBy<InfluencerAggregate> { it.displayName }
        else -> compareByDescending<InfluencerAggregate> { it.featured }
            .thenByDescending { it.liveNow }
            .thenByDescending { it.latestActivityAt }
            .thenBy { it.displayName }
    }

    private fun InfluencerAggregate.toListItem(): InfluencerListItem = InfluencerListItem(
        influencerId = id.toString(),
        slug = slug,
        name = displayName,
        summary = bio,
        profileImageUrl = profileImageUrl,
        categories = categories.map { it.code },
        platforms = channels.map { it.platform }.distinct(),
        liveStatus = if (liveNow) "live" else "offline",
        recentActivityAt = latestActivityAt?.toString(),
        badges = buildList {
            if (featured) add("featured")
        },
    )

    private fun defaultTimezone(): String =
        appProperties.timezone.ifBlank { "Asia/Seoul" }
}

private fun String?.toFreshnessStatus(): FreshnessStatus = when (this?.lowercase()) {
    "fresh" -> FreshnessStatus.fresh
    "stale" -> FreshnessStatus.stale
    "manual" -> FreshnessStatus.manual
    else -> FreshnessStatus.unknown
}
