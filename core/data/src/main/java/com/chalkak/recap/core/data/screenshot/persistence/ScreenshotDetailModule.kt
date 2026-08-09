package com.chalkak.recap.core.data.screenshot.persistence

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
object ScreenshotDetailModule {
    @Provides
    @Singleton
    fun provideScreenshotDetailRepository(
        mockProvider: Provider<MockScreenshotDetailRepository>,
        remoteProvider: Provider<RemoteScreenshotDetailRepository>,
    ): ScreenshotDetailRepository {
        return BackendSelection.select(
            useMockBackend = BuildConfig.USE_MOCK_BACKEND,
            mockProvider = mockProvider,
            remoteProvider = remoteProvider,
        )
    }
}
