package com.mystarnow.shared.data.repository

import com.mystarnow.shared.InMemoryDeveloperSettingsStorage
import com.mystarnow.shared.core.model.AppMode
import com.mystarnow.shared.core.model.DeveloperSettings
import com.mystarnow.shared.data.mock.MockMyStarNowApi
import com.mystarnow.shared.data.remote.MyStarNowApi
import com.mystarnow.shared.data.remote.SwitchableMyStarNowApiProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

private class FakeLiveApi : MyStarNowApi {
    override suspend fun getHomeFeed(timezone: String?, locale: String?) = throw UnsupportedOperationException()
    override suspend fun getInfluencers(query: String, category: String?, platforms: List<com.mystarnow.shared.domain.model.Platform>, sort: String, cursor: String?, limit: Int) = throw UnsupportedOperationException()
    override suspend fun getInfluencerDetail(slug: String, timezone: String?, activitiesLimit: Int, schedulesLimit: Int) = throw UnsupportedOperationException()
    override suspend fun getAppConfig(clientPlatform: String, clientVersion: String?, locale: String?) = throw UnsupportedOperationException()
}

@OptIn(ExperimentalCoroutinesApi::class)
class DeveloperSettingsModeSwitchTest {
    @Test
    fun settingsRepositoryUpdatesModeAndBaseUrl() = runTest {
        val repository = DeveloperSettingsRepositoryImpl(InMemoryDeveloperSettingsStorage())

        repository.updateMode(AppMode.LIVE)
        repository.updateBaseUrl("http://localhost:18080/")

        val current = repository.observeSettings().first()
        assertEquals(AppMode.LIVE, current.mode)
        assertEquals("http://localhost:18080", current.baseUrl)
    }

    @Test
    fun switchableApiProviderUsesLatestMode() = runTest {
        val storage = InMemoryDeveloperSettingsStorage(DeveloperSettings())
        val mockApi = MockMyStarNowApi()
        val provider = SwitchableMyStarNowApiProvider(
            settingsStorage = storage,
            mockApi = mockApi,
            liveApiFactory = { FakeLiveApi() },
        )

        val first = provider.getApi()
        storage.updateMode(AppMode.LIVE)
        val second = provider.getApi()

        assertEquals(mockApi, first)
        assertNotEquals(mockApi, second)
    }
}
