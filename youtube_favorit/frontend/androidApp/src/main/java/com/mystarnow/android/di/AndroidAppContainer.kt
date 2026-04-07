package com.mystarnow.android.di

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.mystarnow.shared.data.mock.MockMyStarNowApi
import com.mystarnow.shared.data.remote.KtorMyStarNowApi
import com.mystarnow.shared.data.remote.SwitchableMyStarNowApiProvider
import com.mystarnow.shared.data.repository.DeveloperSettingsRepositoryImpl
import com.mystarnow.shared.data.repository.FavoritesRepositoryImpl
import com.mystarnow.shared.data.repository.FeatureFlagRepositoryImpl
import com.mystarnow.shared.data.repository.HomeRepositoryImpl
import com.mystarnow.shared.data.repository.InfluencerRepositoryImpl
import com.mystarnow.shared.domain.usecase.ObserveDeveloperSettings
import com.mystarnow.shared.domain.usecase.GetFeatureFlags
import com.mystarnow.shared.domain.usecase.GetHomeFeed
import com.mystarnow.shared.domain.usecase.GetInfluencerDetail
import com.mystarnow.shared.domain.usecase.ObserveFavorites
import com.mystarnow.shared.domain.usecase.SearchInfluencers
import com.mystarnow.shared.domain.usecase.SetApiBaseUrl
import com.mystarnow.shared.domain.usecase.SetDeveloperMode
import com.mystarnow.shared.domain.usecase.ToggleFavorite
import com.mystarnow.shared.platform.AndroidDeveloperSettingsStorage
import com.mystarnow.shared.platform.AndroidFavoritesStorage
import com.mystarnow.shared.platform.createAndroidHttpClient
import com.mystarnow.shared.ui.navigation.AppServices

class AndroidAppContainer(
    private val context: Context,
) {
    private val httpClient = createAndroidHttpClient()
    private val mockApi = MockMyStarNowApi()
    private val favoritesStorage = AndroidFavoritesStorage(context)
    private val developerSettingsStorage = AndroidDeveloperSettingsStorage(context)
    private val developerSettingsRepository = DeveloperSettingsRepositoryImpl(developerSettingsStorage)

    private val apiProvider = SwitchableMyStarNowApiProvider(
        settingsStorage = developerSettingsStorage,
        mockApi = mockApi,
        liveApiFactory = { baseUrl ->
            KtorMyStarNowApi(
                httpClient = httpClient,
                baseUrl = baseUrl,
            )
        },
    )

    private val homeRepository = HomeRepositoryImpl(apiProvider)
    private val influencerRepository = InfluencerRepositoryImpl(apiProvider)
    private val favoritesRepository = FavoritesRepositoryImpl(favoritesStorage)
    private val featureFlagRepository = FeatureFlagRepositoryImpl(apiProvider)

    val appServices = AppServices(
        getHomeFeed = GetHomeFeed(homeRepository),
        searchInfluencers = SearchInfluencers(influencerRepository),
        getInfluencerDetail = GetInfluencerDetail(influencerRepository),
        toggleFavorite = ToggleFavorite(favoritesRepository),
        observeFavorites = ObserveFavorites(favoritesRepository),
        getFeatureFlags = GetFeatureFlags(featureFlagRepository),
        observeDeveloperSettings = ObserveDeveloperSettings(developerSettingsRepository),
        setDeveloperMode = SetDeveloperMode(developerSettingsRepository),
        setApiBaseUrl = SetApiBaseUrl(developerSettingsRepository),
    )

    fun openExternalLink(url: String) {
        runCatching {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}
