package com.mystarnow.backend.idol.service

import com.mystarnow.backend.common.api.ApiResponse
import com.mystarnow.backend.common.api.FreshnessStatus
import com.mystarnow.backend.common.api.ResponseMeta
import com.mystarnow.backend.common.api.SectionStates
import com.mystarnow.backend.common.api.collectSectionErrors
import com.mystarnow.backend.common.api.hasPartialFailure
import com.mystarnow.backend.common.error.BadRequestException
import com.mystarnow.backend.common.error.ResourceNotFoundException
import com.mystarnow.backend.common.web.CursorCodec
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.OffsetDateTime
import java.util.UUID

@Service
class GroupQueryService(
    private val readSupport: IdolCatalogReadSupport,
    private val clock: Clock,
) {
    fun getGroups(
        requestId: String,
        q: String?,
        sort: String?,
        cursor: String?,
        limit: Int?,
    ): ApiResponse<GroupListResponseData> {
        val now = OffsetDateTime.now(clock)
        val resolvedSort = resolveSort(sort)
        val resolvedLimit = resolveLimit(limit, 50)
        val offset = CursorCodec.decodeOffset(cursor)

        val filtered = applyGroupQuery(readSupport.loadActiveGroupAggregates(), q, resolvedSort)
        val page = filtered.drop(offset).take(resolvedLimit)
        val nextOffset = offset + page.size
        val nextCursor = if (nextOffset < filtered.size) CursorCodec.encodeOffset(nextOffset) else null

        val results = if (page.isEmpty()) {
            SectionStates.empty(
                GroupResultsSectionData(
                    items = emptyList(),
                    pageInfo = PageInfo(resolvedLimit, nextCursor, nextCursor != null),
                    appliedFilters = GroupAppliedFilters(q = q?.takeIf { it.isNotBlank() }, sort = resolvedSort),
                ),
                now,
            )
        } else {
            SectionStates.success(
                GroupResultsSectionData(
                    items = page.map { aggregate ->
                        GroupListItemView(
                            groupId = aggregate.group.id.toString(),
                            groupSlug = aggregate.group.slug,
                            groupName = aggregate.group.displayName,
                            description = aggregate.group.description,
                            coverImageUrl = aggregate.group.coverImageUrl,
                            officialChannelCount = aggregate.officialChannels.size,
                            memberCount = aggregate.members.size,
                            memberPersonalChannelCount = aggregate.memberChannels.size,
                            latestVideoAt = aggregate.latestVideoAt?.toString(),
                            latestVideoThumbnailUrl = aggregate.videos.firstOrNull()?.thumbnailUrl,
                            badges = buildListBadges(aggregate),
                        )
                    },
                    pageInfo = PageInfo(resolvedLimit, nextCursor, nextCursor != null),
                    appliedFilters = GroupAppliedFilters(q = q?.takeIf { it.isNotBlank() }, sort = resolvedSort),
                ),
                now,
                FreshnessStatus.manual,
            )
        }

        val filters = SectionStates.success(
            GroupFiltersSectionData(
                sortOptions = listOf(
                    SortOptionItem("featured", "추천순"),
                    SortOptionItem("recent_video", "최근 업로드순"),
                    SortOptionItem("name", "이름순"),
                ),
            ),
            now,
            FreshnessStatus.manual,
        )

        return ApiResponse(
            meta = ResponseMeta(
                requestId = requestId,
                generatedAt = now,
                partialFailure = hasPartialFailure(results, filters),
            ),
            data = GroupListResponseData(
                results = results,
                filters = filters,
            ),
            errors = collectSectionErrors(
                "results" to results,
                "filters" to filters,
            ),
        )
    }

    fun getGroupDetail(
        requestId: String,
        groupSlug: String,
        videosCursor: String?,
        videosLimit: Int?,
        contentType: String?,
    ): ApiResponse<GroupDetailResponseData> {
        val now = OffsetDateTime.now(clock)
        val aggregate = readSupport.loadActiveGroupAggregateBySlug(groupSlug)
            ?: throw ResourceNotFoundException("No group exists for slug '$groupSlug'.")
        val resolvedLimit = resolveLimit(videosLimit, 20)
        val offset = CursorCodec.decodeOffset(videosCursor)
        val normalizedContentType = normalizeContentType(contentType)
        val filteredVideos = aggregate.videos.filter {
            normalizedContentType == null || it.contentType == normalizedContentType
        }
        val pagedVideos = filteredVideos.drop(offset).take(resolvedLimit)
        val nextOffset = offset + pagedVideos.size
        val nextCursor = if (nextOffset < filteredVideos.size) CursorCodec.encodeOffset(nextOffset) else null

        val channelById = (aggregate.officialChannels + aggregate.memberChannels).associateBy { it.id }
        val memberById = aggregate.members.associateBy { it.id }

        val groupHeader = SectionStates.success(
            GroupHeaderSectionData(
                groupId = aggregate.group.id.toString(),
                groupSlug = aggregate.group.slug,
                groupName = aggregate.group.displayName,
                description = aggregate.group.description,
                coverImageUrl = aggregate.group.coverImageUrl,
                memberCount = aggregate.members.size,
                officialChannelCount = aggregate.officialChannels.size,
                memberPersonalChannelCount = aggregate.memberChannels.size,
                latestVideoAt = aggregate.latestVideoAt?.toString(),
                isFeatured = aggregate.group.featured,
            ),
            now,
            FreshnessStatus.manual,
        )

        val officialChannels = if (aggregate.officialChannels.isEmpty()) {
            SectionStates.empty(GroupOfficialChannelsSectionData(emptyList()), now)
        } else {
            SectionStates.success(
                GroupOfficialChannelsSectionData(
                    aggregate.officialChannels.map { channel ->
                        GroupOfficialChannelItemView(
                            channelId = channel.id.toString(),
                            externalChannelId = channel.externalChannelId,
                            channelName = channel.displayLabel ?: channel.externalChannelId,
                            handle = channel.handle,
                            channelUrl = channel.channelUrl,
                            channelType = channel.channelType,
                            isOfficial = channel.official,
                            latestVideoAt = aggregate.videos.firstOrNull { it.channelId == channel.id }?.publishedAt?.toString(),
                        )
                    },
                ),
                now,
                FreshnessStatus.manual,
            )
        }

        val members = if (aggregate.members.isEmpty()) {
            SectionStates.empty(GroupMembersSectionData(emptyList()), now)
        } else {
            SectionStates.success(
                GroupMembersSectionData(
                    aggregate.members.map { member ->
                        val channels = aggregate.memberChannels.filter { it.ownerMemberId == member.id }
                        val latestVideoAt = aggregate.videos.firstOrNull { video ->
                            channels.any { it.id == video.channelId }
                        }?.publishedAt
                        GroupMemberItemView(
                            memberId = member.id.toString(),
                            memberSlug = member.slug,
                            memberName = member.displayName,
                            profileImageUrl = member.profileImageUrl,
                            hasPersonalChannel = channels.isNotEmpty(),
                            personalChannelCount = channels.size,
                            latestVideoAt = latestVideoAt?.toString(),
                        )
                    },
                ),
                now,
                FreshnessStatus.manual,
            )
        }

        val recentVideos = if (pagedVideos.isEmpty()) {
            SectionStates.empty(
                GroupRecentVideosSectionData(
                    items = emptyList(),
                    pageInfo = PageInfo(resolvedLimit, nextCursor, nextCursor != null),
                ),
                now,
            )
        } else {
            SectionStates.success(
                GroupRecentVideosSectionData(
                    items = pagedVideos.map { video ->
                        val channel = requireNotNull(channelById[video.channelId])
                        val member = channel.ownerMemberId?.let(memberById::get)
                        IdolViewMapper.toVideoFeedItem(video, channel, aggregate.group, member)
                    },
                    pageInfo = PageInfo(resolvedLimit, nextCursor, nextCursor != null),
                ),
                now,
                pagedVideos.firstOrNull()?.freshnessStatus.toFreshnessStatus(),
            )
        }

        val detailMeta = SectionStates.success(
            GroupDetailMetaSectionData(
                supportedPlatforms = listOf("youtube"),
                channelTypes = listOf("GROUP_OFFICIAL", "MEMBER_PERSONAL", "SUB_UNIT", "LABEL"),
            ),
            now,
            FreshnessStatus.manual,
        )

        return ApiResponse(
            meta = ResponseMeta(
                requestId = requestId,
                generatedAt = now,
                partialFailure = hasPartialFailure(groupHeader, officialChannels, members, recentVideos, detailMeta),
            ),
            data = GroupDetailResponseData(
                groupHeader = groupHeader,
                officialChannels = officialChannels,
                members = members,
                recentVideos = recentVideos,
                detailMeta = detailMeta,
            ),
            errors = collectSectionErrors(
                "groupHeader" to groupHeader,
                "officialChannels" to officialChannels,
                "members" to members,
                "recentVideos" to recentVideos,
                "detailMeta" to detailMeta,
            ),
        )
    }

    private fun applyGroupQuery(
        groups: List<GroupAggregate>,
        keyword: String?,
        sort: String,
    ): List<GroupAggregate> {
        val normalizedKeyword = keyword?.trim()?.lowercase()?.takeIf { it.isNotBlank() }
        val filtered = if (normalizedKeyword == null) {
            groups
        } else {
            groups.filter {
                it.group.slug.lowercase().contains(normalizedKeyword) ||
                    it.group.displayName.lowercase().contains(normalizedKeyword)
            }
        }
        return when (sort) {
            "featured" -> filtered.sortedWith(
                compareByDescending<GroupAggregate> { it.group.featured }
                    .thenByDescending { it.latestVideoAt }
                    .thenBy { it.group.displayName },
            )
            "recent_video" -> filtered.sortedWith(
                compareByDescending<GroupAggregate> { it.latestVideoAt }
                    .thenBy { it.group.displayName },
            )
            "name" -> filtered.sortedBy { it.group.displayName }
            else -> throw BadRequestException("Invalid sort")
        }
    }

    private fun resolveSort(sort: String?): String = sort?.takeIf { it.isNotBlank() } ?: "featured"

    private fun resolveLimit(limit: Int?, max: Int): Int {
        val resolved = limit ?: 20
        if (resolved !in 1..max) {
            throw BadRequestException("The query parameter 'limit' must be between 1 and $max.")
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

    private fun buildListBadges(aggregate: GroupAggregate): List<String> =
        buildList {
            if (aggregate.group.featured) add("featured")
            if (aggregate.officialChannels.isNotEmpty()) add("official")
        }
}

internal fun String?.toFreshnessStatus(): com.mystarnow.backend.common.api.FreshnessStatus = when (this?.lowercase()) {
    "fresh" -> com.mystarnow.backend.common.api.FreshnessStatus.fresh
    "stale" -> com.mystarnow.backend.common.api.FreshnessStatus.stale
    "manual" -> com.mystarnow.backend.common.api.FreshnessStatus.manual
    else -> com.mystarnow.backend.common.api.FreshnessStatus.unknown
}
