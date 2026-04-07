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

@Service
class MemberQueryService(
    private val readSupport: IdolCatalogReadSupport,
    private val clock: Clock,
) {
    fun getMemberDetail(
        requestId: String,
        memberSlug: String,
        videosCursor: String?,
        videosLimit: Int?,
        contentType: String?,
    ): ApiResponse<MemberDetailResponseData> {
        val now = OffsetDateTime.now(clock)
        val aggregate = readSupport.loadActiveMemberAggregateBySlug(memberSlug)
            ?: throw ResourceNotFoundException("No member exists for slug '$memberSlug'.")
        val resolvedLimit = resolveLimit(videosLimit)
        val offset = CursorCodec.decodeOffset(videosCursor)
        val normalizedContentType = normalizeContentType(contentType)
        val filteredVideos = aggregate.videos.filter {
            normalizedContentType == null || it.contentType == normalizedContentType
        }
        val pagedVideos = filteredVideos.drop(offset).take(resolvedLimit)
        val nextOffset = offset + pagedVideos.size
        val nextCursor = if (nextOffset < filteredVideos.size) CursorCodec.encodeOffset(nextOffset) else null
        val channelById = aggregate.channels.associateBy { it.id }

        val memberProfile = SectionStates.success(
            MemberProfileSectionData(
                memberId = aggregate.member.id.toString(),
                memberSlug = aggregate.member.slug,
                memberName = aggregate.member.displayName,
                profileImageUrl = aggregate.member.profileImageUrl,
                group = IdolViewMapper.toGroupRef(aggregate.group),
            ),
            now,
            FreshnessStatus.manual,
        )

        val personalChannels = if (aggregate.channels.isEmpty()) {
            SectionStates.empty(MemberPersonalChannelsSectionData(emptyList()), now)
        } else {
            SectionStates.success(
                MemberPersonalChannelsSectionData(
                    aggregate.channels.map { channel ->
                        MemberPersonalChannelItemView(
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

        val recentVideos = if (pagedVideos.isEmpty()) {
            SectionStates.empty(
                MemberRecentVideosSectionData(
                    items = emptyList(),
                    pageInfo = PageInfo(resolvedLimit, nextCursor, nextCursor != null),
                ),
                now,
            )
        } else {
            SectionStates.success(
                MemberRecentVideosSectionData(
                    items = pagedVideos.map { video ->
                        val channel = requireNotNull(channelById[video.channelId])
                        IdolViewMapper.toVideoFeedItem(video, channel, aggregate.group, aggregate.member)
                    },
                    pageInfo = PageInfo(resolvedLimit, nextCursor, nextCursor != null),
                ),
                now,
                pagedVideos.firstOrNull()?.freshnessStatus.toFreshnessStatus(),
            )
        }

        return ApiResponse(
            meta = ResponseMeta(
                requestId = requestId,
                generatedAt = now,
                partialFailure = hasPartialFailure(memberProfile, personalChannels, recentVideos),
            ),
            data = MemberDetailResponseData(
                memberProfile = memberProfile,
                personalChannels = personalChannels,
                recentVideos = recentVideos,
            ),
            errors = collectSectionErrors(
                "memberProfile" to memberProfile,
                "personalChannels" to personalChannels,
                "recentVideos" to recentVideos,
            ),
        )
    }

    private fun resolveLimit(limit: Int?): Int {
        val resolved = limit ?: 20
        if (resolved !in 1..20) {
            throw BadRequestException("The query parameter 'videosLimit' must be between 1 and 20.")
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
