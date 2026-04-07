package com.mystarnow.backend.home.service

import com.mystarnow.backend.common.api.SectionState
import com.mystarnow.backend.idol.service.PageInfo
import com.mystarnow.backend.idol.service.VideoFeedItemView

data class HomeResponseData(
    val recentVideos: SectionState<HomeRecentVideosSectionData>,
    val featuredGroups: SectionState<HomeFeaturedGroupsSectionData>,
)

data class HomeRecentVideosSectionData(
    val items: List<VideoFeedItemView>,
    val pageInfo: PageInfo,
)

data class HomeFeaturedGroupsSectionData(
    val items: List<HomeFeaturedGroupItemView>,
)

data class HomeFeaturedGroupItemView(
    val groupId: String,
    val groupSlug: String,
    val groupName: String,
    val coverImageUrl: String?,
    val officialChannelCount: Int,
    val memberPersonalChannelCount: Int,
    val latestVideoAt: String?,
)
