package com.mystarnow.backend.idol.service

import com.mystarnow.backend.common.api.SectionState

data class MemberDetailResponseData(
    val memberProfile: SectionState<MemberProfileSectionData>,
    val personalChannels: SectionState<MemberPersonalChannelsSectionData>,
    val recentVideos: SectionState<MemberRecentVideosSectionData>,
)

data class MemberProfileSectionData(
    val memberId: String,
    val memberSlug: String,
    val memberName: String,
    val profileImageUrl: String?,
    val group: GroupRefView,
)

data class MemberPersonalChannelsSectionData(
    val items: List<MemberPersonalChannelItemView>,
)

data class MemberPersonalChannelItemView(
    val channelId: String,
    val externalChannelId: String,
    val channelName: String,
    val handle: String?,
    val channelUrl: String,
    val channelType: String,
    val isOfficial: Boolean,
    val latestVideoAt: String?,
)

data class MemberRecentVideosSectionData(
    val items: List<VideoFeedItemView>,
    val pageInfo: PageInfo,
)

