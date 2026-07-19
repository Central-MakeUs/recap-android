package com.chalkak.recap.core.data.screenshot

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ScreenshotAnalysisModule {
    @Binds
    @Singleton
    abstract fun bindScreenshotAnalysisRepository(
        repository: SwitchingScreenshotAnalysisRepository,
    ): ScreenshotAnalysisRepository

    companion object {
        @Provides
        @Singleton
        fun provideScreenshotMockRandomizer(): ScreenshotMockRandomizer {
            return ScreenshotMockRandomizer()
        }
    }
}
