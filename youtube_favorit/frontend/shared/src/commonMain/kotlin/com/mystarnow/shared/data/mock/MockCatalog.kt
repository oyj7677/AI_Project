package com.mystarnow.shared.data.mock

import com.mystarnow.shared.data.remote.dto.ApiEnvelopeDto
import com.mystarnow.shared.data.remote.dto.AppliedFiltersDto
import com.mystarnow.shared.data.remote.dto.FeaturedSectionDto
import com.mystarnow.shared.data.remote.dto.FilterOptionDto
import com.mystarnow.shared.data.remote.dto.FilterOptionsDto
import com.mystarnow.shared.data.remote.dto.HomePayloadDto
import com.mystarnow.shared.data.remote.dto.ChannelDto
import com.mystarnow.shared.data.remote.dto.ChannelSectionDto
import com.mystarnow.shared.data.remote.dto.DetailMetaDto
import com.mystarnow.shared.data.remote.dto.InfluencerDetailPayloadDto
import com.mystarnow.shared.data.remote.dto.InfluencerListPayloadDto
import com.mystarnow.shared.data.remote.dto.InfluencerProfileDto
import com.mystarnow.shared.data.remote.dto.InfluencerResultsDto
import com.mystarnow.shared.data.remote.dto.InfluencerSummaryDto
import com.mystarnow.shared.data.remote.dto.LiveStatusDto
import com.mystarnow.shared.data.remote.dto.PageInfoDto
import com.mystarnow.shared.data.remote.dto.ScheduleItemDto
import com.mystarnow.shared.data.remote.dto.ScheduleSectionDto
import com.mystarnow.shared.data.remote.dto.SectionDto
import kotlinx.serialization.json.Json

internal object MockCatalog {
    private val json = Json { ignoreUnknownKeys = true }

    val homeEnvelope: ApiEnvelopeDto<HomePayloadDto> =
        json.decodeFromString(MockPayloads.HOME)

    private val haruDetailEnvelope = json.decodeFromString<com.mystarnow.shared.data.remote.dto.ApiEnvelopeDto<InfluencerDetailPayloadDto>>(MockPayloads.DETAIL)

    val configEnvelope = json.decodeFromString<com.mystarnow.shared.data.remote.dto.ApiEnvelopeDto<com.mystarnow.shared.data.remote.dto.AppConfigPayloadDto>>(MockPayloads.CONFIG)

    val influencers = listOf(
        InfluencerSummaryDto(
            influencerId = "inf_haru",
            slug = "haru",
            name = "하루",
            summary = "게임, 토크 중심 스트리머",
            profileImageUrl = null,
            categories = listOf("game", "talk"),
            platforms = listOf("youtube", "instagram"),
            liveStatus = "live",
            recentActivityAt = "2026-04-04T11:55:00Z",
            badges = listOf("featured"),
        ),
        InfluencerSummaryDto(
            influencerId = "inf_mina",
            slug = "mina",
            name = "미나",
            summary = "브이로그와 릴스 중심 크리에이터",
            profileImageUrl = null,
            categories = listOf("lifestyle"),
            platforms = listOf("instagram"),
            liveStatus = "offline",
            recentActivityAt = "2026-04-04T10:15:00Z",
            badges = emptyList(),
        ),
        InfluencerSummaryDto(
            influencerId = "inf_jun",
            slug = "jun",
            name = "준",
            summary = "게임 하이라이트와 편집 영상",
            profileImageUrl = null,
            categories = listOf("game"),
            platforms = listOf("youtube"),
            liveStatus = "scheduled",
            recentActivityAt = "2026-04-04T09:00:00Z",
            badges = emptyList(),
        ),
    )

