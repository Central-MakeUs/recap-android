package com.chalkak.recap.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UserPreferencesDataStore

@Module
@InstallIn(SingletonComponent::class)
object UserPreferencesModule {
    @Provides
    @Singleton
    @UserPreferencesDataStore
    fun provideUserPreferencesDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = context.userPreferencesDataStore
}
