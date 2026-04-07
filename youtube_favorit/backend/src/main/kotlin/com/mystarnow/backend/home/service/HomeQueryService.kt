package com.mystarnow.backend.home.service

import com.mystarnow.backend.common.api.ApiResponse
import com.mystarnow.backend.common.api.FreshnessStatus
import com.mystarnow.backend.common.api.ResponseMeta
import com.mystarnow.backend.common.api.SectionStates
import com.mystarnow.backend.common.api.collectSectionErrors
import com.mystarnow.backend.common.api.hasPartialFailure
import com.mystarnow.backend.common.error.BadRequestException
import com.mystarnow.backend.common.web.CursorCodec
import com.mystarnow.backend.idol.service.IdolCatalogReadSupport
import com.mystarnow.backend.idol.service.IdolViewMapper
import com.mystarnow.backend.idol.service.PageInfo
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.OffsetDateTime

@Service
class HomeQueryService(
    private val readSupport: IdolCatalogReadSupport,
    private val clock: Clock,
) {
    fun getHome(
        requestId: String,
        cursor: String?,
        limit: Int?,
        contentType: String?,
    ): ApiResponse<HomeResponseData> {
        val now = OffsetDateTime.now(clock)
        val aggregates = readSupport.loadActiveGroupAggregates()
        val resolvedLimit = resolveLimit(limit)
        val offset = CursorCodec.decodeOffset(cursor)
        val normalizedContentType = normalizeContentType(contentType)
        val videoItems = aggregates
            .flatMap { aggregate ->
                val channelById = (aggregate.officialChannels + aggregate.memberChannels).associateBy { it.id }
                val memberById = aggregate.members.associateBy { it.id }
                aggregate.videos.map { video ->
                    val channel = requireNotNull(channelById[video.channelId])
                    val member = channel.ownerMemberId?.let(memberById::get)
                    IdolViewMapper.toVideoFeedItem(video, channel, aggregate.group, member)
                }
            }
            .filter { normalizedContentType == null || it.contentType == normalizedContentType }
            .sortedByDescending { it.publishedAt }

        val page = videoItems.drop(offset).take(resolvedLimit)
        val nextOffset = offset + page.size
        val nextCursor = if (nextOffset < videoItems.size) CursorCodec.encodeOffset(nextOffset) else null

        val recentVideos = if (page.isEmpty()) {
            SectionStates.empty(
                HomeRecentVideosSectionData(
                    items = emptyList(),
                    pageInfo = PageInfo(resolvedLimit, nextCursor, nextCursor != null),
                ),
                now,
            )
        } else {
            SectionStates.success(
                HomeRecentVideosSectionData(
                    items = page,
                    pageInfo = PageInfo(resolvedLimit, nextCursor, nextCursor != null),
                ),
                now,
                FreshnessStatus.manual,
            )
        }

        val featuredGroups = aggregates
            .filter { it.group.featured }
            .sortedWith(
                compareByDescending<com.mystarnow.backend.idol.service.GroupAggregate> { it.latestVideoAt }
                    .thenBy { it.group.displayName },
            )
            .take(8)

        val featuredSection = if (featuredGroups.isEmpty()) {
            SectionStates.empty(HomeFeaturedGroupsSectionData(emptyList()), now)
        } else {
            SectionStates.success(
                HomeFeaturedGroupsSectionData(
                    items = featuredGroups.map {
                        HomeFeaturedGroupItemView(
                            groupId = it.group.id.toString(),
                            groupSlug = it.group.slug,
                            groupName = it.group.displayName,
                            coverImageUrl = it.group.coverImageUrl,
                            officialChannelCount = it.officialChannels.size,
                            memberPersonalChannelCount = it.memberChannels.size,
                            latestVideoAt = it.latestVideoAt?.toString(),
                        )
                    },
                ),
                now,
                FreshnessStatus.manual,
            )
        }

        return ApiResponse(
            meta = ResponseMeta(
                requestId = requestId,
                generatedAt = now,
                partialFailure = hasPartialFailure(recentVideos, featuredSection),
            ),
            data = HomeResponseData(
                recentVideos = recentVideos,
                featuredGroups = featuredSection,
            ),
            errors = collectSectionErrors(
                "recentVideos" to recentVideos,
                "featuredGroups" to featuredSection,
            ),
        )
    }

    private fun resolveLimit(limit: Int?): Int {
        val resolved = limit ?: 8
        if (resolved !in 1..20) {
            throw BadRequestException("The query parameter 'limit' must be between 1 and 20.")
        }
        return resolved
    }

    private fun normalizeContentType(contentType: String?): String? {
        val normalized = contentType?.trim()?.lowercase()?.takeIf { it.isNotBlank() }
        if (normalized == null || normalized == "all") {
            return null
        }
        if (normalized !in setOf("video", "short")) {
            throw BadRequestException("The query parameter 'contentType' must be one of all, video, short.")
        }
        return normalized
    }
}
