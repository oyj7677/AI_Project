package com.mystarnow.shared.platform

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.mystarnow.shared.core.platform.FavoritesStorage
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class AndroidFavoritesStorage(
    context: Context,
) : FavoritesStorage {
    private val favoriteKey = stringSetPreferencesKey("favorite_slugs")
    private val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
        produceFile = { context.preferencesDataStoreFile("mystarnow-preferences") },
    )

    override fun observeFavorites(): Flow<Set<String>> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[favoriteKey] ?: emptySet()
        }

    override suspend fun toggleFavorite(slug: String) {
        dataStore.edit { preferences ->
            val current = preferences[favoriteKey].orEmpty().toMutableSet()
            if (!current.add(slug)) {
                current.remove(slug)
            }
            preferences[favoriteKey] = current
        }
    }
}
