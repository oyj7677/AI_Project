package com.mystarnow.shared.platform

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.mystarnow.shared.core.model.AppMode
import com.mystarnow.shared.core.model.DeveloperSettings
import com.mystarnow.shared.core.platform.DeveloperSettingsStorage
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class AndroidDeveloperSettingsStorage(
    context: Context,
) : DeveloperSettingsStorage {
    private val modeKey = stringPreferencesKey("app_mode")
    private val baseUrlKey = stringPreferencesKey("api_base_url")
    private val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
        produceFile = { context.preferencesDataStoreFile("mystarnow-dev-settings") },
    )

    override fun observeSettings(): Flow<DeveloperSettings> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            DeveloperSettings(
                mode = when (preferences[modeKey]) {
                    AppMode.LIVE.name -> AppMode.LIVE
                    else -> AppMode.MOCK
                },
                baseUrl = preferences[baseUrlKey] ?: "http://10.0.2.2:8080",
            )
        }

    override suspend fun updateMode(mode: AppMode) {
        dataStore.edit { it[modeKey] = mode.name }
    }

    override suspend fun updateBaseUrl(baseUrl: String) {
        dataStore.edit { it[baseUrlKey] = baseUrl.trim() }
    }
}
