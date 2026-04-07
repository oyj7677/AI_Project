package com.mystarnow.shared.core.platform

import com.mystarnow.shared.core.model.AppMode
import com.mystarnow.shared.core.model.DeveloperSettings
import kotlinx.coroutines.flow.Flow

interface DeveloperSettingsStorage {
    fun observeSettings(): Flow<DeveloperSettings>
    suspend fun updateMode(mode: AppMode)
    suspend fun updateBaseUrl(baseUrl: String)
}
