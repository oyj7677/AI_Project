package com.mystarnow.shared.data.remote.dto

import com.mystarnow.shared.data.mock.MockPayloads
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json

class DtoParsingTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun parsesHomeEnvelopeWithPartialFailure() {
        val payload = json.decodeFromString<ApiEnvelopeDto<HomePayloadDto>>(MockPayloads.HOME)

        assertEquals("v1", payload.meta.apiVersion)
        assertTrue(payload.meta.partialFailure)
        assertEquals("partial", payload.data!!.latestUpdates.status)
        assertEquals("youtube", payload.data.liveNow.data.items.first().platform)
    }

    @Test
    fun parsesDetailEnvelope() {
        val payload = json.decodeFromString<ApiEnvelopeDto<InfluencerDetailPayloadDto>>(MockPayloads.DETAIL)

        assertEquals("haru", payload.data!!.profile.data.slug)
        assertEquals(2, payload.data.channels.data.items.size)
        assertEquals("partial", payload.data.recentActivities.status)
    }

    @Test
    fun parsesConfigEnvelope() {
        val payload = json.decodeFromString<ApiEnvelopeDto<AppConfigPayloadDto>>(MockPayloads.CONFIG)

        assertEquals(true, payload.data!!.featureFlags.data.enableLiveNow)
        assertEquals(2, payload.data.runtime.data.supportedPlatforms.size)
    }
}
