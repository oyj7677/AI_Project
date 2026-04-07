package com.mystarnow.shared.core.model

enum class AppMode {
    MOCK,
    LIVE,
}

data class DeveloperSettings(
    val mode: AppMode = AppMode.MOCK,
    val baseUrl: String = "http://10.0.2.2:8080",
)
