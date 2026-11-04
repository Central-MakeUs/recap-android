package com.chalkak.recap.core.data.network

import com.chalkak.recap.core.data.auth.remote.AuthApi
import com.chalkak.recap.core.data.capture.remote.CaptureApi
import com.chalkak.recap.core.data.home.remote.HomeApi
import com.chalkak.recap.core.data.search.remote.SearchApi
import com.chalkak.recap.core.data.storage.remote.StorageApi
import com.chalkak.recap.core.data.user.remote.UserApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideJson(): Json =
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }

    @Provides
    @Singleton
    fun provideClock(): Clock = Clock.systemUTC()

    @Provides
    @Singleton
    fun provideOkHttpClient(
        offlineNetworkInterceptor: OfflineNetworkInterceptor,
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator,
    ): OkHttpClient {
        return baseClientBuilder(offlineNetworkInterceptor)
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)
            .build()
    }

    @Provides
    @Singleton
    @TokenRefreshApi
    fun provideTokenRefreshClient(
        offlineNetworkInterceptor: OfflineNetworkInterceptor,
    ): OkHttpClient = baseClientBuilder(offlineNetworkInterceptor).build()

    private fun baseClientBuilder(
        offlineNetworkInterceptor: OfflineNetworkInterceptor,
    ): OkHttpClient.Builder = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(offlineNetworkInterceptor)

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json,
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(RecapApiConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    // Interceptors block while awaiting refresh. Its async call must have its own dispatcher.
    @Provides
    @Singleton
    @TokenRefreshApi
    fun provideTokenRefreshApi(
        @TokenRefreshApi okHttpClient: OkHttpClient,
        json: Json,
    ): AuthApi = provideRetrofit(okHttpClient, json).create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideCaptureApi(retrofit: Retrofit): CaptureApi = retrofit.create(CaptureApi::class.java)

    @Provides
    @Singleton
    fun provideHomeApi(retrofit: Retrofit): HomeApi = retrofit.create(HomeApi::class.java)

    @Provides
    @Singleton
    fun provideStorageApi(retrofit: Retrofit): StorageApi = retrofit.create(StorageApi::class.java)

    @Provides
    @Singleton
    fun provideSearchApi(retrofit: Retrofit): SearchApi = retrofit.create(SearchApi::class.java)

    @Provides
    @Singleton
    fun provideUserApi(retrofit: Retrofit): UserApi = retrofit.create(UserApi::class.java)
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TokenRefreshApi
