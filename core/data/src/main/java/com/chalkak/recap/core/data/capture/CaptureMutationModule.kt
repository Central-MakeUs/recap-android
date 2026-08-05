package com.chalkak.recap.core.data.capture

import com.chalkak.recap.core.data.BuildConfig
import com.chalkak.recap.core.data.backend.BackendSelection
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CaptureMutationModule {
    @Binds
    @Singleton
    abstract fun bindCaptureThumbnailUpdates(
        cache: RemoteCaptureThumbnailCache,
    ): CaptureThumbnailUpdates

    companion object {
        @Provides
        @Singleton
        fun provideCaptureMutationRepository(
            mockProvider: Provider<MockCaptureMutationRepository>,
            remoteProvider: Provider<RemoteCaptureMutationRepository>,
        ): CaptureMutationRepository {
            return BackendSelection.select(
                useMockBackend = BuildConfig.USE_MOCK_BACKEND,
                mockProvider = mockProvider,
                remoteProvider = remoteProvider,
            )
        }
    }
}
