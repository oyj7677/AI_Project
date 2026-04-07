package com.mystarnow.shared.data.repository

import com.mystarnow.shared.core.model.AppMode
import com.mystarnow.shared.core.model.DeveloperSettings
import com.mystarnow.shared.core.platform.DeveloperSettingsStorage
import com.mystarnow.shared.domain.repository.DeveloperSettingsRepository
import kotlinx.coroutines.flow.Flow

class DeveloperSettingsRepositoryImpl(
    private val storage: DeveloperSettingsStorage,
) : DeveloperSettingsRepository {
    override fun observeSettings(): Flow<DeveloperSettings> = storage.observeSettings()

    override suspend fun updateMode(mode: AppMode) {
        storage.updateMode(mode)
    }

    override suspend fun updateBaseUrl(baseUrl: String) {
        val normalized = baseUrl.trim()
        if (normalized.isNotEmpty()) {
            storage.updateBaseUrl(normalized.removeSuffix("/"))
        }
    }
}
