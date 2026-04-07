package com.mystarnow.shared.data.remote

import com.mystarnow.shared.core.model.AppMode
import com.mystarnow.shared.core.platform.DeveloperSettingsStorage
import kotlinx.coroutines.flow.first

class SwitchableMyStarNowApiProvider(
    private val settingsStorage: DeveloperSettingsStorage,
    private val mockApi: MyStarNowApi,
    private val liveApiFactory: (String) -> MyStarNowApi,
) : MyStarNowApiProvider {
    private val liveApiCache = mutableMapOf<String, MyStarNowApi>()

    override suspend fun getApi(): MyStarNowApi {
        val settings = settingsStorage.observeSettings().first()
        return if (settings.mode == AppMode.MOCK) {
            mockApi
        } else {
            liveApiCache.getOrPut(settings.baseUrl) {
                liveApiFactory(settings.baseUrl)
            }
        }
    }
}
