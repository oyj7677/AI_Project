package com.mystarnow.backend.idol.service

import com.mystarnow.backend.persistence.entity.IdolGroupEntity
import com.mystarnow.backend.persistence.entity.IdolMemberEntity
import com.mystarnow.backend.persistence.entity.YouTubeChannelEntity
import com.mystarnow.backend.persistence.entity.YouTubeVideoEntity

object IdolViewMapper {
    fun toGroupRef(group: IdolGroupEntity): GroupRefView = GroupRefView(
        groupId = group.id.toString(),
        groupSlug = group.slug,
        groupName = group.displayName,
    )

    fun toMemberRef(member: IdolMemberEntity): MemberRefView = MemberRefView(
        memberId = member.id.toString(),
        memberSlug = member.slug,
        memberName = member.displayName,
    )

    fun toChannelSummary(channel: YouTubeChannelEntity): ChannelSummaryView = ChannelSummaryView(
        channelId = channel.id.toString(),
        externalChannelId = channel.externalChannelId,
        channelName = channel.displayLabel ?: channel.externalChannelId,
        handle = channel.handle,
        channelUrl = channel.channelUrl,
        channelType = channel.channelType,
        isOfficial = channel.official,
    )

    fun toVideoFeedItem(
        video: YouTubeVideoEntity,
        channel: YouTubeChannelEntity,
        group: IdolGroupEntity,
        member: IdolMemberEntity?,
    ): VideoFeedItemView = VideoFeedItemView(
        videoId = video.id.toString(),
        externalVideoId = video.externalVideoId,
        title = video.title,
        description = video.description,
        thumbnailUrl = video.thumbnailUrl,
        publishedAt = video.publishedAt.toString(),
        videoUrl = video.videoUrl,
        contentType = video.contentType,
        channel = toChannelSummary(channel),
        group = toGroupRef(group),
        member = member?.let(::toMemberRef),
        badges = buildBadges(channel, member, video),
    )

    private fun buildBadges(
        channel: YouTubeChannelEntity,
        member: IdolMemberEntity?,
        video: YouTubeVideoEntity,
    ): List<String> {
        val badges = mutableListOf<String>()
        badges += when (channel.channelType) {
            "GROUP_OFFICIAL" -> "group-official"
            "MEMBER_PERSONAL" -> "member-personal"
            "SUB_UNIT" -> "sub-unit"
            "LABEL" -> "label"
            else -> "channel"
        }
        if (member != null) {
            badges += "member"
        }
        if (video.pinned) {
            badges += "pinned"
        }
        return badges
    }
}
