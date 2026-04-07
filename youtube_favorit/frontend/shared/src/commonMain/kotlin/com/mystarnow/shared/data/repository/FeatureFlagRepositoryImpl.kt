package com.mystarnow.shared.data.repository

import com.mystarnow.shared.data.remote.MyStarNowApiProvider
import com.mystarnow.shared.data.remote.toAppConfigDomain
import com.mystarnow.shared.domain.model.AppConfig
import com.mystarnow.shared.domain.repository.FeatureFlagRepository

class FeatureFlagRepositoryImpl(
    private val apiProvider: MyStarNowApiProvider,
) : FeatureFlagRepository {
    override suspend fun getAppConfig(
        clientPlatform: String,
        clientVersion: String?,
        locale: String?,
    ): AppConfig = apiProvider.getApi().getAppConfig(
        clientPlatform = clientPlatform,
        clientVersion = clientVersion,
        locale = locale,
    ).toAppConfigDomain()
}
