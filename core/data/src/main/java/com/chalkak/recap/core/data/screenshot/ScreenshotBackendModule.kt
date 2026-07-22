package com.chalkak.recap.core.data.screenshot

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ScreenshotBackendModule {
    @Binds
    @Singleton
    abstract fun bindScreenshotBackendModeStore(
        impl: DataStoreScreenshotBackendModeStore,
    ): ScreenshotBackendModeStore
}
