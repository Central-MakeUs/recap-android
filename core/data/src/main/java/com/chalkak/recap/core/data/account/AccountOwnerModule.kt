package com.chalkak.recap.core.data.account

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
annotation class AccountOwnerDataStore

@Module
@InstallIn(SingletonComponent::class)
object AccountOwnerModule {
    @Provides
    @Singleton
    @AccountOwnerDataStore
    fun provideAccountOwnerDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = context.accountOwnerDataStore
}
