package com.chalkak.recap.core.data.user

import com.chalkak.recap.core.data.BuildConfig
import com.chalkak.recap.core.data.backend.BackendSelection
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UserModule {
    @Provides
    @Singleton
    fun provideUserRepository(
        mockProvider: Provider<MockUserRepository>,
        remoteProvider: Provider<RemoteUserRepository>,
    ): UserRepository {
        return BackendSelection.select(
            useMockBackend = BuildConfig.USE_MOCK_BACKEND,
            mockProvider = mockProvider,
            remoteProvider = remoteProvider,
        )
    }
}
