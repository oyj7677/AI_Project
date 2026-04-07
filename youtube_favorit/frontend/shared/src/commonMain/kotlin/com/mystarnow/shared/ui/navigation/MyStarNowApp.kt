package com.mystarnow.shared.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.mystarnow.shared.presentation.detail.InfluencerDetailViewModel
import com.mystarnow.shared.presentation.favorites.FavoritesViewModel
import com.mystarnow.shared.presentation.home.HomeViewModel
import com.mystarnow.shared.presentation.list.InfluencerListViewModel
import com.mystarnow.shared.ui.components.AppFeatureFlagsUiModel
import com.mystarnow.shared.ui.components.DeveloperSettingsUiModel
import com.mystarnow.shared.ui.components.toUiModel
import com.mystarnow.shared.ui.detail.InfluencerDetailScreen
import com.mystarnow.shared.ui.favorites.FavoritesScreen
import com.mystarnow.shared.ui.home.HomeScreen
import com.mystarnow.shared.ui.list.InfluencerListScreen
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun MyStarNowApp(
    services: AppServices,
    onOpenExternalLink: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val homeViewModel = remember(services) {
        HomeViewModel(
            getHomeFeed = services.getHomeFeed,
            toggleFavorite = services.toggleFavorite,
            observeFavorites = services.observeFavorites,
        )
    }
    val listViewModel = remember(services) {
        InfluencerListViewModel(
            searchInfluencers = services.searchInfluencers,
            toggleFavorite = services.toggleFavorite,
            observeFavorites = services.observeFavorites,
        )
    }
    val favoritesViewModel = remember(services) {
        FavoritesViewModel(
            searchInfluencers = services.searchInfluencers,
            toggleFavorite = services.toggleFavorite,
            observeFavorites = services.observeFavorites,
        )
    }
    var route by remember { mutableStateOf<AppRoute>(AppRoute.Home) }
    val snackbarHostState = remember { SnackbarHostState() }
    val appScope = rememberCoroutineScope()
    val developerSettings by produceState(initialValue = DeveloperSettingsUiModel()) {
        services.observeDeveloperSettings().collect { settings ->
            value = settings.toUiModel()
        }
    }
    val featureFlags by produceState(
        initialValue = AppFeatureFlagsUiModel(),
        key1 = developerSettings.mode,
        key2 = developerSettings.baseUrl,
    ) {
        value = runCatching { services.getFeatureFlags().featureFlags.data.toUiModel() }
            .getOrDefault(AppFeatureFlagsUiModel())
    }

    DisposableEffect(Unit) {
        onDispose {
            homeViewModel.clear()
            listViewModel.clear()
            favoritesViewModel.clear()
        }
    }

    LaunchedEffect(homeViewModel) {
        homeViewModel.events.collectLatest {
            if (it is com.mystarnow.shared.core.model.ViewEvent.Message) {
                snackbarHostState.showSnackbar(it.text)
            }
        }
    }
    LaunchedEffect(listViewModel) {
        listViewModel.events.collectLatest {
            if (it is com.mystarnow.shared.core.model.ViewEvent.Message) {
                snackbarHostState.showSnackbar(it.text)
            }
        }
    }
    LaunchedEffect(favoritesViewModel) {
        favoritesViewModel.events.collectLatest {
            if (it is com.mystarnow.shared.core.model.ViewEvent.Message) {
                snackbarHostState.showSnackbar(it.text)
            }
        }
    }
    LaunchedEffect(developerSettings.mode, developerSettings.baseUrl) {
        homeViewModel.refresh()
        listViewModel.retry()
        favoritesViewModel.retry()
    }

    val isDetail = route is AppRoute.InfluencerDetail
    MaterialTheme {
        Scaffold(
            modifier = modifier,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (!isDetail) {
                    NavigationBar {
                        NavigationBarItem(
                            selected = route == AppRoute.Home,
                            onClick = { route = AppRoute.Home },
                            label = { Text("홈") },
                            icon = { Text("H") },
                        )
                        NavigationBarItem(
                            selected = route == AppRoute.InfluencerList,
                            onClick = { route = AppRoute.InfluencerList },
                            label = { Text("목록") },
                            icon = { Text("L") },
                        )
                        NavigationBarItem(
                            selected = route == AppRoute.Favorites,
                            onClick = { route = AppRoute.Favorites },
                            label = { Text("즐겨찾기") },
                            icon = { Text("F") },
                        )
                    }
                }
            },
        ) { innerPadding ->
            when (val currentRoute = route) {
                AppRoute.Home -> HomeScreen(
                    state = homeViewModel.state.value,
                    settings = developerSettings,
                    featureFlags = featureFlags,
                    onRefresh = homeViewModel::refresh,
                    onOpenInfluencer = { slug -> route = AppRoute.InfluencerDetail(slug) },
                    onToggleFavorite = homeViewModel::onToggleFavorite,
                    onModeChange = { mode ->
                        appScope.launch { services.setDeveloperMode(mode) }
                    },
                    onBaseUrlChange = { baseUrl ->
                        appScope.launch { services.setApiBaseUrl(baseUrl) }
                    },
                    onOpenExternalLink = onOpenExternalLink,
                    modifier = Modifier.padding(innerPadding),
                )

                AppRoute.InfluencerList -> InfluencerListScreen(
                    state = listViewModel.state.value,
                    onQueryChange = listViewModel::updateQuery,
                    onSearch = listViewModel::onSearch,
                    onSelectCategory = listViewModel::selectCategory,
                    onTogglePlatform = listViewModel::togglePlatform,
                    onSortChange = listViewModel::updateSort,
                    onOpenInfluencer = { slug -> route = AppRoute.InfluencerDetail(slug) },
                    onToggleFavorite = listViewModel::onToggleFavorite,
                    onLoadMore = listViewModel::loadMore,
                    modifier = Modifier.padding(innerPadding),
                )

                AppRoute.Favorites -> FavoritesScreen(
                    state = favoritesViewModel.state.value,
                    onOpenInfluencer = { slug -> route = AppRoute.InfluencerDetail(slug) },
                    onToggleFavorite = favoritesViewModel::onToggleFavorite,
                    modifier = Modifier.padding(innerPadding),
                )

                is AppRoute.InfluencerDetail -> {
                    val detailViewModel = remember(currentRoute.slug, services) {
                        InfluencerDetailViewModel(
                            slug = currentRoute.slug,
                            getInfluencerDetail = services.getInfluencerDetail,
                            toggleFavorite = services.toggleFavorite,
                            observeFavorites = services.observeFavorites,
                        )
                    }
                    DisposableEffect(detailViewModel) {
                        onDispose { detailViewModel.clear() }
                    }
                    LaunchedEffect(detailViewModel) {
                        detailViewModel.events.collectLatest {
                            if (it is com.mystarnow.shared.core.model.ViewEvent.Message) {
                                snackbarHostState.showSnackbar(it.text)
                            }
                        }
                    }
                    InfluencerDetailScreen(
                        state = detailViewModel.state.value,
                        onBack = { route = AppRoute.Home },
                        onRetry = detailViewModel::reload,
                        onToggleFavorite = detailViewModel::onToggleFavorite,
                        onOpenExternalLink = onOpenExternalLink,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}
