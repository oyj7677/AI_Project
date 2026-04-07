package com.mystarnow.shared.data.remote

import com.mystarnow.shared.data.remote.dto.ApiEnvelopeDto
import com.mystarnow.shared.data.remote.dto.AppConfigPayloadDto
import com.mystarnow.shared.data.remote.dto.HomePayloadDto
import com.mystarnow.shared.data.remote.dto.InfluencerDetailPayloadDto
import com.mystarnow.shared.data.remote.dto.InfluencerListPayloadDto
import com.mystarnow.shared.domain.model.Platform
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class KtorMyStarNowApi(
    private val httpClient: HttpClient,
    private val baseUrl: String,
) : MyStarNowApi {
    override suspend fun getHomeFeed(
        timezone: String?,
        locale: String?,
    ): ApiEnvelopeDto<HomePayloadDto> = httpClient.get("$baseUrl/v1/home") {
        timezone?.let { parameter("timezone", it) }
        locale?.let { parameter("locale", it) }
    }.body()

    override suspend fun getInfluencers(
        query: String,
        category: String?,
        platforms: List<Platform>,
        sort: String,
        cursor: String?,
        limit: Int,
    ): ApiEnvelopeDto<InfluencerListPayloadDto> = httpClient.get("$baseUrl/v1/influencers") {
        if (query.isNotBlank()) parameter("q", query)
        category?.let { parameter("category", it) }
        platforms.forEach { parameter("platform", it.name.lowercase()) }
        parameter("sort", sort)
        cursor?.let { parameter("cursor", it) }
        parameter("limit", limit)
    }.body()

    override suspend fun getInfluencerDetail(
        slug: String,
        timezone: String?,
        activitiesLimit: Int,
        schedulesLimit: Int,
    ): ApiEnvelopeDto<InfluencerDetailPayloadDto> = httpClient.get("$baseUrl/v1/influencers/$slug") {
        timezone?.let { parameter("timezone", it) }
        parameter("activitiesLimit", activitiesLimit)
        parameter("schedulesLimit", schedulesLimit)
    }.body()

    override suspend fun getAppConfig(
        clientPlatform: String,
        clientVersion: String?,
        locale: String?,
    ): ApiEnvelopeDto<AppConfigPayloadDto> = httpClient.get("$baseUrl/v1/meta/app-config") {
        parameter("clientPlatform", clientPlatform)
        clientVersion?.let { parameter("clientVersion", it) }
        locale?.let { parameter("locale", it) }
    }.body()
}
