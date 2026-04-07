package com.mystarnow.backend.idol.service

data class PageInfo(
    val limit: Int,
    val nextCursor: String?,
    val hasNext: Boolean,
)

data class GroupRefView(
    val groupId: String,
    val groupSlug: String,
    val groupName: String,
)

data class MemberRefView(
    val memberId: String,
    val memberSlug: String,
    val memberName: String,
)

data class ChannelSummaryView(
    val channelId: String,
    val externalChannelId: String,
    val channelName: String,
    val handle: String?,
    val channelUrl: String,
    val channelType: String,
    val isOfficial: Boolean,
)

data class VideoFeedItemView(
    val videoId: String,
    val externalVideoId: String?,
    val title: String,
    val description: String?,
    val thumbnailUrl: String?,
    val publishedAt: String,
    val videoUrl: String,
    val contentType: String,
    val channel: ChannelSummaryView,
    val group: GroupRefView,
    val member: MemberRefView?,
    val badges: List<String>,
)
