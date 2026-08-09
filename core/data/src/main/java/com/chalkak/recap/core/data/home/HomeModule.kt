package com.chalkak.recap.core.data.home

import com.chalkak.recap.core.data.BuildConfig
import com.chalkak.recap.core.data.backend.BackendSelection
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HomeModule {
    @Provides
    @Singleton
    fun provideHomeRepository(
        mockProvider: Provider<MockHomeRepository>,
        remoteProvider: Provider<RemoteHomeRepository>,
    ): HomeRepository {
        return BackendSelection.select(
            useMockBackend = BuildConfig.USE_MOCK_BACKEND,
            mockProvider = mockProvider,
            remoteProvider = remoteProvider,
        )
    }

    @Provides
    @Singleton
    fun provideRecentCapturesRepository(
        mockProvider: Provider<MockRecentCapturesRepository>,
        remoteProvider: Provider<RemoteRecentCapturesRepository>,
    ): RecentCapturesRepository {
        return BackendSelection.select(
            useMockBackend = BuildConfig.USE_MOCK_BACKEND,
            mockProvider = mockProvider,
            remoteProvider = remoteProvider,
        )
    }
}
