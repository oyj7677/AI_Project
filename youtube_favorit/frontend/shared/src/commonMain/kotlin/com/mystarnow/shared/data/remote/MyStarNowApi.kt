package com.mystarnow.shared.data.remote

import com.mystarnow.shared.data.remote.dto.ApiEnvelopeDto
import com.mystarnow.shared.data.remote.dto.AppConfigPayloadDto
import com.mystarnow.shared.data.remote.dto.HomePayloadDto
import com.mystarnow.shared.data.remote.dto.InfluencerDetailPayloadDto
import com.mystarnow.shared.data.remote.dto.InfluencerListPayloadDto
import com.mystarnow.shared.domain.model.Platform

interface MyStarNowApi {
    suspend fun getHomeFeed(
        timezone: String? = null,
        locale: String? = null,
    ): ApiEnvelopeDto<HomePayloadDto>

    suspend fun getInfluencers(
        query: String = "",
        category: String? = null,
        platforms: List<Platform> = emptyList(),
        sort: String = "featured",
        cursor: String? = null,
        limit: Int = 20,
    ): ApiEnvelopeDto<InfluencerListPayloadDto>

    suspend fun getInfluencerDetail(
        slug: String,
        timezone: String? = null,
        activitiesLimit: Int = 20,
        schedulesLimit: Int = 10,
    ): ApiEnvelopeDto<InfluencerDetailPayloadDto>

    suspend fun getAppConfig(
        clientPlatform: String = "android",
        clientVersion: String? = null,
        locale: String? = null,
    ): ApiEnvelopeDto<AppConfigPayloadDto>
}
