package com.chalkak.recap.core.data.home

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HomeModule {
    @Binds
    @Singleton
    abstract fun bindHomeRepository(
        repository: SwitchingHomeRepository,
    ): HomeRepository

    @Binds
    @Singleton
    abstract fun bindRecentCapturesRepository(
        repository: SwitchingRecentCapturesRepository,
    ): RecentCapturesRepository
}
