package com.mystarnow.shared.ui.navigation

import com.mystarnow.shared.domain.usecase.ObserveDeveloperSettings
import com.mystarnow.shared.domain.usecase.GetFeatureFlags
import com.mystarnow.shared.domain.usecase.GetHomeFeed
import com.mystarnow.shared.domain.usecase.GetInfluencerDetail
import com.mystarnow.shared.domain.usecase.ObserveFavorites
import com.mystarnow.shared.domain.usecase.SearchInfluencers
import com.mystarnow.shared.domain.usecase.SetApiBaseUrl
import com.mystarnow.shared.domain.usecase.SetDeveloperMode
import com.mystarnow.shared.domain.usecase.ToggleFavorite

data class AppServices(
    val getHomeFeed: GetHomeFeed,
    val searchInfluencers: SearchInfluencers,
    val getInfluencerDetail: GetInfluencerDetail,
    val toggleFavorite: ToggleFavorite,
    val observeFavorites: ObserveFavorites,
    val getFeatureFlags: GetFeatureFlags,
    val observeDeveloperSettings: ObserveDeveloperSettings,
    val setDeveloperMode: SetDeveloperMode,
    val setApiBaseUrl: SetApiBaseUrl,
)
