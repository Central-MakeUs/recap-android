package com.chalkak.recap.core.data.screenshot.persistence

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ScreenshotCardModule {
    @Binds
    @Singleton
    abstract fun bindScreenshotCardRepository(
        repository: DefaultScreenshotCardRepository,
    ): ScreenshotCardRepository
}
