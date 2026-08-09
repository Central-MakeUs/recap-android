package com.chalkak.recap.core.data.search

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
object SearchModule {
    @Provides
    @Singleton
    fun provideSearchRepository(
        mockProvider: Provider<MockSearchRepository>,
        remoteProvider: Provider<RemoteSearchRepository>,
    ): SearchRepository {
        return BackendSelection.select(
            useMockBackend = BuildConfig.USE_MOCK_BACKEND,
            mockProvider = mockProvider,
            remoteProvider = remoteProvider,
        )
    }
}
