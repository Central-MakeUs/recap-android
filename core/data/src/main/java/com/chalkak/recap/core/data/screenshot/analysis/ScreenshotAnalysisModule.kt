package com.chalkak.recap.core.data.screenshot.analysis

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
object ScreenshotAnalysisModule {
    @Provides
    @Singleton
    fun provideScreenshotAnalysisRepository(
        mockProvider: Provider<MockScreenshotAnalysisRepository>,
        remoteProvider: Provider<RemoteScreenshotAnalysisRepository>,
    ): ScreenshotAnalysisRepository {
        return BackendSelection.select(
            useMockBackend = BuildConfig.USE_MOCK_BACKEND,
            mockProvider = mockProvider,
            remoteProvider = remoteProvider,
        )
    }

    @Provides
    @Singleton
    fun provideScreenshotMockRandomizer(): ScreenshotMockRandomizer {
        return ScreenshotMockRandomizer()
    }
}
