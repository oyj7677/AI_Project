package com.mystarnow.backend.idol.service

import com.mystarnow.backend.common.api.SectionState

data class GroupListResponseData(
    val results: SectionState<GroupResultsSectionData>,
    val filters: SectionState<GroupFiltersSectionData>,
)

data class GroupResultsSectionData(
    val items: List<GroupListItemView>,
    val pageInfo: PageInfo,
    val appliedFilters: GroupAppliedFilters,
)

data class GroupListItemView(
    val groupId: String,
    val groupSlug: String,
    val groupName: String,
    val description: String?,
    val coverImageUrl: String?,
    val officialChannelCount: Int,
    val memberCount: Int,
    val memberPersonalChannelCount: Int,
    val latestVideoAt: String?,
    val latestVideoThumbnailUrl: String?,
    val badges: List<String>,
)

data class GroupAppliedFilters(
    val q: String?,
    val sort: String,
)

data class GroupFiltersSectionData(
    val sortOptions: List<SortOptionItem>,
)

data class SortOptionItem(
    val value: String,
    val label: String,
)

data class GroupDetailResponseData(
    val groupHeader: SectionState<GroupHeaderSectionData>,
    val officialChannels: SectionState<GroupOfficialChannelsSectionData>,
    val members: SectionState<GroupMembersSectionData>,
    val recentVideos: SectionState<GroupRecentVideosSectionData>,
    val detailMeta: SectionState<GroupDetailMetaSectionData>,
)

data class GroupHeaderSectionData(
    val groupId: String,
    val groupSlug: String,
    val groupName: String,
    val description: String?,
    val coverImageUrl: String?,
    val memberCount: Int,
    val officialChannelCount: Int,
    val memberPersonalChannelCount: Int,
    val latestVideoAt: String?,
    val isFeatured: Boolean,
)

data class GroupOfficialChannelsSectionData(
    val items: List<GroupOfficialChannelItemView>,
)

data class GroupOfficialChannelItemView(
    val channelId: String,
    val externalChannelId: String,
    val channelName: String,
    val handle: String?,
    val channelUrl: String,
    val channelType: String,
    val isOfficial: Boolean,
    val latestVideoAt: String?,
)

data class GroupMembersSectionData(
    val items: List<GroupMemberItemView>,
)

data class GroupMemberItemView(
    val memberId: String,
    val memberSlug: String,
    val memberName: String,
    val profileImageUrl: String?,
    val hasPersonalChannel: Boolean,
    val personalChannelCount: Int,
    val latestVideoAt: String?,
)

data class GroupRecentVideosSectionData(
    val items: List<VideoFeedItemView>,
    val pageInfo: PageInfo,
)

data class GroupDetailMetaSectionData(
    val supportedPlatforms: List<String>,
    val channelTypes: List<String>,
)

