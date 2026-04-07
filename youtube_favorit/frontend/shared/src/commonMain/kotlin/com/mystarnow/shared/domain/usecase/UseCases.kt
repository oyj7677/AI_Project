package com.mystarnow.shared.domain.usecase

import com.mystarnow.shared.core.model.AppMode
import com.mystarnow.shared.core.model.DeveloperSettings
import com.mystarnow.shared.domain.model.AppConfig
import com.mystarnow.shared.domain.model.HomeFeed
import com.mystarnow.shared.domain.model.InfluencerDetail
import com.mystarnow.shared.domain.model.InfluencerListPage
import com.mystarnow.shared.domain.model.SearchInfluencersQuery
import com.mystarnow.shared.domain.repository.DeveloperSettingsRepository
import com.mystarnow.shared.domain.repository.FavoritesRepository
import com.mystarnow.shared.domain.repository.FeatureFlagRepository
import com.mystarnow.shared.domain.repository.HomeRepository
import com.mystarnow.shared.domain.repository.InfluencerRepository
import kotlinx.coroutines.flow.Flow

class GetHomeFeed(
    private val repository: HomeRepository,
) {
    suspend operator fun invoke(timezone: String? = null, locale: String? = null): HomeFeed =
        repository.getHomeFeed(timezone = timezone, locale = locale)
}

class SearchInfluencers(
    private val repository: InfluencerRepository,
) {
    suspend operator fun invoke(query: SearchInfluencersQuery): InfluencerListPage =
        repository.search(query)
}

class GetInfluencerDetail(
    private val repository: InfluencerRepository,
) {
    suspend operator fun invoke(
        slug: String,
        timezone: String? = null,
        activitiesLimit: Int = 20,
        schedulesLimit: Int = 10,
    ): InfluencerDetail = repository.getDetail(
        slug = slug,
        timezone = timezone,
        activitiesLimit = activitiesLimit,
        schedulesLimit = schedulesLimit,
    )
}

class ToggleFavorite(
    private val repository: FavoritesRepository,
) {
    suspend operator fun invoke(slug: String) = repository.toggleFavorite(slug)
}

class ObserveFavorites(
    private val repository: FavoritesRepository,
) {
    operator fun invoke(): Flow<Set<String>> = repository.observeFavorites()
}

class GetFeatureFlags(
    private val repository: FeatureFlagRepository,
) {
    suspend operator fun invoke(
        clientPlatform: String = "android",
        clientVersion: String? = null,
        locale: String? = null,
    ): AppConfig = repository.getAppConfig(
        clientPlatform = clientPlatform,
        clientVersion = clientVersion,
        locale = locale,
    )
}

class ObserveDeveloperSettings(
    private val repository: DeveloperSettingsRepository,
) {
    operator fun invoke(): Flow<DeveloperSettings> = repository.observeSettings()
}

class SetDeveloperMode(
    private val repository: DeveloperSettingsRepository,
) {
    suspend operator fun invoke(mode: AppMode) {
        repository.updateMode(mode)
    }
}

class SetApiBaseUrl(
    private val repository: DeveloperSettingsRepository,
) {
    suspend operator fun invoke(baseUrl: String) {
        repository.updateBaseUrl(baseUrl)
    }
}
