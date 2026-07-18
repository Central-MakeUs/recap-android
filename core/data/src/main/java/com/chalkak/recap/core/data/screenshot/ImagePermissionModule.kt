package com.chalkak.recap.core.data.screenshot

import com.chalkak.recap.core.data.LocalScreenshotDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ImagePermissionModule {
    @Binds
    @Singleton
    abstract fun bindImagePermissionRepository(
        localScreenshotDataSource: LocalScreenshotDataSource,
    ): ImagePermissionRepository
}
