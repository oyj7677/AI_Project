package com.mystarnow.shared.data.repository

import com.mystarnow.shared.data.mock.MockCatalog
import com.mystarnow.shared.data.remote.toAppConfigDomain
import com.mystarnow.shared.data.remote.toDomain
import com.mystarnow.shared.data.remote.toInfluencerDetailDomain
import com.mystarnow.shared.data.remote.toInfluencerListDomain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MappersTest {
    @Test
    fun mapsHomeEnvelopeIntoStableDomainSections() {
        val home = MockCatalog.homeEnvelope.toDomain()

        assertEquals(1, home.liveNow.data.items.size)
        assertEquals("하루", home.featuredInfluencers.data.items.first().name)
        assertTrue(home.meta.partialFailure)
    }

    @Test
    fun mapsListEnvelopeIntoPaginatedDomainData() {
        val listPage = MockCatalog.listEnvelope(
            query = "",
            category = null,
            platforms = emptyList(),
            sort = "featured",
            cursor = null,
            limit = 20,
        ).toInfluencerListDomain()

        assertEquals(3, listPage.results.data.items.size)
        assertEquals(true, listPage.filters.data.platforms.isNotEmpty())
    }

    @Test
    fun mapsDetailAndConfigEnvelopes() {
        val detail = MockCatalog.detailEnvelope("haru").toInfluencerDetailDomain()
        val config = MockCatalog.configEnvelope.toAppConfigDomain()

        assertEquals("haru", detail.profile.data.slug)
        assertEquals(true, config.featureFlags.data.showSchedules)
    }
}