    fun listEnvelope(
        query: String,
        category: String?,
        platforms: List<String>,
        sort: String,
        cursor: String?,
        limit: Int,
    ): ApiEnvelopeDto<InfluencerListPayloadDto> {
        val filtered = influencers
            .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) || it.summary.contains(query, ignoreCase = true) }
            .filter { category == null || it.categories.contains(category) }
            .filter { platforms.isEmpty() || it.platforms.any { platform -> platform in platforms } }
            .let { list ->
                when (sort) {
                    "name" -> list.sortedBy { it.name }
                    "recent_activity" -> list.sortedByDescending { it.recentActivityAt }
                    else -> list.sortedByDescending { it.badges.contains("featured") }
                }
            }

        val offset = cursor?.substringAfter("cursor_")?.toIntOrNull() ?: 0
        val pageItems = filtered.drop(offset).take(limit)
        val nextOffset = offset + pageItems.size
        val nextCursor = if (nextOffset < filtered.size) "cursor_$nextOffset" else null

        val payload = InfluencerListPayloadDto(
            results = com.mystarnow.shared.data.remote.dto.SectionDto(
                status = if (pageItems.isEmpty()) "empty" else "success",
                freshness = "fresh",
                generatedAt = "2026-04-04T12:00:00Z",
                staleAt = null,
                data = InfluencerResultsDto(
                    items = pageItems,
                    pageInfo = PageInfoDto(
                        limit = limit,
                        nextCursor = nextCursor,
                        hasNext = nextCursor != null,
                    ),
                    appliedFilters = AppliedFiltersDto(
                        q = query.ifBlank { null },
                        category = category,
                        platform = platforms,
                        sort = sort,
                    ),
                ),
                error = null,
            ),
            filters = com.mystarnow.shared.data.remote.dto.SectionDto(
                status = "success",
                freshness = "manual",
                generatedAt = "2026-04-04T11:50:00Z",
                staleAt = null,
                data = FilterOptionsDto(
                    categories = listOf(
                        FilterOptionDto("game", "게임", 2),
                        FilterOptionDto("talk", "토크", 1),
                        FilterOptionDto("lifestyle", "라이프스타일", 1),
                    ),
                    platforms = listOf(
                        FilterOptionDto("youtube", "YouTube", enabled = true),
                        FilterOptionDto("instagram", "Instagram", enabled = true),
                    ),
                    sortOptions = listOf(
                        FilterOptionDto("featured", "추천순"),
                        FilterOptionDto("recent_activity", "최근 활동순"),
                        FilterOptionDto("name", "이름순"),
                    ),
                ),
                error = null,
            ),
        )

        return ApiEnvelopeDto(
            meta = homeEnvelope.meta.copy(requestId = "req_list_mock", partialFailure = false),
            data = payload,
            errors = emptyList(),
        )
    }

    fun detailEnvelope(slug: String): ApiEnvelopeDto<InfluencerDetailPayloadDto> {
        if (slug == "haru") return haruDetailEnvelope

        val influencer = influencers.firstOrNull { it.slug == slug } ?: influencers.first()
        val payload = InfluencerDetailPayloadDto(
            profile = SectionDto(
                status = "success",
                freshness = "manual",
                generatedAt = "2026-04-04T11:50:00Z",
                staleAt = null,
                data = InfluencerProfileDto(
                    influencerId = influencer.influencerId,
                    slug = influencer.slug,
                    name = influencer.name,
                    bio = influencer.summary,
                    profileImageUrl = influencer.profileImageUrl,
                    categories = influencer.categories,
                    isFeatured = influencer.badges.contains("featured"),
                ),
                error = null,
            ),
            channels = SectionDto(
                status = "success",
                freshness = "manual",
                generatedAt = "2026-04-04T11:50:00Z",
                staleAt = null,
                data = ChannelSectionDto(
                    items = influencer.platforms.mapIndexed { index, platform ->
                        ChannelDto(
                            platform = platform,
                            handle = "@${influencer.slug}_${platform}",
                            channelUrl = "https://${platform}.com/${influencer.slug}",
                            isOfficial = true,
                            isPrimary = index == 0,
                        )
                    }
                ),
                error = null,
            ),
            liveStatus = SectionDto(
                status = "success",
                freshness = "fresh",
                generatedAt = "2026-04-04T11:59:30Z",
                staleAt = null,
                data = LiveStatusDto(
                    isLive = influencer.liveStatus == "live",
                    platform = influencer.platforms.firstOrNull(),
                    liveTitle = if (influencer.liveStatus == "live") "${influencer.name} 라이브" else null,
                    startedAt = if (influencer.liveStatus == "live") "2026-04-04T11:40:00Z" else null,
                    watchUrl = influencer.platforms.firstOrNull()?.let { "https://$it.com/${influencer.slug}" },
                ),
                error = null,
            ),
            recentActivities = SectionDto(
                status = "success",
                freshness = "manual",
                generatedAt = "2026-04-04T11:55:00Z",
                staleAt = null,
                data = haruDetailEnvelope.data!!.recentActivities.data.copy(
                    items = listOf(
                        haruDetailEnvelope.data.recentActivities.data.items.first().copy(
                            activityId = "act_${influencer.slug}",
                            influencerId = influencer.influencerId,
                            platform = influencer.platforms.first(),
                            title = "${influencer.name} 최근 업데이트",
                            externalUrl = "https://${influencer.platforms.first()}.com/${influencer.slug}",
                        )
                    )
                ),
                error = null,
            ),
            schedules = SectionDto(
                status = "success",
                freshness = "manual",
                generatedAt = "2026-04-04T11:50:00Z",
                staleAt = null,
                data = ScheduleSectionDto(
                    timezone = "Asia/Seoul",
                    date = null,
                    items = listOf(
                        ScheduleItemDto(
                            scheduleId = "sch_${influencer.slug}",
                            title = "${influencer.name} 예정 방송",
                            scheduledAt = "2026-04-05T11:00:00Z",
                            platform = influencer.platforms.firstOrNull(),
                            note = "mock schedule",
                        )
                    )
                ),
                error = null,
            ),
            detailMeta = SectionDto(
                status = "success",
                freshness = "manual",
                generatedAt = "2026-04-04T11:50:00Z",
                staleAt = null,
                data = DetailMetaDto(
                    relatedTags = influencer.categories,
                    supportedPlatforms = influencer.platforms,
                ),
                error = null,
            ),
        )

        return ApiEnvelopeDto(
            meta = homeEnvelope.meta.copy(requestId = "req_detail_${influencer.slug}", partialFailure = false),
            data = payload,
            errors = emptyList(),
        )
    }
}
