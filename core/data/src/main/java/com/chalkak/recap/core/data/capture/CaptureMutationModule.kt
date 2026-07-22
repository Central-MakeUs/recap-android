package com.chalkak.recap.core.data.capture

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CaptureMutationModule {
    @Binds
    @Singleton
    abstract fun bindCaptureMutationRepository(
        repository: SwitchingCaptureMutationRepository,
    ): CaptureMutationRepository
}
