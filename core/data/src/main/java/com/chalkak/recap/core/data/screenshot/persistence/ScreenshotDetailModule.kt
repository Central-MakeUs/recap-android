package com.chalkak.recap.core.data.screenshot.persistence

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ScreenshotDetailModule {
    @Binds
    @Singleton
    abstract fun bindScreenshotDetailRepository(
        repository: SwitchingScreenshotDetailRepository,
    ): ScreenshotDetailRepository
}
