package com.mystarnow.shared.domain.repository

import com.mystarnow.shared.domain.model.AppConfig
import com.mystarnow.shared.domain.model.HomeFeed
import com.mystarnow.shared.domain.model.InfluencerDetail
import com.mystarnow.shared.domain.model.InfluencerListPage
import com.mystarnow.shared.domain.model.SearchInfluencersQuery
import kotlinx.coroutines.flow.Flow

interface HomeRepository {
    suspend fun getHomeFeed(timezone: String? = null, locale: String? = null): HomeFeed
}

interface InfluencerRepository {
    suspend fun search(query: SearchInfluencersQuery): InfluencerListPage
    suspend fun getDetail(
        slug: String,
        timezone: String? = null,
        activitiesLimit: Int = 20,
        schedulesLimit: Int = 10,
    ): InfluencerDetail
}

interface FavoritesRepository {
    fun observeFavorites(): Flow<Set<String>>
    suspend fun toggleFavorite(slug: String)
}

interface FeatureFlagRepository {
    suspend fun getAppConfig(
        clientPlatform: String = "android",
        clientVersion: String? = null,
        locale: String? = null,
    ): AppConfig
}
