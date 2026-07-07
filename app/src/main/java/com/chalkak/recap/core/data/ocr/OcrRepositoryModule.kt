package com.chalkak.recap.core.data.ocr

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class OcrRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindImagePermissionRepository(
        ocrRepository: OcrRepository,
    ): ImagePermissionRepository
}
