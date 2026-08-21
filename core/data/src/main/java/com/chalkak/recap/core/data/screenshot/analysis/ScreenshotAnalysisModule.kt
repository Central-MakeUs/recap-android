package com.chalkak.recap.core.data.screenshot.analysis

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ScreenshotAnalysisModule {
    @Provides
    @Singleton
    fun provideScreenshotAnalysisRepository(
        demo: DemoScreenshotAnalysisRepository,
    ): ScreenshotAnalysisRepository {
        return demo
    }

    @Provides
    @Singleton
    fun provideScreenshotMockRandomizer(): ScreenshotMockRandomizer {
        return ScreenshotMockRandomizer()
    }
}
