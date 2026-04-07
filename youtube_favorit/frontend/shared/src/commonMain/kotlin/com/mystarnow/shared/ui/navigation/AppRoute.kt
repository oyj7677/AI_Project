package com.mystarnow.shared.ui.navigation

sealed interface AppRoute {
    data object Home : AppRoute
    data object InfluencerList : AppRoute
    data object Favorites : AppRoute
    data class InfluencerDetail(val slug: String) : AppRoute
}
