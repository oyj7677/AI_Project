package com.mystarnow.shared.data.mock

import com.mystarnow.shared.data.remote.MyStarNowApi
import com.mystarnow.shared.data.remote.dto.ApiEnvelopeDto
import com.mystarnow.shared.data.remote.dto.AppConfigPayloadDto
import com.mystarnow.shared.data.remote.dto.HomePayloadDto
import com.mystarnow.shared.data.remote.dto.InfluencerDetailPayloadDto
import com.mystarnow.shared.data.remote.dto.InfluencerListPayloadDto
import com.mystarnow.shared.domain.model.Platform

class MockMyStarNowApi : MyStarNowApi {
    override suspend fun getHomeFeed(
        timezone: String?,
        locale: String?,
    ): ApiEnvelopeDto<HomePayloadDto> = MockCatalog.homeEnvelope

    override suspend fun getInfluencers(
        query: String,
        category: String?,
        platforms: List<Platform>,
        sort: String,
        cursor: String?,
        limit: Int,
    ): ApiEnvelopeDto<InfluencerListPayloadDto> = MockCatalog.listEnvelope(
        query = query,
        category = category,
        platforms = platforms.map { it.name.lowercase() },
        sort = sort,
        cursor = cursor,
        limit = limit,
    )

    override suspend fun getInfluencerDetail(
        slug: String,
        timezone: String?,
        activitiesLimit: Int,
        schedulesLimit: Int,
    ): ApiEnvelopeDto<InfluencerDetailPayloadDto> = MockCatalog.detailEnvelope(slug)

    override suspend fun getAppConfig(
        clientPlatform: String,
        clientVersion: String?,
        locale: String?,
    ): ApiEnvelopeDto<AppConfigPayloadDto> = MockCatalog.configEnvelope
}
